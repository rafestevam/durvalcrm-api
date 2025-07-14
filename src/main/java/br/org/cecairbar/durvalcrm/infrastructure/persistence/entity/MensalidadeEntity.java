package br.org.cecairbar.durvalcrm.infrastructure.persistence.entity;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mensalidades", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"associado_id", "mes_referencia", "ano_referencia"}))
public class MensalidadeEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "associado_id", nullable = false)
    public UUID associadoId;

    @Column(name = "mes_referencia", nullable = false)
    public Integer mesReferencia;

    @Column(name = "ano_referencia", nullable = false)
    public Integer anoReferencia;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    public BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public StatusMensalidade status;

    @Column(name = "data_vencimento", nullable = false)
    public LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    public LocalDateTime dataPagamento;

    @Column(name = "qr_code_pix", length = 1000)
    public String qrCodePix;

    @Column(name = "identificador_pix", nullable = false, unique = true)
    public String identificadorPix;

    @Column(name = "criado_em", nullable = false)
    public LocalDateTime criadoEm;

    public static MensalidadeEntity fromDomain(Mensalidade mensalidade) {
        MensalidadeEntity entity = new MensalidadeEntity();
        entity.id = mensalidade.getId();
        entity.associadoId = mensalidade.getAssociadoId();
        entity.mesReferencia = mensalidade.getMesReferencia();
        entity.anoReferencia = mensalidade.getAnoReferencia();
        entity.valor = mensalidade.getValor();
        entity.status = mensalidade.getStatus();
        entity.dataVencimento = mensalidade.getDataVencimento();
        entity.dataPagamento = mensalidade.getDataPagamento();
        entity.qrCodePix = mensalidade.getQrCodePix();
        entity.identificadorPix = mensalidade.getIdentificadorPix();
        entity.criadoEm = mensalidade.getCriadoEm();
        return entity;
    }

    public Mensalidade toDomain() {
        // Usar reflection ou método factory para reconstruir o objeto de domínio
        // Implementação simplificada aqui
        Mensalidade mensalidade = Mensalidade.criar(
            this.associadoId, 
            this.mesReferencia, 
            this.anoReferencia, 
            this.valor
        );
        mensalidade.setId(this.id);
        mensalidade.setQrCodePix(this.qrCodePix);
        if (this.dataPagamento != null) {
            mensalidade.marcarComoPaga(this.dataPagamento);
        }
        return mensalidade;
    }
}