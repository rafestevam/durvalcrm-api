package br.org.cecairbar.durvalcrm.application.usecase.mensalidade;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.FormaPagamento;
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
        System.out.println("Marcando mensalidade como paga: " + mensalidadeId);
        
        Mensalidade mensalidade = mensalidadeRepository.findById(mensalidadeId);
        
        if (mensalidade == null) {
            System.out.println("Mensalidade não encontrada: " + mensalidadeId);
            throw new NotFoundException("Mensalidade não encontrada");
        }
        
        System.out.println("Mensalidade encontrada, status atual: " + mensalidade.getStatus());
        
        mensalidade.marcarComoPaga(dataPagamento);
        
        System.out.println("Status após marcar como paga: " + mensalidade.getStatus());
        
        mensalidadeRepository.update(mensalidade);
        
        System.out.println("Mensalidade atualizada no banco de dados");
    }

    @Override
    @Transactional
    public void executar(UUID mensalidadeId, Instant dataPagamento, FormaPagamento metodoPagamento) {
        System.out.println("Marcando mensalidade como paga com método: " + mensalidadeId + " - " + metodoPagamento);
        
        Mensalidade mensalidade = mensalidadeRepository.findById(mensalidadeId);
        
        if (mensalidade == null) {
            System.out.println("Mensalidade não encontrada: " + mensalidadeId);
            throw new NotFoundException("Mensalidade não encontrada");
        }
        
        System.out.println("Mensalidade encontrada, status atual: " + mensalidade.getStatus());
        
        mensalidade.marcarComoPaga(dataPagamento, metodoPagamento);
        
        System.out.println("Status após marcar como paga: " + mensalidade.getStatus() + " - Método: " + metodoPagamento);
        
        mensalidadeRepository.update(mensalidade);
        
        System.out.println("Mensalidade atualizada no banco de dados com método de pagamento");
    }
}