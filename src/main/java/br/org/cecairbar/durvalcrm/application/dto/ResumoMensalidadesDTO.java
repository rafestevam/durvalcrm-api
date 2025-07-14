package br.org.cecairbar.durvalcrm.application.dto;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para resumo das mensalidades de um período
 */
public class ResumoMensalidadesDTO {
    
    @JsonProperty("totalAssociados")
    private int totalAssociados;
    
    @JsonProperty("totalPendentes")
    private int totalPendentes;
    
    @JsonProperty("totalPagas")
    private int totalPagas;
    
    @JsonProperty("totalAtrasadas")
    private int totalAtrasadas;
    
    @JsonProperty("valorTotalEsperado")
    private BigDecimal valorTotalEsperado;
    
    @JsonProperty("valorArrecadado")
    private BigDecimal valorArrecadado;
    
    @JsonProperty("valorPendente")
    private BigDecimal valorPendente;
    
    @JsonProperty("valorAtrasado")
    private BigDecimal valorAtrasado;
    
    @JsonProperty("percentualArrecadacao")
    private double percentualArrecadacao;
    
    @JsonProperty("mes")
    private int mes;
    
    @JsonProperty("ano")
    private int ano;

    // Construtor padrão
    public ResumoMensalidadesDTO() {}

    // Construtor completo
    public ResumoMensalidadesDTO(int totalAssociados, int totalPendentes, int totalPagas, 
                                int totalAtrasadas, BigDecimal valorTotalEsperado, 
                                BigDecimal valorArrecadado, BigDecimal valorPendente,
                                BigDecimal valorAtrasado, double percentualArrecadacao,
                                int mes, int ano) {
        this.totalAssociados = totalAssociados;
        this.totalPendentes = totalPendentes;
        this.totalPagas = totalPagas;
        this.totalAtrasadas = totalAtrasadas;
        this.valorTotalEsperado = valorTotalEsperado;
        this.valorArrecadado = valorArrecadado;
        this.valorPendente = valorPendente;
        this.valorAtrasado = valorAtrasado;
        this.percentualArrecadacao = percentualArrecadacao;
        this.mes = mes;
        this.ano = ano;
    }

