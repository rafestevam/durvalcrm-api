package br.org.cecairbar.durvalcrm.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Associado {
    private UUID id;
    private String nomeCompleto;
    private String cpf;
    private String email;
    private String telefone;
    private boolean ativo;
}
