package br.edu.ifc.bikes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

//linha do relatório de locações e devoluções (requisito 8); datas no formato dd/MM/yyyy HH:mm
public record LocacaoRelatorioDto(
        Long id,
        String nomeCliente,
        String codigoBicicleta,
        @JsonFormat(pattern = LocacaoResponseDto.FORMATO_DATA)
        @Schema(type = "string", example = "20/11/2026 08:50")
        LocalDateTime dataRetirada,
        @JsonFormat(pattern = LocacaoResponseDto.FORMATO_DATA)
        @Schema(type = "string", example = "20/11/2026 10:15")
        LocalDateTime dataDevolucao,
        Long duracaoMinutos,
        String status
) { }
