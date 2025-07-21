package br.org.cecairbar.durvalcrm.domain.model;

public enum StatusDoacao {
    PENDENTE("Pendente"),
    PROCESSANDO("Processando"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusDoacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}