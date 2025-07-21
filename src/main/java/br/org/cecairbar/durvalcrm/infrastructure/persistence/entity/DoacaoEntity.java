package br.org.cecairbar.durvalcrm.infrastructure.persistence.entity;

import br.org.cecairbar.durvalcrm.domain.model.MetodoPagamento;
import br.org.cecairbar.durvalcrm.domain.model.StatusDoacao;
import br.org.cecairbar.durvalcrm.domain.model.TipoDoacao;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "doacoes")
public class DoacaoEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "UUID")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associado_id", nullable = true)
    public AssociadoEntity associado;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    public BigDecimal valor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TipoDoacao tipo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StatusDoacao status;

    @Column(length = 500)
    public String descricao;

    @NotNull
    @Column(name = "data_doacao", nullable = false)
    public LocalDateTime dataDoacao;

    @Column(name = "data_confirmacao")
    public LocalDateTime dataConfirmacao;

    @Column(name = "codigo_transacao")
    public String codigoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento")
    public MetodoPagamento metodoPagamento;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dataDoacao == null) {
            dataDoacao = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusDoacao.PENDENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}