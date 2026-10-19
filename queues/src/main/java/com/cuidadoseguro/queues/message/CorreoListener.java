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
import com.cuidadoseguro.queues.dto.SolicitudCorreoPDF;
import com.rabbitmq.client.Channel;

@Component
public class CorreoListener {

    private static final Logger logger =
            LoggerFactory.getLogger(CorreoListener.class);

    private static final String NOTIFICATION_SERVICE_BASE_URL = "http://cuidadoseguro-notif-1042718212.us-east-1.elb.amazonaws.com:8089";

    private final Inbox bandeja;
    private final RestClient notificationClient;

    // Constructor simplificado inyectando únicamente los Beans requeridos
    public CorreoListener(Inbox bandeja, RestClient.Builder builder) {
        this.bandeja = bandeja;
        this.notificationClient = builder
                .baseUrl(NOTIFICATION_SERVICE_BASE_URL)
                .build();
    }

    @RabbitListener(
        queues = RabbitConfig.COLA_CORREO,
        containerFactory = "manualFactory"
    )
    public void correo(
            SolicitudCorreoPDF solicitud,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        try {
            logger.info(
                    "Solicitud de correo recibida para PDF: {}",
                    solicitud.getRequestId());

            notificationClient.post()
                    .uri("/api/correos/enviar-pdf")
                    .body(solicitud)
                    .retrieve()
                    .toBodilessEntity();

            bandeja.anotar(
                    RabbitConfig.COLA_CORREO,
                    "Correo enviado para solicitud PDF: "
                            + solicitud.getRequestId());
            logger.info(
                    "Correo enviado para solicitud PDF: {}",
                    solicitud.getRequestId());

            channel.basicAck(tag, false);

        } catch (Exception e) {
            logger.error("Error enviando correo", e);

            channel.basicNack(tag, false, false);
        }
    }
}
