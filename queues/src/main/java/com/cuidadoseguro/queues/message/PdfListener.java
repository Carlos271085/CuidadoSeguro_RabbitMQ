
package com.cuidadoseguro.queues.message;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.cuidadoseguro.queues.config.RabbitConfig;
import com.cuidadoseguro.queues.dto.SolicitudPDF;
import com.cuidadoseguro.queues.service.PdfStorageService;
import com.rabbitmq.client.Channel;

@Component
public class PdfListener {

    private static final Logger logger =
            LoggerFactory.getLogger(PdfListener.class);

    private final RestClient restClient;
    private final PdfStorageService storage;

    public PdfListener(
            RestClient.Builder builder,
            PdfStorageService storage,
            @Value("${pdf.service.base-url:http://localhost:8000}") String pdfServiceBaseUrl) {

        this.restClient = builder
                .baseUrl(pdfServiceBaseUrl)
                .build();

        this.storage = storage;
    }

    @RabbitListener(
        queues = RabbitConfig.COLA_PDF_DI,
        containerFactory = "manualFactory"
    )
    public void generarPDFDirect(
            SolicitudPDF solicitud,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        procesarPDF(solicitud, channel, tag);
    }

    @RabbitListener(
        queues = RabbitConfig.COLA_PDF_FO,
        containerFactory = "manualFactory"
    )
    public void generarPDFFanout(
            SolicitudPDF solicitud,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        procesarPDF(solicitud, channel, tag);
    }

    private void procesarPDF(
            SolicitudPDF solicitud,
            Channel channel,
            long tag) throws IOException {

        try {
            String requestId = solicitud.getRequestId();

            logger.info("Procesando PDF: {}", requestId);

            byte[] pdf = restClient.post()
                    .uri("/api/pdf/generar")
                    .body(solicitud.getDatos())
                    .retrieve()
                    .body(byte[].class);

            if (pdf == null || pdf.length == 0) {
                throw new IllegalStateException(
                        "El servicio PDF devolvió un archivo vacío");
            }

            storage.guardar(requestId, pdf);

            channel.basicAck(tag, false);

            logger.info(
                    "PDF generado y mensaje confirmado: {}",
                    requestId);

        } catch (Exception e) {

            logger.error(
                    "Error procesando solicitud PDF",
                    e);

            try {
                storage.registrarError(
                        solicitud.getRequestId(),
                        "Error al generar el PDF");

                channel.basicNack(tag, false, false);

            } catch (Exception ackError) {
                logger.error(
                        "No se pudo rechazar el mensaje",
                        ackError);

                throw new IOException(
                        "Falló el procesamiento y el rechazo del mensaje",
                        ackError);
            }
        }
    }
}
