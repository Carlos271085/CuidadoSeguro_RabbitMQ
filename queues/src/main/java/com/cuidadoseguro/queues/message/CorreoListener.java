
package com.cuidadoseguro.queues.message;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.cuidadoseguro.queues.config.RabbitConfig;
import com.rabbitmq.client.Channel;

@Component
public class CorreoListener {

    private static final Logger logger =
            LoggerFactory.getLogger(CorreoListener.class);

    private final Inbox bandeja;

    public CorreoListener(Inbox bandeja) {
        this.bandeja = bandeja;
    }

    @RabbitListener(
        queues = RabbitConfig.COLA_CORREO,
        containerFactory = "manualFactory"
    )
    public void correo(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        try {
            logger.info("Correo recibido: {}", message);

            bandeja.anotar(
                    RabbitConfig.COLA_CORREO,
                    "Procesado: " + message);

            channel.basicAck(tag, false);

        } catch (Exception e) {

            logger.error("Error procesando correo", e);

            channel.basicNack(tag, false, false);
        }
    }
}
