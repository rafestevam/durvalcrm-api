package br.org.cecairbar.durvalcrm.application.dto;

import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumoVendasDTO {
    
    private Instant dataInicio;
    private Instant dataFim;
    
    private long totalVendas;
    private BigDecimal valorTotalVendas;
    private BigDecimal valorMedioVenda;
    
    // Vendas por origem
    private long vendasCantina;
    private long vendasBazar;
    private long vendasLivros;
    
    // Valores por origem
    private BigDecimal valorCantina;
    private BigDecimal valorBazar;
    private BigDecimal valorLivros;
    
    // Percentuais por origem
    private BigDecimal percentualCantina;
    private BigDecimal percentualBazar;
    private BigDecimal percentualLivros;
    
    // Associado que mais vendeu
    private String associadoMaisVendas;
    private long vendasAssociadoTop;
    
    // Factory method para criar resumo
    public static ResumoVendasDTO criar(Instant dataInicio, Instant dataFim, 
                                        Map<OrigemVenda, Long> vendasPorOrigem,
                                        Map<OrigemVenda, BigDecimal> valoresPorOrigem,
                                        long totalVendas, BigDecimal valorTotal) {
        
        ResumoVendasDTO resumo = ResumoVendasDTO.builder()
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .totalVendas(totalVendas)
                .valorTotalVendas(valorTotal)
                .build();
        
        // Calcular valor médio
        if (totalVendas > 0) {
            resumo.setValorMedioVenda(valorTotal.divide(BigDecimal.valueOf(totalVendas), 2, RoundingMode.HALF_UP));
        } else {
            resumo.setValorMedioVenda(BigDecimal.ZERO);
        }
        
        // Definir vendas por origem
        resumo.setVendasCantina(vendasPorOrigem.getOrDefault(OrigemVenda.CANTINA, 0L));
        resumo.setVendasBazar(vendasPorOrigem.getOrDefault(OrigemVenda.BAZAR, 0L));
        resumo.setVendasLivros(vendasPorOrigem.getOrDefault(OrigemVenda.LIVROS, 0L));
        
        // Definir valores por origem
        resumo.setValorCantina(valoresPorOrigem.getOrDefault(OrigemVenda.CANTINA, BigDecimal.ZERO));
        resumo.setValorBazar(valoresPorOrigem.getOrDefault(OrigemVenda.BAZAR, BigDecimal.ZERO));
        resumo.setValorLivros(valoresPorOrigem.getOrDefault(OrigemVenda.LIVROS, BigDecimal.ZERO));
        
        // Calcular percentuais
        if (valorTotal.compareTo(BigDecimal.ZERO) > 0) {
            resumo.setPercentualCantina(resumo.getValorCantina().multiply(BigDecimal.valueOf(100)).divide(valorTotal, 2, RoundingMode.HALF_UP));
            resumo.setPercentualBazar(resumo.getValorBazar().multiply(BigDecimal.valueOf(100)).divide(valorTotal, 2, RoundingMode.HALF_UP));
            resumo.setPercentualLivros(resumo.getValorLivros().multiply(BigDecimal.valueOf(100)).divide(valorTotal, 2, RoundingMode.HALF_UP));
        } else {
            resumo.setPercentualCantina(BigDecimal.ZERO);
            resumo.setPercentualBazar(BigDecimal.ZERO);
            resumo.setPercentualLivros(BigDecimal.ZERO);
        }
        
        return resumo;
    }
}