package br.org.cecairbar.durvalcrm.application.dto;

import br.org.cecairbar.durvalcrm.domain.model.FormaPagamento;
import lombok.Data;

import java.time.Instant;

@Data
public class MarcarPagamentoDTO {
    private Instant dataPagamento;
    private String observacao;
    private FormaPagamento metodoPagamento; // PIX ou DINHEIRO
}