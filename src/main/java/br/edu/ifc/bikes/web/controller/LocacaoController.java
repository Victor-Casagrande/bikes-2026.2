package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.DevolucaoDto;
import br.edu.ifc.bikes.dto.LocacaoCreateDto;
import br.edu.ifc.bikes.dto.LocacaoRelatorioDto;
import br.edu.ifc.bikes.dto.LocacaoResponseDto;
import br.edu.ifc.bikes.dto.mapper.LocacaoMapper;
import br.edu.ifc.bikes.entity.Locacao;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.service.LocacaoService;
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

@Tag(name = "Locações", description = "Contém as operações de locação, devolução e relatório de bicicletas. Todas exigem um Bearer Token, obtido em /api/v1/auth.")
@RestController
@RequestMapping("api/v1/locacoes")
@RequiredArgsConstructor
public class LocacaoController {

    private final LocacaoService locacaoService;
    private final LocacaoMapper locacaoMapper;

    @Operation(summary = "Solicitar a locação de uma bicicleta",
        description = "O sistema escolhe uma bicicleta livre e registra a data/hora de retirada. O cliente informa a data/hora " +
                "prevista para devolução e os dados do cartão de crédito (somente os 4 últimos dígitos são gravados). " +
                "Acesso restrito a Role='CLIENTE' com cadastro de cliente.",
        responses = {
                @ApiResponse(responseCode = "201", description = "Locação criada com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocacaoResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil ADMIN)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "O usuário autenticado ainda não possui cadastro de cliente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "409", description = "Não há bicicletas disponíveis",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos (data, cartão)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<LocacaoResponseDto> create(@Valid @RequestBody LocacaoCreateDto dto,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        Locacao locacao = locacaoService.solicitar(userDetails.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoMapper.toResponse(locacao));
    }

    @Operation(summary = "Listar as locações do cliente autenticado",
        description = "Recurso para o cliente consultar as suas próprias locações. Acesso restrito a Role='CLIENTE'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Lista com as locações do cliente",
                        content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = LocacaoResponseDto.class)))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil ADMIN)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping("/minhas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<LocacaoResponseDto>> getMinhas(@Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        List<Locacao> locacoes = locacaoService.buscarPorUsuario(userDetails.getId());
        return ResponseEntity.ok(locacaoMapper.toResponse(locacoes));
    }

    @Operation(summary = "Relatório de locações e devoluções",
        description = "Relação de todas as locações com nome do cliente, código da bicicleta, data de retirada e devolução, " +
                "duração (em minutos; para locações ativas, até o momento) e status. Acesso restrito a Role='ADMIN'.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                        content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = LocacaoRelatorioDto.class)))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou perfil CLIENTE)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping("/relatorio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocacaoRelatorioDto>> getRelatorio() {
        return ResponseEntity.ok(locacaoMapper.toRelatorio(locacaoService.buscarTodas()));
    }

    @Operation(summary = "Recuperar uma locação pelo id",
        description = "O ADMIN recupera qualquer locação; o CLIENTE, apenas as suas.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Recurso recuperado com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocacaoResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou locação de outro cliente)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "Locação não encontrada",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<LocacaoResponseDto> getById(@PathVariable Long id,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        return ResponseEntity.ok(locacaoMapper.toResponse(locacaoService.buscarPorId(id, userDetails)));
    }

    @Operation(summary = "Registrar a devolução da bicicleta",
        description = "Registra a data/hora efetiva de devolução, calcula o tempo de uso e o valor a ser debitado no cartão " +
                "(por hora iniciada, mínimo de 1 hora). A bicicleta volta a LIVRE, ou vai para MANUTENCAO se o corpo " +
                "informar {\"manutencao\": true}. O ADMIN devolve qualquer locação; o CLIENTE, apenas as suas.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Devolução registrada; a resposta traz a duração e o valor",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocacaoResponseDto.class))),
                @ApiResponse(responseCode = "403", description = "Usuário sem permissão (sem token ou locação de outro cliente)",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "Locação não encontrada",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "409", description = "A locação já foi finalizada",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    @PatchMapping("/{id}/devolucao")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<LocacaoResponseDto> devolver(@PathVariable Long id,
                                                       @RequestBody(required = false) DevolucaoDto dto,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        boolean manutencao = dto != null && Boolean.TRUE.equals(dto.manutencao());
        Locacao locacao = locacaoService.devolver(id, userDetails, manutencao);
        return ResponseEntity.ok(locacaoMapper.toResponse(locacao));
    }
}
