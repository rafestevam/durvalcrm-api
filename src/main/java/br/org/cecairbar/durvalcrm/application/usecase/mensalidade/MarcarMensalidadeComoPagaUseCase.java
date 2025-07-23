package br.org.cecairbar.durvalcrm.application.usecase.mensalidade;

import br.org.cecairbar.durvalcrm.domain.model.FormaPagamento;
import java.time.Instant;
import java.util.UUID;

public interface MarcarMensalidadeComoPagaUseCase {
    void executar(UUID mensalidadeId, Instant dataPagamento);
    void executar(UUID mensalidadeId, Instant dataPagamento, FormaPagamento metodoPagamento);
}