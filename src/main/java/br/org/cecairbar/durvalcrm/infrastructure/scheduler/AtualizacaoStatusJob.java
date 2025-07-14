package br.org.cecairbar.durvalcrm.infrastructure.scheduler;

import br.org.cecairbar.durvalcrm.application.usecase.AtualizarStatusMensalidadesUseCase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class AtualizacaoStatusJob {
    
    private static final Logger LOGGER = Logger.getLogger(AtualizacaoStatusJob.class.getName());
    
    @Inject
    AtualizarStatusMensalidadesUseCase atualizarStatusUseCase;
    
    // Executa todos os dias às 06:00
    @Scheduled(cron = "0 0 6 * * ?")
    public void executarAtualizacaoStatus() {
        try {
            LOGGER.info("Iniciando atualização automática de status das mensalidades");
            int atualizadas = atualizarStatusUseCase.executar();
            LOGGER.info("Finalizada atualização automática. Mensalidades atualizadas: " + atualizadas);
        } catch (Exception e) {
            LOGGER.severe("Erro durante atualização automática de status: " + e.getMessage());
        }
    }
}