package br.org.cecairbar.durvalcrm.application.dto;

import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import br.org.cecairbar.durvalcrm.domain.model.FormaPagamento;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaDTO {
    
    private UUID id;
    
    @NotEmpty(message = "Descrição é obrigatória")
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;
    
    @NotNull(message = "Origem é obrigatória")
    private OrigemVenda origem;
    
    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;
    
    private Instant dataVenda;
    
    private Instant criadoEm;
    private Instant atualizadoEm;
    
    // Factory method para criar nova venda
    public static VendaDTO criar(String descricao, BigDecimal valor, OrigemVenda origem, FormaPagamento formaPagamento) {
        return VendaDTO.builder()
                .descricao(descricao)
                .valor(valor)
                .origem(origem)
                .formaPagamento(formaPagamento)
                .dataVenda(Instant.now())
                .build();
    }
}