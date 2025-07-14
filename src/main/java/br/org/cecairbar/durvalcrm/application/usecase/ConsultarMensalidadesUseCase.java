package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.application.dto.MensalidadeDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoMensalidadesDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConsultarMensalidadesUseCase {

    @Inject
    MensalidadeRepository mensalidadeRepository;

    public ResumoMensalidadesDTO obterResumo(int mes, int ano) {
        var mensalidades = mensalidadeRepository.findByMesEAno(mes, ano);
        
        return ResumoMensalidadesDTO.criarDoList(mensalidades);
    }

    public List<MensalidadeDTO> listarPorPeriodo(int mes, int ano) {
        var mensalidades = mensalidadeRepository.findByMesEAno(mes, ano);
        
        return mensalidades.stream()
            .map(MensalidadeDTO::fromDomain)
            .collect(Collectors.toList());
    }
}