package br.org.cecairbar.durvalcrm.domain.model;

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
public class Venda {
    
    @NotNull
    private UUID id;
    
    @NotEmpty
    @Size(max = 255)
    private String descricao;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;
    
    @NotNull
    private OrigemVenda origem;
    
    @NotNull
    private Instant dataVenda;
    
    @NotNull
    private Instant criadoEm;
    
    private Instant atualizadoEm;
    
    // Factory method para criar nova venda
    public static Venda criar(String descricao, BigDecimal valor, OrigemVenda origem) {
        return Venda.builder()
                .id(UUID.randomUUID())
                .descricao(descricao)
                .valor(valor)
                .origem(origem)
                .dataVenda(Instant.now())
                .criadoEm(Instant.now())
                .build();
    }
    
    // Método para atualizar dados da venda
    public void atualizar(String descricao, BigDecimal valor, OrigemVenda origem) {
        if (descricao != null && !descricao.trim().isEmpty()) {
            this.descricao = descricao;
        }
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            this.valor = valor;
        }
        if (origem != null) {
            this.origem = origem;
        }
        this.atualizadoEm = Instant.now();
    }
    
    // Método para validar se a venda está válida
    public boolean isValida() {
        return id != null && 
               descricao != null && !descricao.trim().isEmpty() &&
               valor != null && valor.compareTo(BigDecimal.ZERO) > 0 &&
               origem != null &&
               dataVenda != null &&
               criadoEm != null;
    }
}
