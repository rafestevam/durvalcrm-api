package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;

@ApplicationScoped
public class AtualizarStatusMensalidadesUseCase {

    private static final Logger LOG = Logger.getLogger(AtualizarStatusMensalidadesUseCase.class);

    @Inject
    MensalidadeRepository mensalidadeRepository;

    @Transactional
    public int executar() {
        LOG.info("Iniciando atualização de status das mensalidades...");
        
        try {
            // Busca mensalidades pendentes que estão vencidas
            var mensalidadesVencidas = mensalidadeRepository.findVencidas();
            
            int mensalidadesAtualizadas = 0;
            for (var mensalidade : mensalidadesVencidas) {
                if (mensalidade.getStatus() == StatusMensalidade.PENDENTE && 
                    mensalidade.getDataVencimento().isBefore(LocalDate.now())) {
                    
                    mensalidade.atualizarStatus();
                    mensalidadeRepository.save(mensalidade);
                    mensalidadesAtualizadas++;
                }
            }
            
            LOG.infof("Atualizado status de %d mensalidades para ATRASADA", mensalidadesAtualizadas);
            return mensalidadesAtualizadas;
            
        } catch (Exception e) {
            LOG.error("Erro ao atualizar status das mensalidades", e);
            throw new RuntimeException("Falha na atualização de status das mensalidades", e);
        }
    }
}