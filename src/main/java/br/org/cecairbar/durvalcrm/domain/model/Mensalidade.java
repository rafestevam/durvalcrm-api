package br.org.cecairbar.durvalcrm.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Mensalidade {
    private UUID id;
    private UUID associadoId;
    private int mesReferencia;
    private int anoReferencia;
    private BigDecimal valor;
    private StatusMensalidade status;
    private LocalDate dataVencimento;
    private Instant dataPagamento;
    private String qrCodePix;
    private String identificadorPix;
    private Instant criadoEm;

    // Construtor privado para controle da criação
    private Mensalidade() {}

    // Factory method para criar nova mensalidade
    public static Mensalidade criar(UUID associadoId, int mes, int ano, BigDecimal valor) {
        Mensalidade mensalidade = new Mensalidade();
        mensalidade.id = UUID.randomUUID();
        mensalidade.associadoId = associadoId;
        mensalidade.mesReferencia = mes;
        mensalidade.anoReferencia = ano;
        mensalidade.valor = valor;
        mensalidade.status = StatusMensalidade.PENDENTE;
        mensalidade.dataVencimento = calcularDataVencimento(mes, ano);
        mensalidade.identificadorPix = gerarIdentificadorPix(associadoId, mes, ano);
        mensalidade.criadoEm = Instant.now();
        return mensalidade;
    }

    private static LocalDate calcularDataVencimento(int mes, int ano) {
        // Vencimento dia 10 do mês de referência
        return LocalDate.of(ano, mes, 10);
    }

    private static String gerarIdentificadorPix(UUID associadoId, int mes, int ano) {
        String shortId = associadoId.toString().substring(0, 8);
        return String.format("MENS%s%02d%d", shortId, mes, ano);
    }

    public void marcarComoPaga(Instant dataPagamento) {
        if (this.status == StatusMensalidade.PAGA) {
            throw new IllegalStateException("Mensalidade já está paga");
        }
        this.status = StatusMensalidade.PAGA;
        this.dataPagamento = dataPagamento;
    }

    public void atualizarStatus() {
        if (this.status == StatusMensalidade.PENDENTE && 
            LocalDate.now().isAfter(this.dataVencimento)) {
            this.status = StatusMensalidade.ATRASADA;
        }
    }

    public boolean isVencida() {
        return LocalDate.now().isAfter(this.dataVencimento);
    }

    public String getChaveReferencia() {
        return String.format("%d-%02d", anoReferencia, mesReferencia);
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getAssociadoId() { return associadoId; }
    public int getMesReferencia() { return mesReferencia; }
    public int getAnoReferencia() { return anoReferencia; }
    public BigDecimal getValor() { return valor; }
    public StatusMensalidade getStatus() { return status; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public Instant getDataPagamento() { return dataPagamento; }
    public String getQrCodePix() { return qrCodePix; }
    public String getIdentificadorPix() { return identificadorPix; }
    public Instant getCriadoEm() { return criadoEm; }

    // Setters (para uso interno e reconstrução)
    public void setId(UUID id) { this.id = id; }
    public void setQrCodePix(String qrCodePix) { this.qrCodePix = qrCodePix; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
    protected void setStatus(StatusMensalidade status) { this.status = status; }
}