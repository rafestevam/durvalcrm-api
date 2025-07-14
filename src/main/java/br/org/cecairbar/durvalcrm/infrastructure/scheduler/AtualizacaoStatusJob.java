package br.org.cecairbar.durvalcrm.infrastructure.scheduler;

import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AtualizacaoStatusJob {

    private static final Logger LOG = Logger.getLogger(AtualizacaoStatusJob.class);

    @Inject
    MensalidadeRepository mensalidadeRepository;

    @Scheduled(cron = "0 0 1 * * ?") // Executa todo dia às 01:00
    @Transactional
    public void atualizarStatusMensalidades() {
        LOG.info("Iniciando atualização de status das mensalidades...");
        
        try {
            var mensalidadesVencidas = mensalidadeRepository.findVencidas();
            
            for (var mensalidade : mensalidadesVencidas) {
                mensalidade.atualizarStatus();
                mensalidadeRepository.save(mensalidade);
            }
            
            LOG.infof("Atualizado status de %d mensalidades", mensalidadesVencidas.size());
        } catch (Exception e) {
            LOG.error("Erro ao atualizar status das mensalidades", e);
        }
    }
}