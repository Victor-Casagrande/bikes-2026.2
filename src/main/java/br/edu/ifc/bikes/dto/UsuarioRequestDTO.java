package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//@NotBlank testa o envio de ao menos 1 caracter
public record UsuarioRequestDTO(
        @NotBlank
        @Email(message = "O email deve ser válido")
        String username,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min=6, max=6)
        String password,
        //opcional: sem role o usuário é CLIENTE; ADMIN só pode ser criado por um admin
        //(ou, se ainda não houver nenhum admin, para criar o primeiro)
        @Pattern(regexp = "(ROLE_)?(ADMIN|CLIENTE)", message = "O perfil deve ser ADMIN ou CLIENTE")
        String role
) {
    public UsuarioRequestDTO(String username, String password) {
        this(username, password, null);
    }
}
