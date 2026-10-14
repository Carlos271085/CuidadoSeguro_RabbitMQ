
package com.cuidadoseguro.queues.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    private String tipo_documento;
}
