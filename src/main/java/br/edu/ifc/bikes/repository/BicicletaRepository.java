package br.edu.ifc.bikes.repository;

import br.edu.ifc.bikes.entity.Bicicleta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    Optional<Bicicleta> findByCodigo(String codigo);

    //PESSIMISTIC_WRITE (SELECT ... FOR UPDATE): duas solicitações ao mesmo tempo
    //não conseguem pegar a mesma bicicleta livre
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Bicicleta> findFirstByStatusOrderByIdAsc(Bicicleta.StatusBicicleta status);
}
