package br.org.cecairbar.durvalcrm.application.dto;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MensalidadeDTO {
    public UUID id;
    public UUID associadoId;
    public String nomeAssociado;
    public int mesReferencia;
    public int anoReferencia;
    public BigDecimal valor;
    public StatusMensalidade status;
    public LocalDate dataVencimento;
    public LocalDateTime dataPagamento;
    public String qrCodePix;
    public String identificadorPix;
    public boolean vencida;

    public static MensalidadeDTO fromDomain(Mensalidade mensalidade) {
        MensalidadeDTO dto = new MensalidadeDTO();
        dto.id = mensalidade.getId();
        dto.associadoId = mensalidade.getAssociadoId();
        dto.mesReferencia = mensalidade.getMesReferencia();
        dto.anoReferencia = mensalidade.getAnoReferencia();
        dto.valor = mensalidade.getValor();
        dto.status = mensalidade.getStatus();
        dto.dataVencimento = mensalidade.getDataVencimento();
        dto.dataPagamento = mensalidade.getDataPagamento();
        dto.qrCodePix = mensalidade.getQrCodePix();
        dto.identificadorPix = mensalidade.getIdentificadorPix();
        dto.vencida = mensalidade.isVencida();
        return dto;
    }
}