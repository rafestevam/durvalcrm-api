package br.org.cecairbar.durvalcrm.application.doacao;

import java.math.BigDecimal;

public class DoacaoEstatisticasDTO {
    private BigDecimal totalArrecadado;
    private Long totalDoacoes;
    private Long doacoesConfirmadas;
    private Long doacoesPendentes;
    private Long doacoesCanceladas;
    private BigDecimal ticketMedio;

    public BigDecimal getTotalArrecadado() {
        return totalArrecadado;
    }

    public void setTotalArrecadado(BigDecimal totalArrecadado) {
        this.totalArrecadado = totalArrecadado;
    }

    public Long getTotalDoacoes() {
        return totalDoacoes;
    }

    public void setTotalDoacoes(Long totalDoacoes) {
        this.totalDoacoes = totalDoacoes;
    }

    public Long getDoacoesConfirmadas() {
        return doacoesConfirmadas;
    }

    public void setDoacoesConfirmadas(Long doacoesConfirmadas) {
        this.doacoesConfirmadas = doacoesConfirmadas;
    }

    public Long getDoacoesPendentes() {
        return doacoesPendentes;
    }

    public void setDoacoesPendentes(Long doacoesPendentes) {
        this.doacoesPendentes = doacoesPendentes;
    }

    public Long getDoacoesCanceladas() {
        return doacoesCanceladas;
    }

    public void setDoacoesCanceladas(Long doacoesCanceladas) {
        this.doacoesCanceladas = doacoesCanceladas;
    }

    public BigDecimal getTicketMedio() {
        return ticketMedio;
    }

    public void setTicketMedio(BigDecimal ticketMedio) {
        this.ticketMedio = ticketMedio;
    }
}