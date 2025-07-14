package br.org.cecairbar.durvalcrm.application.dto;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class MensalidadeDTO {
    public UUID id;
    public UUID associadoId;
    public String nomeAssociado;
    public int mesReferencia;
    public int anoReferencia;
    public BigDecimal valor;
    public StatusMensalidade status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate dataVencimento;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public Instant dataPagamento;
    
    public String qrCodePix;
    public String identificadorPix;
    public boolean vencida;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public Instant criadoEm;
    
    // Campos calculados úteis para o frontend
    public String chaveReferencia; // Ex: "2025-07"
    public String descricaoStatus;
    public int diasVencimento; // Negativo = atrasado, positivo = dias para vencer

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
        dto.criadoEm = mensalidade.getCriadoEm();
        
        // Campos calculados
        dto.vencida = mensalidade.isVencida();
        dto.chaveReferencia = mensalidade.getChaveReferencia();
        dto.descricaoStatus = getDescricaoStatus(mensalidade.getStatus());
        dto.diasVencimento = calcularDiasVencimento(mensalidade.getDataVencimento());
        
        return dto;
    }

    public static MensalidadeDTO fromDomainWithNomeAssociado(Mensalidade mensalidade, String nomeAssociado) {
        MensalidadeDTO dto = fromDomain(mensalidade);
        dto.nomeAssociado = nomeAssociado;
        return dto;
    }

    private static String getDescricaoStatus(StatusMensalidade status) {
        return switch (status) {
            case PENDENTE -> "Pendente";
            case PAGA -> "Paga";
            case ATRASADA -> "Atrasada";
        };
    }

    private static int calcularDiasVencimento(LocalDate dataVencimento) {
        return (int) LocalDate.now().until(dataVencimento).getDays();
    }

    // Método para facilitar ordenação no frontend
    public String getStatusOrdem() {
        return switch (status) {
            case ATRASADA -> "1_ATRASADA";
            case PENDENTE -> "2_PENDENTE";
            case PAGA -> "3_PAGA";
        };
    }

    // Método para facilitar exibição de período no frontend
    public String getPeriodoFormatado() {
        String[] meses = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return meses[mesReferencia - 1] + "/" + anoReferencia;
    }
}