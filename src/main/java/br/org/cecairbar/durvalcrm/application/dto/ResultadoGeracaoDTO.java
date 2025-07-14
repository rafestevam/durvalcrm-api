package br.org.cecairbar.durvalcrm.application.dto;

public class ResultadoGeracaoDTO {
    public int cobrancasGeradas;
    public int jaExistiam;
    public int totalAssociados;
    public String mensagem;

    public ResultadoGeracaoDTO(int geradas, int jaExistiam, int total) {
        this.cobrancasGeradas = geradas;
        this.jaExistiam = jaExistiam;
        this.totalAssociados = total;
        this.mensagem = criarMensagem();
    }

    private String criarMensagem() {
        if (cobrancasGeradas == 0) {
            return "Todas as cobranças já foram geradas para este período.";
        }
        return String.format("Geradas %d novas cobranças. %d já existiam.", 
            cobrancasGeradas, jaExistiam);
    }
}