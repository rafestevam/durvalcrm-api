package br.org.cecairbar.durvalcrm.domain.model;

public enum FormaPagamento {
    PIX("PIX"),
    DINHEIRO("Dinheiro");
    
    private final String descricao;
    
    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}