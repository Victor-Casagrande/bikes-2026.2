package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.exception.CpfUniqueViolationException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente salvar(Cliente cliente) {
        if (clienteRepository.existsByUsuarioId(cliente.getUsuario().getId())) {
            throw new UsernameUniqueViolationException(
                    String.format("O usuário '%s' já possui um cliente cadastrado", cliente.getUsuario().getUsername()));
        }
        try {
            //saveAndFlush: a violação do CPF único acontece aqui, e não só no commit
            return clienteRepository.saveAndFlush(cliente);
        } catch (DataIntegrityViolationException ex) {
            throw new CpfUniqueViolationException(String.format("O CPF '%s' já está cadastrado", cliente.getCpf()));
        }
    }

    @Transactional
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cliente com id=%d não encontrado", id))
        );
    }

    @Transactional
    public Cliente buscarPorUsuarioId(Long usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId).orElseThrow(
                () -> new EntityNotFoundException("O usuário autenticado ainda não possui um cliente cadastrado")
        );
    }

    @Transactional
    public List<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }
}