    /**
     * Método estático para criar ResumoMensalidadesDTO a partir de uma lista de mensalidades
     */
    public static ResumoMensalidadesDTO criarDoList(List<Mensalidade> mensalidades) {
        if (mensalidades == null || mensalidades.isEmpty()) {
            return new ResumoMensalidadesDTO(0, 0, 0, 0, 
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
                0.0, 0, 0);
        }

        // Obter mês e ano da primeira mensalidade
        Mensalidade primeira = mensalidades.get(0);
        int mes = primeira.getMesReferencia();
        int ano = primeira.getAnoReferencia();

        // Contadores
        int totalAssociados = mensalidades.size();
        int totalPendentes = 0;
        int totalPagas = 0;
        int totalAtrasadas = 0;

        // Valores
        BigDecimal valorTotalEsperado = BigDecimal.ZERO;
        BigDecimal valorArrecadado = BigDecimal.ZERO;
        BigDecimal valorPendente = BigDecimal.ZERO;
        BigDecimal valorAtrasado = BigDecimal.ZERO;

        // Processar cada mensalidade
        for (Mensalidade mensalidade : mensalidades) {
            valorTotalEsperado = valorTotalEsperado.add(mensalidade.getValor());
            
            switch (mensalidade.getStatus()) {
                case PENDENTE:
                    totalPendentes++;
                    valorPendente = valorPendente.add(mensalidade.getValor());
                    break;
                case PAGA:
                    totalPagas++;
                    valorArrecadado = valorArrecadado.add(mensalidade.getValor());
                    break;
                case ATRASADA:
                    totalAtrasadas++;
                    valorAtrasado = valorAtrasado.add(mensalidade.getValor());
                    break;
            }
        }

        // Calcular percentual de arrecadação
        double percentualArrecadacao = 0.0;
        if (valorTotalEsperado.compareTo(BigDecimal.ZERO) > 0) {
            percentualArrecadacao = valorArrecadado
                .divide(valorTotalEsperado, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }

        return new ResumoMensalidadesDTO(
            totalAssociados, totalPendentes, totalPagas, totalAtrasadas,
            valorTotalEsperado, valorArrecadado, valorPendente, valorAtrasado,
            percentualArrecadacao, mes, ano
        );
    }

    /**
     * Método para criar um resumo vazio para período sem mensalidades
     */
    public static ResumoMensalidadesDTO vazio(int mes, int ano) {
        return new ResumoMensalidadesDTO(0, 0, 0, 0, 
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
            0.0, mes, ano);
    }

    /**
     * Método estático para criar ResumoMensalidadesDTO com total de associados específico
     */
    public static ResumoMensalidadesDTO criarDoListComTotalAssociados(
        List<Mensalidade> mensalidades, 
        int totalAssociadosAtivos) {
        
        if (mensalidades == null || mensalidades.isEmpty()) {
            return new ResumoMensalidadesDTO(
                totalAssociadosAtivos, 0, 0, 0, 
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
                0.0, 0, 0
            );
        }

        // Obter mês e ano da primeira mensalidade
        Mensalidade primeira = mensalidades.get(0);
        int mes = primeira.getMesReferencia();
        int ano = primeira.getAnoReferencia();

        // Contadores (mantém mensalidades.size() para mensalidades geradas)
        int totalPendentes = 0;
        int totalPagas = 0;
        int totalAtrasadas = 0;

        // Valores
        BigDecimal valorTotalEsperado = BigDecimal.valueOf(totalAssociadosAtivos).multiply(new BigDecimal("10.90"));
        BigDecimal valorArrecadado = BigDecimal.ZERO;
        BigDecimal valorPendente = BigDecimal.ZERO;
        BigDecimal valorAtrasado = BigDecimal.ZERO;

        // Processar cada mensalidade
        for (Mensalidade mensalidade : mensalidades) {
            switch (mensalidade.getStatus()) {
                case PENDENTE:
                    totalPendentes++;
                    valorPendente = valorPendente.add(mensalidade.getValor());
                    break;
                case PAGA:
                    totalPagas++;
                    valorArrecadado = valorArrecadado.add(mensalidade.getValor());
                    break;
                case ATRASADA:
                    totalAtrasadas++;
                    valorAtrasado = valorAtrasado.add(mensalidade.getValor());
                    break;
            }
        }

        // Calcular pendentes para associados sem mensalidades geradas
        int associadosSemMensalidade = totalAssociadosAtivos - mensalidades.size();
        if (associadosSemMensalidade > 0) {
            totalPendentes += associadosSemMensalidade;
            valorPendente = valorPendente.add(
                BigDecimal.valueOf(associadosSemMensalidade).multiply(new BigDecimal("10.90"))
            );
        }

        // Calcular percentual de arrecadação
        double percentualArrecadacao = 0.0;
        if (valorTotalEsperado.compareTo(BigDecimal.ZERO) > 0) {
            percentualArrecadacao = valorArrecadado
                .divide(valorTotalEsperado, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }

        return new ResumoMensalidadesDTO(
            totalAssociadosAtivos, totalPendentes, totalPagas, totalAtrasadas,
            valorTotalEsperado, valorArrecadado, valorPendente, valorAtrasado,
            percentualArrecadacao, mes, ano
        );
    }

    // Getters e Setters
    public int getTotalAssociados() { return totalAssociados; }
    public void setTotalAssociados(int totalAssociados) { this.totalAssociados = totalAssociados; }

    public int getTotalPendentes() { return totalPendentes; }
    public void setTotalPendentes(int totalPendentes) { this.totalPendentes = totalPendentes; }

    public int getTotalPagas() { return totalPagas; }
    public void setTotalPagas(int totalPagas) { this.totalPagas = totalPagas; }

    public int getTotalAtrasadas() { return totalAtrasadas; }
    public void setTotalAtrasadas(int totalAtrasadas) { this.totalAtrasadas = totalAtrasadas; }

    public BigDecimal getValorTotalEsperado() { return valorTotalEsperado; }
    public void setValorTotalEsperado(BigDecimal valorTotalEsperado) { this.valorTotalEsperado = valorTotalEsperado; }

    public BigDecimal getValorArrecadado() { return valorArrecadado; }
    public void setValorArrecadado(BigDecimal valorArrecadado) { this.valorArrecadado = valorArrecadado; }

    public BigDecimal getValorPendente() { return valorPendente; }
    public void setValorPendente(BigDecimal valorPendente) { this.valorPendente = valorPendente; }

    public BigDecimal getValorAtrasado() { return valorAtrasado; }
    public void setValorAtrasado(BigDecimal valorAtrasado) { this.valorAtrasado = valorAtrasado; }

    public double getPercentualArrecadacao() { return percentualArrecadacao; }
    public void setPercentualArrecadacao(double percentualArrecadacao) { this.percentualArrecadacao = percentualArrecadacao; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    @Override
    public String toString() {
        return String.format(
            "ResumoMensalidadesDTO{mes=%d, ano=%d, totalAssociados=%d, " +
            "totalPendentes=%d, totalPagas=%d, totalAtrasadas=%d, " +
            "valorTotalEsperado=%s, valorArrecadado=%s, percentualArrecadacao=%.2f%%}",
            mes, ano, totalAssociados, totalPendentes, totalPagas, totalAtrasadas,
            valorTotalEsperado, valorArrecadado, percentualArrecadacao
        );
    }
}