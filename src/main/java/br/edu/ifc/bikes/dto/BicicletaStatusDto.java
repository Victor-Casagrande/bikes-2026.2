package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

//usado pelo admin para colocar a bicicleta em manutenção ou liberá-la de novo
public record BicicletaStatusDto(
        @NotBlank(message = "O status é obrigatório")
        @Pattern(regexp = "LIVRE|MANUTENCAO", message = "O status deve ser LIVRE ou MANUTENCAO")
        String status
) { }
