package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.application.dto.MensalidadeDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoMensalidadesDTO;
import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConsultarMensalidadesUseCase {

    @Inject
    MensalidadeRepository mensalidadeRepository;

    @Inject
    AssociadoRepository associadoRepository;

    /**
     * Obtém resumo das mensalidades por período
     */
    public ResumoMensalidadesDTO obterResumo(int mes, int ano) {
        var mensalidades = mensalidadeRepository.findByMesEAno(mes, ano);
        var associadosAtivos = associadoRepository.findByAtivo(true); // ADICIONAR
        
        return ResumoMensalidadesDTO.criarDoListComTotalAssociados(
            mensalidades, 
            associadosAtivos.size() // USAR TOTAL DE ASSOCIADOS ATIVOS
        );
    }

    /**
     * Lista mensalidades por período
     */
    public List<MensalidadeDTO> listarPorPeriodo(int mes, int ano) {
        var mensalidades = mensalidadeRepository.findByMesEAno(mes, ano);
        
        return mensalidades.stream()
            .map(MensalidadeDTO::fromDomain)
            .collect(Collectors.toList());
    }

    /**
     * Obtém uma mensalidade específica por ID
     */
    public MensalidadeDTO obterPorId(String id) {
        try {
            UUID mensalidadeId = UUID.fromString(id);
            Mensalidade mensalidade = mensalidadeRepository.findById(mensalidadeId);
            
            if (mensalidade == null) {
                return null;
            }
            
            return MensalidadeDTO.fromDomain(mensalidade);
        } catch (IllegalArgumentException e) {
            // ID inválido (não é um UUID válido)
            return null;
        }
    }

    /**
     * Lista mensalidades por status
     */
    public List<MensalidadeDTO> listarPorStatus(String status) {
        var mensalidades = mensalidadeRepository.findByStatus(status);
        
        return mensalidades.stream()
            .map(MensalidadeDTO::fromDomain)
            .collect(Collectors.toList());
    }

    /**
     * Lista mensalidades de um associado específico
     */
    public List<MensalidadeDTO> listarPorAssociado(UUID associadoId) {
        var mensalidades = mensalidadeRepository.findByAssociadoId(associadoId);
        
        return mensalidades.stream()
            .map(MensalidadeDTO::fromDomain)
            .collect(Collectors.toList());
    }

    /**
     * Lista mensalidades de um associado em um período específico
     */
    public List<MensalidadeDTO> listarPorAssociadoEPeriodo(UUID associadoId, int mes, int ano) {
        var mensalidades = mensalidadeRepository.findByAssociadoIdAndMesEAno(associadoId, mes, ano);
        
        return mensalidades.stream()
            .map(MensalidadeDTO::fromDomain)
            .collect(Collectors.toList());
    }

    /**
     * Verifica se existe mensalidade para um associado em um período
     */
    public boolean existeMensalidadeParaAssociadoNoPeriodo(UUID associadoId, int mes, int ano) {
        return mensalidadeRepository.existsByAssociadoEPeriodo(associadoId, mes, ano);
    }

    /**
     * Conta mensalidades por status em um período
     */
    public long contarPorStatusEPeriodo(String status, int mes, int ano) {
        return mensalidadeRepository.countByStatusAndMesEAno(status, mes, ano);
    }
}