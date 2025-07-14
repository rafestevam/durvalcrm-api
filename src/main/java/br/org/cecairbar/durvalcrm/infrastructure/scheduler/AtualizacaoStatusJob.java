package br.org.cecairbar.durvalcrm.infrastructure.scheduler;

import br.org.cecairbar.durvalcrm.application.usecase.AtualizarStatusMensalidadesUseCase;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AtualizacaoStatusJob {

    private static final Logger LOG = Logger.getLogger(AtualizacaoStatusJob.class);

    @Inject
    AtualizarStatusMensalidadesUseCase atualizarStatusUseCase;

    @Scheduled(cron = "0 0 1 * * ?") // Executa todo dia às 01:00
    public void atualizarStatusMensalidades() {
        LOG.info("Executando job de atualização de status das mensalidades...");
        
        try {
            int mensalidadesAtualizadas = atualizarStatusUseCase.executar();
            LOG.infof("Job concluído com sucesso. %d mensalidades atualizadas.", mensalidadesAtualizadas);
        } catch (Exception e) {
            LOG.error("Erro no job de atualização de status das mensalidades", e);
        }
    }
}