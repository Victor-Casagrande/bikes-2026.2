package br.edu.ifc.bikes.dto;

import java.time.LocalDateTime;

//linha do relatório de locações e devoluções (requisito 8)
public record LocacaoRelatorioDto(
        Long id,
        String nomeCliente,
        String codigoBicicleta,
        LocalDateTime dataRetirada,
        LocalDateTime dataDevolucao,
        Long duracaoMinutos,
        String status
) { }
