
package com.cuidadoseguro.queues.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DatosPDF {

    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 12)
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;

    @NotBlank(message = "La fecha de nacimiento es obligatoria")
    @JsonProperty("fecha_nacimiento")
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "La fecha debe tener formato yyyy-MM-dd"
    )
    private String fechaNacimiento;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @JsonProperty("tipo_documento")
    @Size(max = 100)
    private String tipoDocumento;

    @Email(message = "El correo de destino no es válido")
    @JsonProperty("correo_destino")
    private String correoDestino;

    @JsonProperty("enviar_correo")
    private boolean enviarCorreo = true;

    public DatosPDF() {
    }

    public DatosPDF(
            String rut,
            String nombre,
            String fechaNacimiento,
            String tipoDocumento) {
        this.rut = rut;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoDocumento = tipoDocumento;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getCorreoDestino() {
        return correoDestino;
    }

    public void setCorreoDestino(String correoDestino) {
        this.correoDestino = correoDestino;
    }

    public boolean isEnviarCorreo() {
        return enviarCorreo;
    }

    public void setEnviarCorreo(boolean enviarCorreo) {
        this.enviarCorreo = enviarCorreo;
    }
}
