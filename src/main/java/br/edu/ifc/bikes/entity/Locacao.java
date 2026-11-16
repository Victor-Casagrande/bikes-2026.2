package br.edu.ifc.bikes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

//===lombok
@Getter
@Setter
//===lombok

@Entity
@Table(name = "locacoes")
@EntityListeners(AuditingEntityListener.class)
public class Locacao implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //um cliente pode ter várias locações (1:N)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    //uma bicicleta participa de várias locações ao longo do tempo, mas só de uma ATIVA por vez (N:1)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_bicicleta", nullable = false)
    private Bicicleta bicicleta;

    @Column(name = "data_retirada", nullable = false)
    private LocalDateTime dataRetirada;

    @Column(name = "data_prevista_devolucao", nullable = false)
    private LocalDateTime dataPrevistaDevolucao;

    //preenchida só ao registrar a devolução
    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;

    //valor a ser debitado no cartão, calculado na devolução
    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusLocacao status = StatusLocacao.ATIVA;

    //por segurança, do cartão de crédito guardamos apenas os 4 últimos dígitos
    @Column(name = "cartao_final", nullable = false, length = 4)
    private String cartaoFinal;

    @CreatedDate
    private LocalDateTime dataCriacao;
    @LastModifiedDate
    private LocalDateTime dataModificacao;
    @CreatedBy
    private String criadoPor;
    @LastModifiedBy
    private String modificadoPor;

    public enum StatusLocacao {
        ATIVA, FINALIZADA
    }

    //duração da locação em minutos: até a devolução ou, se ainda ativa, até o momento
    //(não é persistida: o acesso do JPA é pelos campos, e este método não tem campo)
    public Long getDuracaoMinutos() {
        if (dataRetirada == null) return null;
        LocalDateTime fim = dataDevolucao != null ? dataDevolucao : LocalDateTime.now();
        return Duration.between(dataRetirada, fim).toMinutes();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Locacao locacao)) return false;
        return Objects.equals(id, locacao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
