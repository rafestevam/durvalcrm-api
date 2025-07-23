package br.org.cecairbar.durvalcrm.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceitasPorMetodoPagamentoDTO {
    
    private BigDecimal totalPix;
    private BigDecimal totalDinheiro;
    private BigDecimal totalGeral;
    
}