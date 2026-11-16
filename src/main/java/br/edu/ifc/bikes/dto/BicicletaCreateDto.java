package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BicicletaCreateDto(
        @NotBlank(message = "O código é obrigatório")
        @Size(min = 4, max = 10, message = "O código deve ter entre 4 e 10 caracteres")
        String codigo,
        @NotBlank(message = "O modelo é obrigatório")
        @Size(min = 2, max = 100, message = "O modelo deve ter entre 2 e 100 caracteres")
        String modelo,
        @NotBlank(message = "O status é obrigatório")
        //ALUGADA não é aceito: esse status só é definido ao registrar uma locação
        @Pattern(regexp = "LIVRE|MANUTENCAO", message = "O status deve ser LIVRE ou MANUTENCAO")
        String status
) { }
