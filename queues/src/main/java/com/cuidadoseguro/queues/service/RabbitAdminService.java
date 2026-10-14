
package com.cuidadoseguro.queues.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
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

    public void crearExchangeFanout(String nombre) {
        amqpAdmin.declareExchange(
                new FanoutExchange(nombre, true, false));
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

        public void crearBindingFanout(
                String nombreCola,
                String nombreExchange) {

        Queue cola = new Queue(nombreCola, true);

        FanoutExchange exchange =
                new FanoutExchange(nombreExchange, true, false);

        amqpAdmin.declareQueue(cola);
        amqpAdmin.declareExchange(exchange);

        Binding binding = BindingBuilder
                .bind(cola)
                .to(exchange);

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

    public void eliminarBindingFanout(
            String nombreCola,
            String nombreExchange) {

        Queue cola = new Queue(nombreCola, true);

        FanoutExchange exchange =
                new FanoutExchange(nombreExchange, true, false);

        Binding binding = BindingBuilder
                .bind(cola)
                .to(exchange);

        amqpAdmin.removeBinding(binding);
    }
}
