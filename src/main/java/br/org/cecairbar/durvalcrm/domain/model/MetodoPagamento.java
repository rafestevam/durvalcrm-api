package br.org.cecairbar.durvalcrm.domain.model;

public enum MetodoPagamento {
    PIX("PIX"),
    DINHEIRO("Dinheiro");
    
    private final String descricao;
    
    MetodoPagamento(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}