package br.org.cecairbar.durvalcrm.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AssociadoDTO {
    private UUID id;
    private String nomeCompleto;
    private String cpf;
    private String email;
    private String telefone;
}
