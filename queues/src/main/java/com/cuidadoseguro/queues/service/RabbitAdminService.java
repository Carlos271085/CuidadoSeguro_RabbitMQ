
package com.cuidadoseguro.queues.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void crearCola(String nombre) {
        amqpAdmin.declareQueue(
                new Queue(nombre, true));
    }

    public void eliminarCola(String nombre) {
        amqpAdmin.deleteQueue(nombre);
    }

    public void crearExchangeDirect(String nombre) {
        amqpAdmin.declareExchange(
                new DirectExchange(nombre, true, false));
    }

    public void crearExchangeTopic(String nombre) {
        amqpAdmin.declareExchange(
                new TopicExchange(nombre, true, false));
    }

    public void eliminarExchange(String nombre) {
        amqpAdmin.deleteExchange(nombre);
    }

    
        public void crearBindingDirect(
                String nombreCola,
                String nombreExchange,
                String routingKey) {

        Queue cola = new Queue(nombreCola, true);

        DirectExchange exchange =
                new DirectExchange(nombreExchange, true, false);

        amqpAdmin.declareQueue(cola);
        amqpAdmin.declareExchange(exchange);

        Binding binding = BindingBuilder
                .bind(cola)
                .to(exchange)
                .with(routingKey);

        amqpAdmin.declareBinding(binding);
        }

        public void crearBindingTopic(
                String nombreCola,
                String nombreExchange,
                String routingKey) {

        Queue cola = new Queue(nombreCola, true);

        TopicExchange exchange =
                new TopicExchange(nombreExchange, true, false);

        amqpAdmin.declareQueue(cola);
        amqpAdmin.declareExchange(exchange);

        Binding binding = BindingBuilder
                .bind(cola)
                .to(exchange)
                .with(routingKey);

        amqpAdmin.declareBinding(binding);
        }


    public void eliminarBindingDirect(
            String nombreCola,
            String nombreExchange,
            String routingKey) {

        Queue cola = new Queue(nombreCola, true);

        DirectExchange exchange =
                new DirectExchange(nombreExchange, true, false);

        Binding binding = BindingBuilder
                .bind(cola)
                .to(exchange)
                .with(routingKey);

        amqpAdmin.removeBinding(binding);
    }

    public void eliminarBindingTopic(
            String nombreCola,
            String nombreExchange,
            String routingKey) {

        Queue cola = new Queue(nombreCola, true);

        TopicExchange exchange =
                new TopicExchange(nombreExchange, true, false);

        Binding binding = BindingBuilder
                .bind(cola)
                .to(exchange)
                .with(routingKey);

        amqpAdmin.removeBinding(binding);
    }
}
