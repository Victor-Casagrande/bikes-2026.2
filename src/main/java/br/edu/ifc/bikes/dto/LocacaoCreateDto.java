package br.edu.ifc.bikes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

//a bicicleta não é informada: o sistema escolhe uma livre; a retirada é registrada no momento da solicitação
public record LocacaoCreateDto(
        @NotNull(message = "A data/hora prevista para devolução é obrigatória")
        @Future(message = "A data/hora prevista para devolução deve ser futura")
        @Schema(description = "Formato ISO: aaaa-mm-ddThh:mm:ss", example = "2026-11-21T18:00:00")
        LocalDateTime dataPrevistaDevolucao,
        @NotNull(message = "Os dados do cartão de crédito são obrigatórios")
        @Valid
        CartaoDto cartao
) { }
