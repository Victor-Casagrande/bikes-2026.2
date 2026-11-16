package br.edu.ifc.bikes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//dataDevolucao e valor ficam nulos enquanto a locação está ATIVA
public record LocacaoResponseDto(
        Long id,
        String nomeCliente,
        String codigoBicicleta,
        LocalDateTime dataRetirada,
        LocalDateTime dataPrevistaDevolucao,
        LocalDateTime dataDevolucao,
        Long duracaoMinutos,
        BigDecimal valor,
        String status,
        String cartaoFinal
) { }
