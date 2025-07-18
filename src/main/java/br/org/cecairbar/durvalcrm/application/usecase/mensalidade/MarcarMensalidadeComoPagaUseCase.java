package br.org.cecairbar.durvalcrm.application.usecase.mensalidade;

import java.time.Instant;
import java.util.UUID;

public interface MarcarMensalidadeComoPagaUseCase {
    void executar(UUID mensalidadeId, Instant dataPagamento);
}