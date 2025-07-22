package br.org.cecairbar.durvalcrm.infrastructure.persistence.entity;

import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import br.org.cecairbar.durvalcrm.domain.model.FormaPagamento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;
    
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false)
    private OrigemVenda origem;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private FormaPagamento formaPagamento;
    
    @Column(name = "data_venda", nullable = false)
    private Instant dataVenda;
    
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
    
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
        if (dataVenda == null) {
            dataVenda = Instant.now();
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        atualizadoEm = Instant.now();
    }
}