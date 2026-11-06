package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.ClienteCreateDto;
import br.edu.ifc.bikes.dto.ClienteResponseDto;
import br.edu.ifc.bikes.dto.mapper.ClienteMapper;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.service.ClienteService;
import br.edu.ifc.bikes.service.UsuarioService;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Clientes", description = "Contém todas as operações relativas aos recursos para cadastro e leitura de um cliente. Todas exigem um Bearer Token, obtido em /api/v1/auth.")
@RestController
@RequestMapping("api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final ClienteMapper clienteMapper;

    @Operation(summary = "Criar um novo cliente",
        description = "Recurso para criar um cliente vinculado ao usuário autenticado. Acesso restrito a Role='CLIENTE'.",
        responses = {
                @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil ADMIN)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "409", description = "CPF já cadastrado ou usuário já possui um cliente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada inválidos",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteResponseDto> create(@Valid @RequestBody ClienteCreateDto dto,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        Cliente cliente = clienteMapper.toCliente(dto);
        cliente.setUsuario(usuarioService.buscarPorNome(userDetails.getUsername()));
        Cliente clienteCriado = clienteService.salvar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toResponse(clienteCriado));
    }

    @Operation(summary = "Recuperar um cliente pelo id",
        description = "Recurso para recuperar qualquer cliente pelo id. Acesso restrito a Role='ADMIN'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Recurso recuperado com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil CLIENTE)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDto> getById(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(clienteMapper.toResponse(cliente));
    }

    @Operation(summary = "Listar todos os clientes",
        description = "Recurso para listar todos os clientes cadastrados. Acesso restrito a Role='ADMIN'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Lista com todos os clientes cadastrados",
                        content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = ClienteResponseDto.class)))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil CLIENTE)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDto>> getAll() {
        List<Cliente> clientes = clienteService.buscarTodos();
        return ResponseEntity.ok(clienteMapper.toResponse(clientes));
    }

    @Operation(summary = "Recuperar os dados do cliente autenticado",
        description = "Recurso para o cliente consultar os seus próprios dados. Acesso restrito a Role='CLIENTE'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Recurso recuperado com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil ADMIN)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "O usuário autenticado ainda não possui um cliente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping("/detalhes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteResponseDto> getDetalhes(@Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        Cliente cliente = clienteService.buscarPorUsuarioId(userDetails.getId());
        return ResponseEntity.ok(clienteMapper.toResponse(cliente));
    }
}
