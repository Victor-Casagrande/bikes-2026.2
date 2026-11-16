package br.edu.ifc.bikes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

//===lombok
@Getter
@Setter
//===lombok

@Entity
@Table(name = "bicicletas")
@EntityListeners(AuditingEntityListener.class)
public class Bicicleta implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    //a constante definida no enum passa a ser uma string no bd
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusBicicleta status = StatusBicicleta.LIVRE;

    @CreatedDate
    private LocalDateTime dataCriacao;
    @LastModifiedDate
    private LocalDateTime dataModificacao;
    @CreatedBy
    private String criadoPor;
    @LastModifiedBy
    private String modificadoPor;

    public enum StatusBicicleta {
        LIVRE, ALUGADA
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Bicicleta bicicleta)) return false;
        return Objects.equals(id, bicicleta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
