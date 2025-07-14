package br.org.cecairbar.durvalcrm.domain.model;

public enum StatusMensalidade {
    PENDENTE("Pendente"),
    PAGA("Paga"),
    ATRASADA("Atrasada");

    private final String descricao;

    StatusMensalidade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
