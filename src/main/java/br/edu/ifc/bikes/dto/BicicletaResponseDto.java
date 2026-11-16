package br.edu.ifc.bikes.dto;

public record BicicletaResponseDto(
        Long id,
        String codigo,
        String modelo,
        String status
) { }
