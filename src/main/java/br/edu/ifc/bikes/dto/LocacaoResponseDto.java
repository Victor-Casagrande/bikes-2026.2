package br.edu.ifc.bikes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//dataDevolucao e valor ficam nulos enquanto a locação está ATIVA
//as datas saem no formato brasileiro (dd/MM/yyyy HH:mm)
public record LocacaoResponseDto(
        Long id,
        String nomeCliente,
        String codigoBicicleta,
        @JsonFormat(pattern = LocacaoResponseDto.FORMATO_DATA)
        @Schema(type = "string", example = "20/11/2026 08:50")
        LocalDateTime dataRetirada,
        @JsonFormat(pattern = LocacaoResponseDto.FORMATO_DATA)
        @Schema(type = "string", example = "21/11/2026 18:00")
        LocalDateTime dataPrevistaDevolucao,
        @JsonFormat(pattern = LocacaoResponseDto.FORMATO_DATA)
        @Schema(type = "string", example = "20/11/2026 10:15")
        LocalDateTime dataDevolucao,
        Long duracaoMinutos,
        BigDecimal valor,
        String status,
        String cartaoFinal
) {
    public static final String FORMATO_DATA = "dd/MM/yyyy HH:mm";
}
