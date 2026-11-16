package br.edu.ifc.bikes.repository;

import br.edu.ifc.bikes.entity.Locacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {

    //garante que uma bicicleta não participe de duas locações ativas ao mesmo tempo
    boolean existsByBicicletaIdAndStatus(Long bicicletaId, Locacao.StatusLocacao status);

    //join fetch: cliente e bicicleta vêm na mesma consulta (evita uma consulta extra por locação)
    @Query("SELECT l FROM Locacao l JOIN FETCH l.cliente c JOIN FETCH l.bicicleta " +
            "WHERE c.usuario.id = :usuarioId ORDER BY l.dataRetirada DESC")
    List<Locacao> findAllByUsuarioId(Long usuarioId);

    @Query("SELECT l FROM Locacao l JOIN FETCH l.cliente JOIN FETCH l.bicicleta ORDER BY l.dataRetirada DESC")
    List<Locacao> findAllComClienteEBicicleta();
}
