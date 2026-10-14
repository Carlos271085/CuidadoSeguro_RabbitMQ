package com.cuidadoseguro.queues.service;


import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class PdfStorageService {

    private final ConcurrentHashMap<String, byte[]> archivos =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> errores =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Boolean> pendientes =
            new ConcurrentHashMap<>();

    public void iniciar(String requestId) {
        pendientes.put(requestId, true);
    }

    public void guardar(String requestId, byte[] pdf) {
        archivos.put(requestId, pdf);
        pendientes.remove(requestId);
        errores.remove(requestId);
    }

    public void registrarError(String requestId, String mensaje) {
        errores.put(requestId, mensaje);
        pendientes.remove(requestId);
    }

    public byte[] obtener(String requestId) {
        return archivos.get(requestId);
    }

    public boolean estaPendiente(String requestId) {
        return pendientes.containsKey(requestId);
    }

    public String obtenerError(String requestId) {
        return errores.get(requestId);
    }

    public boolean existe(String requestId) {
        return pendientes.containsKey(requestId)
                || archivos.containsKey(requestId)
                || errores.containsKey(requestId);
    }
}