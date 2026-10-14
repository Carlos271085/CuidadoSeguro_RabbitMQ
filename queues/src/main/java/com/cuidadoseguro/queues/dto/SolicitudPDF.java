package com.cuidadoseguro.queues.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class SolicitudPDF {

    private String requestId;
    private DatosPDF datos;
}