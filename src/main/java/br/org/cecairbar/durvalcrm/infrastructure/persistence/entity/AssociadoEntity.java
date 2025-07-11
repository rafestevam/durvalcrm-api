package br.org.cecairbar.durvalcrm.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "associados")
@Data
@EqualsAndHashCode(callSuper=false)
public class AssociadoEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "UUID")
    public UUID id;

    @Column(name = "nome_completo", nullable = false)
    public String nomeCompleto;

    @Column(unique = true, nullable = false, length = 14)
    public String cpf;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(length = 20)
    public String telefone;

    @Column(nullable = false)
    public boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    public Instant criadoEm;
}