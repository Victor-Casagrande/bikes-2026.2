package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.exception.BicicletaIndisponivelException;
import br.edu.ifc.bikes.exception.CodigoUniqueViolationException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;

    @Transactional
    public Bicicleta salvar(Bicicleta bicicleta) {
        try {
            //saveAndFlush: a violação do código único acontece aqui, e não só no commit
            return bicicletaRepository.saveAndFlush(bicicleta);
        } catch (DataIntegrityViolationException ex) {
            throw new CodigoUniqueViolationException(
                    String.format("A bicicleta com código '%s' já está cadastrada", bicicleta.getCodigo()));
        }
    }

    @Transactional
    public Bicicleta buscarPorCodigo(String codigo) {
        return bicicletaRepository.findByCodigo(codigo).orElseThrow(
                () -> new EntityNotFoundException(String.format("Bicicleta com código '%s' não encontrada", codigo))
        );
    }

    @Transactional
    public List<Bicicleta> buscarTodas() {
        return bicicletaRepository.findAll();
    }

    //o admin só alterna entre LIVRE e MANUTENCAO; uma bicicleta alugada só muda pela devolução
    @Transactional
    public Bicicleta alterarStatus(String codigo, Bicicleta.StatusBicicleta novoStatus) {
        Bicicleta bicicleta = buscarPorCodigo(codigo);
        if (bicicleta.getStatus() == Bicicleta.StatusBicicleta.ALUGADA) {
            throw new BicicletaIndisponivelException(
                    String.format("A bicicleta '%s' está alugada; o status muda ao registrar a devolução", codigo));
        }
        bicicleta.setStatus(novoStatus);
        return bicicleta;
    }

    //usado pela locação: o sistema escolhe a primeira bicicleta livre
    @Transactional
    public Bicicleta buscarPrimeiraLivre() {
        return bicicletaRepository.findFirstByStatusOrderByIdAsc(Bicicleta.StatusBicicleta.LIVRE).orElseThrow(
                () -> new BicicletaIndisponivelException("Não há bicicletas disponíveis para locação no momento")
        );
    }
}
