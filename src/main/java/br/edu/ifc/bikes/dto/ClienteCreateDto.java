package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteCreateDto(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 5, max = 100, message = "O nome deve ter entre 5 e 100 caracteres")
        String nome,
        //apenas os 11 dígitos, sem pontos e traço; @CPF confere os dígitos verificadores
        @NotBlank(message = "O CPF é obrigatório")
        @Size(min = 11, max = 11, message = "O CPF deve ter 11 dígitos, sem pontos e traço")
        @CPF(message = "O CPF é inválido")
        String cpf
) { }
