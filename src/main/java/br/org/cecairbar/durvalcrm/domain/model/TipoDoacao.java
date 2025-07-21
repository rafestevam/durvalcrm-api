package br.org.cecairbar.durvalcrm.domain.model;

public enum TipoDoacao {
    UNICA("Doação Única"),
    RECORRENTE("Doação Recorrente");

    private final String descricao;

    TipoDoacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}