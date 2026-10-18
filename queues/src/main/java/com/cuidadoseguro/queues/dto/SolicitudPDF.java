package com.cuidadoseguro.queues.dto;

public class SolicitudPDF {

    private String requestId;
    private DatosPDF datos;

    public SolicitudPDF(String requestId, DatosPDF datos) {
        this.requestId = requestId;
        this.datos = datos;
    }
    public SolicitudPDF() {
    }
    public String getRequestId() {
        return requestId;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public DatosPDF getDatos() {
        return datos;
    }
    public void setDatos(DatosPDF datos) {
        this.datos = datos;
    }
}