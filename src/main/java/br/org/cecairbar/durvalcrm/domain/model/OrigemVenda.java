package br.org.cecairbar.durvalcrm.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representando as possíveis origens de venda
 */
@Getter
@RequiredArgsConstructor
public enum OrigemVenda {
    
    CANTINA("Cantina", "Vendas realizadas na cantina"),
    BAZAR("Bazar", "Vendas realizadas no bazar"),
    LIVROS("Livros", "Vendas de livros");
    
    private final String descricao;
    private final String detalhe;
    
    /**
     * Busca origem pela descrição
     * @param descricao Descrição da origem
     * @return OrigemVenda encontrada ou null
     */
    public static OrigemVenda fromDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return null;
        }
        
        for (OrigemVenda origem : values()) {
            if (origem.getDescricao().equalsIgnoreCase(descricao.trim())) {
                return origem;
            }
        }
        return null;
    }
    
    /**
     * Verifica se a origem é válida
     * @param origem String a ser validada
     * @return true se válida, false caso contrário
     */
    public static boolean isOrigemValida(String origem) {
        return fromDescricao(origem) != null;
    }
}