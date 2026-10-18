package com.cuidadoseguro.queues.controller;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import com.cuidadoseguro.queues.config.RabbitConfig;
import com.cuidadoseguro.queues.dto.DatosPDF;
import com.cuidadoseguro.queues.dto.SolicitudPDF;
import com.cuidadoseguro.queues.message.Inbox;
import com.cuidadoseguro.queues.service.PdfStorageService;

@RestController 
@RequestMapping("/api")
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private static final Set<String> NIVELES = Set.of("INFO", "WARNING", "ERROR");

    private final RabbitTemplate rabbitTemplate;
    private final Inbox bandeja;
    private final RestClient pdfClient;
    private final PdfStorageService storage;
    private final RestClient.Builder restClientBuilder;

    public MainController(
            RabbitTemplate rabbitTemplate,
            Inbox bandeja,
            RestClient.Builder builder,
            PdfStorageService storage,
            @Value("${pdf.service.base-url:http://localhost:8000}") String pdfServiceBaseUrl) {
        this.rabbitTemplate = rabbitTemplate;
        this.bandeja = bandeja;
        this.storage = storage;
        this.restClientBuilder = builder;
        this.pdfClient = builder
            .baseUrl(pdfServiceBaseUrl)
            .build();
    }

    @GetMapping
    public Map<String, String> ayuda() {
        return Map.of(
                "pdf_topic", "POST /api/pdf_topic Emite un PDF a la cola de PDFs (topic)",
                "pdf_direct", "GET /api/pdf_direct  Emite un PDF a la cola de PDFs (direct)"
        );
    }

    @PostMapping("/descargar")
    public ResponseEntity<Map<String, String>> solicitarPDF(
            @Valid @RequestBody DatosPDF datos) {

        String requestId = UUID.randomUUID().toString();

        storage.iniciar(requestId);

        SolicitudPDF solicitud =
                new SolicitudPDF(requestId, datos);

        rabbitTemplate.convertAndSend(
                RabbitConfig.DI_EXCHANGE,
                RabbitConfig.PDF_DI,
                solicitud
        );

        return ResponseEntity.accepted().body(Map.of(
                "requestId", requestId,
                "estado", "PROCESANDO",
                "mensaje", "Consulta el estado y descarga el PDF cuando esté listo"
        ));
    }

    @GetMapping("/descargar/{requestId}")
    public ResponseEntity<?> descargarPDF(
            @PathVariable String requestId) {

        if (!storage.existe(requestId)) {
            return ResponseEntity.notFound().build();
        }

        String error = storage.obtenerError(requestId);

        if (error != null) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("estado", "ERROR", "mensaje", error));
        }

        byte[] pdf = storage.obtener(requestId);

        if (pdf == null) {
            return ResponseEntity.accepted().body(Map.of(
                    "requestId", requestId,
                    "estado", "PROCESANDO"
            ));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ficha-clinica.pdf\""
                )
                .body(pdf);
    }

    @PostMapping(value = "/pdf_direct", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPDFDirecto(
            @Valid @RequestBody DatosPDF datos) {
        byte[] pdf = pdfClient.post()
                .uri("/api/pdf/generar")
                .body(datos)
                .retrieve()
                .body(byte[].class);

        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException("El servicio PDF devolvió un archivo vacío");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ficha-clinica.pdf\"")
                .body(pdf);
    }

    @PostMapping("/pdf_topic")
        public ResponseEntity<Map<String, String>> solicitarPDFTopic(
                @Valid @RequestBody DatosPDF datos) {

        String requestId = UUID.randomUUID().toString();

        storage.iniciar(requestId);

        SolicitudPDF solicitud =
                new SolicitudPDF(requestId, datos);

        rabbitTemplate.convertAndSend(
                RabbitConfig.PDF_TOPIC_EXCHANGE,
                RabbitConfig.PDF_GENERAR_KEY,
                solicitud
        );

        log.info(
                "Solicitud PDF enviada por Topic. ID: {}",
                requestId
        );

        return ResponseEntity.accepted().body(Map.of(
                "requestId", requestId,
                "exchange", RabbitConfig.PDF_TOPIC_EXCHANGE,
                "routingKey", RabbitConfig.PDF_GENERAR_KEY,
                "estado", "PROCESANDO"
        ));
        }

}
