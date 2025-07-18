package br.org.cecairbar.durvalcrm.application.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MarcarPagamentoDTO {
    private Instant dataPagamento;
    private String observacao;
}