package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.BicicletaCreateDto;
import br.edu.ifc.bikes.dto.BicicletaResponseDto;
import br.edu.ifc.bikes.dto.BicicletaStatusDto;
import br.edu.ifc.bikes.dto.mapper.BicicletaMapper;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.service.BicicletaService;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bicicletas", description = "Contém todas as operações relativas aos recursos para cadastro, leitura e alteração de status de uma bicicleta. Todas exigem um Bearer Token de ADMIN, obtido em /api/v1/auth.")
@RestController
@RequestMapping("api/v1/bicicletas")
@RequiredArgsConstructor
public class BicicletaController {

    private final BicicletaService bicicletaService;
    private final BicicletaMapper bicicletaMapper;

    @Operation(summary = "Criar uma nova bicicleta",
        description = "Recurso para cadastrar uma bicicleta. Acesso restrito a Role='ADMIN'.",
        responses = {
                @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BicicletaResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil CLIENTE)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "409", description = "Código da bicicleta já cadastrado",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada inválidos",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BicicletaResponseDto> create(@Valid @RequestBody BicicletaCreateDto dto) {
        Bicicleta bicicleta = bicicletaService.salvar(bicicletaMapper.toBicicleta(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(bicicletaMapper.toResponse(bicicleta));
    }

    @Operation(summary = "Recuperar uma bicicleta pelo código",
        description = "Recurso para recuperar uma bicicleta pelo código. Acesso restrito a Role='ADMIN'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Recurso recuperado com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BicicletaResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil CLIENTE)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "Bicicleta não encontrada",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BicicletaResponseDto> getByCodigo(@PathVariable String codigo) {
        Bicicleta bicicleta = bicicletaService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(bicicletaMapper.toResponse(bicicleta));
    }

    @Operation(summary = "Listar todas as bicicletas",
        description = "Recurso para listar todas as bicicletas cadastradas. Acesso restrito a Role='ADMIN'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Lista com todas as bicicletas cadastradas",
                        content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = BicicletaResponseDto.class)))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil CLIENTE)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BicicletaResponseDto>> getAll() {
        List<Bicicleta> bicicletas = bicicletaService.buscarTodas();
        return ResponseEntity.ok(bicicletaMapper.toResponse(bicicletas));
    }

    @Operation(summary = "Alterar o status de uma bicicleta",
        description = "Recurso para colocar uma bicicleta em manutenção (MANUTENCAO) ou liberá-la (LIVRE). " +
                "Uma bicicleta ALUGADA só muda de status ao registrar a devolução. Acesso restrito a Role='ADMIN'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Status alterado com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BicicletaResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil CLIENTE)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "Bicicleta não encontrada",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "409", description = "Bicicleta alugada não pode mudar de status",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "422", description = "Status inválido",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @PatchMapping("/{codigo}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BicicletaResponseDto> updateStatus(@PathVariable String codigo, @Valid @RequestBody BicicletaStatusDto dto) {
        Bicicleta bicicleta = bicicletaService.alterarStatus(codigo, Bicicleta.StatusBicicleta.valueOf(dto.status()));
        return ResponseEntity.ok(bicicletaMapper.toResponse(bicicleta));
    }
}
