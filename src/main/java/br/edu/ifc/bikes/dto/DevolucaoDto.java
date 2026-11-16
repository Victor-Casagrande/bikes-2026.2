package br.edu.ifc.bikes.dto;

//corpo opcional da devolução: manutencao = true envia a bicicleta para MANUTENCAO em vez de LIVRE
public record DevolucaoDto(
        Boolean manutencao
) { }
