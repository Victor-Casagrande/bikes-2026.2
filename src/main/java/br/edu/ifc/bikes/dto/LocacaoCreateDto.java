package br.edu.ifc.bikes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

//a bicicleta não é informada: o sistema escolhe uma livre; a retirada é registrada no momento da solicitação
public record LocacaoCreateDto(
        @NotNull(message = "A data/hora prevista para devolução é obrigatória")
        @Future(message = "A data/hora prevista para devolução deve ser futura")
        LocalDateTime dataPrevistaDevolucao,
        @NotNull(message = "Os dados do cartão de crédito são obrigatórios")
        @Valid
        CartaoDto cartao
) { }
