package br.edu.ifc.bikes.dto;

public record ClienteResponseDto(
        Long id,
        String nome,
        String cpf
) { }
