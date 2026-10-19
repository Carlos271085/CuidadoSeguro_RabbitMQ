
package com.cuidadoseguro.queues.message;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.util.Base64;
import org.springframework.web.client.RestClient;
import com.cuidadoseguro.queues.dto.SolicitudCorreoPDF;
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
    private final RabbitTemplate rabbitTemplate;

    public PdfListener(
            RestClient.Builder builder,
            PdfStorageService storage,
            RabbitTemplate rabbitTemplate,
            @Value("${pdf.service.base-url:http://cuidadoseguro-pdf-130063674.us-east-1.elb.amazonaws.com:8000}") String pdfServiceBaseUrl) {

        this.restClient = builder
                .baseUrl(pdfServiceBaseUrl)
                .build();

        this.storage = storage;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(
        queues = RabbitConfig.COLA_PDF_DI,
        containerFactory = "manualFactory"
    )
    public byte[] generarPDFDirect(
            SolicitudPDF solicitud,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        return procesarPDF(solicitud, channel, tag);
    }

    @RabbitListener(
        queues = RabbitConfig.COLA_PDF_TOPIC,
        containerFactory = "manualFactory"
    )
    public byte[] generarPDFTopic(
            SolicitudPDF solicitud,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        return procesarPDF(solicitud, channel, tag);
    }

    private byte[] procesarPDF(
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

            if (solicitud.getDatos().isEnviarCorreo()) {
                String correoDestino = solicitud.getDatos().getCorreoDestino();

                if (correoDestino == null || correoDestino.isBlank()) {
                    throw new IllegalArgumentException(
                            "El correo de destino es obligatorio para enviar el PDF");
                }

                SolicitudCorreoPDF correo = new SolicitudCorreoPDF(
                        requestId,
                        correoDestino,
                        solicitud.getDatos().getNombre(),
                        "ficha-clinica-" + requestId + ".pdf",
                        Base64.getEncoder().encodeToString(pdf));

                rabbitTemplate.convertAndSend(
                        RabbitConfig.TOPIC_EXCHANGE,
                        RabbitConfig.CORREO_ENVIAR_KEY,
                        correo);
            }

            channel.basicAck(tag, false);

            logger.info(
                    "PDF generado para solicitud: {}",
                    requestId);

            return pdf;

        } catch (Exception e) {

            logger.error(
                    "Error procesando solicitud PDF",
                    e);

            try {
                storage.registrarError(
                        solicitud.getRequestId(),
                        "Error al generar el PDF o encolar el correo");

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
        return new byte[0];
    }
}
