package br.org.cecairbar.durvalcrm.application.doacao;

import br.org.cecairbar.durvalcrm.domain.model.*;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.DoacaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DoacaoService {
    
    @Inject
    DoacaoRepository doacaoRepository;
    
    @Inject
    AssociadoRepository associadoRepository;
    
    @Inject
    DoacaoMapper mapper;
    
    public List<DoacaoDTO> listarTodas() {
        return mapper.toDTOList(doacaoRepository.findAll());
    }
    
    public DoacaoDTO buscarPorId(UUID id) {
        return doacaoRepository.findById(id)
                .map(mapper::toDTO)
                .orElse(null);
    }
    
    public List<DoacaoDTO> listarPorAssociado(UUID associadoId) {
        return mapper.toDTOList(doacaoRepository.findByAssociado(associadoId));
    }
    
    public List<DoacaoDTO> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return mapper.toDTOList(doacaoRepository.findByPeriodo(inicio, fim));
    }
    
    @Transactional
    public DoacaoDTO criar(DoacaoDTO dto) {
        Associado associado = associadoRepository.findById(dto.getAssociadoId())
                .orElseThrow(() -> new IllegalArgumentException("Associado não encontrado"));
        
        Doacao doacao = mapper.toEntity(dto);
        doacao.setAssociado(associado);
        
        if (doacao.getStatus() == null) {
            doacao.setStatus(StatusDoacao.PENDENTE);
        }
        
        doacao = doacaoRepository.save(doacao);
        return mapper.toDTO(doacao);
    }
    
    @Transactional
    public DoacaoDTO atualizar(UUID id, DoacaoDTO dto) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada"));
        
        mapper.updateEntityFromDTO(dto, doacao);
        
        doacao = doacaoRepository.save(doacao);
        return mapper.toDTO(doacao);
    }
    
    @Transactional
    public DoacaoDTO confirmarPagamento(UUID id, String codigoTransacao, String metodoPagamento) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada"));
        
        if (doacao.getStatus() != StatusDoacao.PENDENTE && doacao.getStatus() != StatusDoacao.PROCESSANDO) {
            throw new IllegalStateException("Doação não pode ser confirmada no status atual");
        }
        
        doacao.setStatus(StatusDoacao.CONFIRMADA);
        doacao.setDataConfirmacao(LocalDateTime.now());
        doacao.setCodigoTransacao(codigoTransacao);
        doacao.setMetodoPagamento(metodoPagamento);
        
        doacao = doacaoRepository.save(doacao);
        return mapper.toDTO(doacao);
    }
    
    @Transactional
    public DoacaoDTO cancelar(UUID id) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada"));
        
        if (doacao.getStatus() == StatusDoacao.CONFIRMADA) {
            throw new IllegalStateException("Doação confirmada não pode ser cancelada");
        }
        
        doacao.setStatus(StatusDoacao.CANCELADA);
        
        doacao = doacaoRepository.save(doacao);
        return mapper.toDTO(doacao);
    }
    
    @Transactional
    public void excluir(UUID id) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada"));
        
        if (doacao.getStatus() == StatusDoacao.CONFIRMADA) {
            throw new IllegalStateException("Doação confirmada não pode ser excluída");
        }
        
        doacaoRepository.delete(doacao);
    }
    
    public DoacaoEstatisticasDTO obterEstatisticas(LocalDateTime inicio, LocalDateTime fim) {
        List<Doacao> doacoes = doacaoRepository.findByPeriodo(inicio, fim);
        
        DoacaoEstatisticasDTO estatisticas = new DoacaoEstatisticasDTO();
        estatisticas.setTotalDoacoes((long) doacoes.size());
        
        long confirmadas = doacoes.stream()
                .filter(d -> d.getStatus() == StatusDoacao.CONFIRMADA)
                .count();
        estatisticas.setDoacoesConfirmadas(confirmadas);
        
        long pendentes = doacoes.stream()
                .filter(d -> d.getStatus() == StatusDoacao.PENDENTE || d.getStatus() == StatusDoacao.PROCESSANDO)
                .count();
        estatisticas.setDoacoesPendentes(pendentes);
        
        long canceladas = doacoes.stream()
                .filter(d -> d.getStatus() == StatusDoacao.CANCELADA)
                .count();
        estatisticas.setDoacoesCanceladas(canceladas);
        
        BigDecimal totalArrecadado = doacoes.stream()
                .filter(d -> d.getStatus() == StatusDoacao.CONFIRMADA)
                .map(Doacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        estatisticas.setTotalArrecadado(totalArrecadado);
        
        if (confirmadas > 0) {
            BigDecimal ticketMedio = totalArrecadado.divide(
                    BigDecimal.valueOf(confirmadas), 2, RoundingMode.HALF_UP);
            estatisticas.setTicketMedio(ticketMedio);
        } else {
            estatisticas.setTicketMedio(BigDecimal.ZERO);
        }
        
        return estatisticas;
    }
    
    public String gerarCodigoPix(UUID doacaoId) {
        doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada"));
        
        return "00020126580014BR.GOV.BCB.PIX01365199e8c3-" + UUID.randomUUID().toString();
    }
}