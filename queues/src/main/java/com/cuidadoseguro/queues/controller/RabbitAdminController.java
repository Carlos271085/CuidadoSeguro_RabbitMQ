package com.cuidadoseguro.queues.controller;

import java.util.Map;

import com.cuidadoseguro.queues.service.RabbitAdminService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rabbitmq")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues/{name}")
    public ResponseEntity<?> crearCola(
            @PathVariable String name) {

        if (!nombreValido(name)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Nombre de cola inválido"));
        }

        service.crearCola(name);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("mensaje", "Cola declarada", "nombre", name));
    }

    
    @PostMapping("/exchanges/direct/{name}")
    public ResponseEntity<?> crearDirect(
            @PathVariable String name) {

        if (!nombreValido(name)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Nombre de exchange inválido"));
        }

        service.crearExchangeDirect(name);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("mensaje", "Exchange Direct declarado",
                       "nombre", name));
    }

    @PostMapping("/exchanges/topic/{name}")
    public ResponseEntity<?> crearTopic(
            @PathVariable String name) {

        if (!nombreValido(name)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Nombre de exchange inválido"));
        }

        service.crearExchangeTopic(name);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("mensaje", "Exchange Topic declarado",
                       "nombre", name));
    }

    
    private boolean nombreValido(String name) {
        return name != null
                && name.matches("[a-zA-Z0-9._-]{1,100}");
    }

        @PostMapping("/bindings/direct")
        public ResponseEntity<?> crearBindingDirect(
                @RequestParam String cola,
                @RequestParam String exchange,
                @RequestParam String routingKey) {

            if (!nombreValido(cola)
                    || !nombreValido(exchange)
                    || routingKey == null
                    || routingKey.isBlank()
                    || routingKey.length() > 100) {

                return ResponseEntity.badRequest().body(
                        Map.of("error", "Parámetros de binding inválidos"));
            }

            service.crearBindingDirect(cola, exchange, routingKey);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("mensaje", "Binding Direct declarado"));
        }

        @PostMapping("/bindings/topic")
        public ResponseEntity<?> crearBindingTopic(
                @RequestParam String cola,
                @RequestParam String exchange,
                @RequestParam String routingKey) {

            if (!nombreValido(cola)
                    || !nombreValido(exchange)
                    || routingKey == null
                    || routingKey.isBlank()
                    || routingKey.length() > 100) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Parámetros de binding inválidos"));
            }

            service.crearBindingTopic(cola, exchange, routingKey);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("mensaje", "Binding Topic declarado"));
        }

                
        @DeleteMapping("/queues/{name}")
        public ResponseEntity<?> eliminarCola(
                @PathVariable String name) {

        if (!nombreValido(name)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Nombre de cola inválido"));
        }

        service.eliminarCola(name);

        return ResponseEntity.ok(
                Map.of("mensaje", "Solicitud de eliminación ejecutada",
                        "nombre", name));
        }

        @DeleteMapping("/exchanges/{name}")
        public ResponseEntity<?> eliminarExchange(
                @PathVariable String name) {

        if (!nombreValido(name)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Nombre de exchange inválido"));
        }

        service.eliminarExchange(name);

        return ResponseEntity.ok(
                Map.of("mensaje", "Solicitud de eliminación ejecutada",
                        "nombre", name));
        }

}
