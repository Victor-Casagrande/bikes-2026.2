package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.LocacaoCreateDto;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Locacao;
import br.edu.ifc.bikes.exception.BicicletaIndisponivelException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.LocacaoFinalizadaException;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.repository.LocacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LocacaoService {

    private final LocacaoRepository locacaoRepository;
    private final ClienteService clienteService;
    private final BicicletaService bicicletaService;

    //valor cobrado por hora iniciada (configurável no application.properties)
    @Value("${bikes.locacao.valor-hora:10.00}")
    private BigDecimal valorHora;

    //requisito 6: o cliente solicita e o sistema escolhe a primeira bicicleta livre
    @Transactional
    public Locacao solicitar(Long usuarioId, LocacaoCreateDto dto) {
        Cliente cliente = clienteService.buscarPorUsuarioId(usuarioId);

        //a busca trava a bicicleta (lock pessimista) até o fim da transação
        Bicicleta bicicleta = bicicletaService.buscarPrimeiraLivre();
        if (locacaoRepository.existsByBicicletaIdAndStatus(bicicleta.getId(), Locacao.StatusLocacao.ATIVA)) {
            throw new BicicletaIndisponivelException(
                    String.format("A bicicleta '%s' já está em uma locação ativa", bicicleta.getCodigo()));
        }
        bicicleta.setStatus(Bicicleta.StatusBicicleta.ALUGADA);

        Locacao locacao = new Locacao();
        locacao.setCliente(cliente);
        locacao.setBicicleta(bicicleta);
        locacao.setDataRetirada(LocalDateTime.now());
        locacao.setDataPrevistaDevolucao(dto.dataPrevistaDevolucao());
        locacao.setStatus(Locacao.StatusLocacao.ATIVA);
        locacao.setCartaoFinal(dto.cartao().ultimosDigitos());
        return locacaoRepository.save(locacao);
    }

    //o admin vê qualquer locação; o cliente, apenas as suas
    @Transactional
    public Locacao buscarPorId(Long id, JwtUserDetails usuario) {
        Locacao locacao = locacaoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Locação com id=%d não encontrada", id))
        );
        boolean admin = "ADMIN".equals(usuario.getRole());
        boolean dono = locacao.getCliente().getUsuario().getId().equals(usuario.getId());
        if (!admin && !dono) {
            throw new AccessDeniedException("A locação pertence a outro cliente");
        }
        return locacao;
    }

    @Transactional
    public List<Locacao> buscarPorUsuario(Long usuarioId) {
        return locacaoRepository.findAllByUsuarioId(usuarioId);
    }

    //requisito 7: registra a devolução, calcula o valor e libera (ou envia para manutenção) a bicicleta
    @Transactional
    public Locacao devolver(Long id, JwtUserDetails usuario, boolean manutencao) {
        Locacao locacao = buscarPorId(id, usuario);
        if (locacao.getStatus() == Locacao.StatusLocacao.FINALIZADA) {
            throw new LocacaoFinalizadaException(
                    String.format("A locação com id=%d já foi finalizada em %s", id,
                            locacao.getDataDevolucao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }

        LocalDateTime agora = LocalDateTime.now();
        locacao.setDataDevolucao(agora);
        locacao.setValor(calcularValor(locacao.getDataRetirada(), agora));
        locacao.setStatus(Locacao.StatusLocacao.FINALIZADA);
        locacao.getBicicleta().setStatus(
                manutencao ? Bicicleta.StatusBicicleta.MANUTENCAO : Bicicleta.StatusBicicleta.LIVRE);
        return locacao;
    }

    //requisito 8: todas as locações, das mais recentes para as mais antigas
    @Transactional
    public List<Locacao> buscarTodas() {
        return locacaoRepository.findAllComClienteEBicicleta();
    }

    //cobrança por hora iniciada, com mínimo de 1 hora (ex.: 1h01 de uso = 2 horas)
    BigDecimal calcularValor(LocalDateTime retirada, LocalDateTime devolucao) {
        long segundos = Duration.between(retirada, devolucao).getSeconds();
        long horas = Math.max(1, (segundos + 3599) / 3600);
        return valorHora.multiply(BigDecimal.valueOf(horas)).setScale(2, RoundingMode.HALF_UP);
    }
}
