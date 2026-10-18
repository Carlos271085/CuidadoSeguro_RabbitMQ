package com.cuidadoseguro.queues.dto;


public class SolicitudCorreoPDF {

    private String requestId;
    private String correoDestino;
    private String nombre;
    private String nombreArchivo;
    private String contenidoBase64;

    

    public SolicitudCorreoPDF() {
    }
    public SolicitudCorreoPDF(String requestId, String correoDestino, String nombre, String nombreArchivo,
            String contenidoBase64) {
        this.requestId = requestId;
        this.correoDestino = correoDestino;
        this.nombre = nombre;
        this.nombreArchivo = nombreArchivo;
        this.contenidoBase64 = contenidoBase64;
    }
    public String getRequestId() {
        return requestId;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public String getCorreoDestino() {
        return correoDestino;
    }
    public void setCorreoDestino(String correoDestino) {
        this.correoDestino = correoDestino;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getNombreArchivo() {
        return nombreArchivo;
    }
    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
    public String getContenidoBase64() {
        return contenidoBase64;
    }
    public void setContenidoBase64(String contenidoBase64) {
        this.contenidoBase64 = contenidoBase64;
    }

    
    
}