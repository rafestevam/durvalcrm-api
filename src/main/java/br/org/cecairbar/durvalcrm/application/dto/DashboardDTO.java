package br.org.cecairbar.durvalcrm.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    
    private BigDecimal receitaConsolidada;
    private BigDecimal receitaMensalidades;
    private BigDecimal receitaCantina;
    private BigDecimal receitaBazar;
    private BigDecimal receitaLivros;
    private BigDecimal receitaDoacoes;
    private Long pagantesMes;
    private Long totalAssociados;
    private List<AssociadoResumoDTO> adimplentes;
    private List<AssociadoResumoDTO> inadimplentes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssociadoResumoDTO {
        private String id;
        private String nomeCompleto;
        private String email;
        private String cpf;
    }
}