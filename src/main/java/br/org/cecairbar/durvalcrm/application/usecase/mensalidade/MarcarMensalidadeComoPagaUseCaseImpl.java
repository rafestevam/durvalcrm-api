package br.org.cecairbar.durvalcrm.application.usecase.mensalidade;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class MarcarMensalidadeComoPagaUseCaseImpl implements MarcarMensalidadeComoPagaUseCase {

    @Inject
    MensalidadeRepository mensalidadeRepository;

    @Override
    @Transactional
    public void executar(UUID mensalidadeId, Instant dataPagamento) {
        Mensalidade mensalidade = mensalidadeRepository.findById(mensalidadeId);
        
        if (mensalidade == null) {
            throw new NotFoundException("Mensalidade não encontrada");
        }
        
        mensalidade.marcarComoPaga(dataPagamento);
        mensalidadeRepository.update(mensalidade);
    }
}