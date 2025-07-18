package br.org.cecairbar.durvalcrm.application.usecase.venda;

import br.org.cecairbar.durvalcrm.application.dto.VendaDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoVendasDTO;
import br.org.cecairbar.durvalcrm.domain.model.Venda;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import br.org.cecairbar.durvalcrm.domain.repository.VendaRepository;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.model.Associado;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.BadRequestException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@ApplicationScoped
public class VendaUseCaseImpl implements VendaUseCase {
    
    @Inject
    VendaRepository vendaRepository;
    
    @Inject
    AssociadoRepository associadoRepository;
    
    @Override
    @Transactional
    public VendaDTO criar(VendaDTO vendaDTO) {
        // Validar se associado existe
        Associado associado = associadoRepository.findById(vendaDTO.getAssociadoId())
                .orElseThrow(() -> new NotFoundException("Associado não encontrado"));
        
        // Criar venda
        Venda venda = Venda.criar(
            vendaDTO.getDescricao(),
            vendaDTO.getValor(),
            vendaDTO.getOrigem(),
            vendaDTO.getAssociadoId(),
            vendaDTO.getObservacoes()
        );
        
        // Salvar
        vendaRepository.save(venda);
        
        // Retornar DTO
        return toDTO(venda, associado.getNomeCompleto());
    }
    
    @Override
    public VendaDTO buscarPorId(UUID id) {
        Venda venda = vendaRepository.findById(id);
        if (venda == null) {
            throw new NotFoundException("Venda não encontrada");
        }
        
        // Buscar nome do associado
        String nomeAssociado = associadoRepository.findById(venda.getAssociadoId())
                .map(Associado::getNomeCompleto)
                .orElse("Não encontrado");
        
        return toDTO(venda, nomeAssociado);
    }
    
    @Override
    @Transactional
    public VendaDTO atualizar(UUID id, VendaDTO vendaDTO) {
        Venda venda = vendaRepository.findById(id);
        if (venda == null) {
            throw new NotFoundException("Venda não encontrada");
        }
        
        // Validar se associado existe (se mudou)
        if (!venda.getAssociadoId().equals(vendaDTO.getAssociadoId())) {
            associadoRepository.findById(vendaDTO.getAssociadoId())
                    .orElseThrow(() -> new NotFoundException("Associado não encontrado"));
        }
        
        // Atualizar venda
        venda.atualizar(
            vendaDTO.getDescricao(),
            vendaDTO.getValor(),
            vendaDTO.getOrigem(),
            vendaDTO.getObservacoes()
        );
        
        // Salvar
        vendaRepository.update(venda);
        
        // Buscar nome do associado
        String nomeAssociado = associadoRepository.findById(venda.getAssociadoId())
                .map(Associado::getNomeCompleto)
                .orElse("Não encontrado");
        
        return toDTO(venda, nomeAssociado);
    }
    
    @Override
    @Transactional
    public void deletar(UUID id) {
        if (!vendaRepository.existsById(id)) {
            throw new NotFoundException("Venda não encontrada");
        }
        vendaRepository.delete(id);
    }
    
    @Override
    public List<VendaDTO> listarTodas() {
        return vendaRepository.findAll().stream()
                .map(this::toDTOComAssociado)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VendaDTO> listarPorPeriodo(Instant dataInicio, Instant dataFim) {
        if (dataInicio.isAfter(dataFim)) {
            throw new BadRequestException("Data de início deve ser anterior à data de fim");
        }
        
        return vendaRepository.findByPeriodo(dataInicio, dataFim).stream()
                .map(this::toDTOComAssociado)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VendaDTO> listarPorAssociado(UUID associadoId) {
        // Verificar se associado existe
        associadoRepository.findById(associadoId)
                .orElseThrow(() -> new NotFoundException("Associado não encontrado"));
        
        return vendaRepository.findByAssociadoId(associadoId).stream()
                .map(this::toDTOComAssociado)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VendaDTO> listarPorOrigem(OrigemVenda origem) {
        return vendaRepository.findByOrigem(origem).stream()
                .map(this::toDTOComAssociado)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VendaDTO> listarRecentes() {
        return vendaRepository.findRecentes().stream()
                .map(this::toDTOComAssociado)
                .collect(Collectors.toList());
    }
    
    @Override
    public ResumoVendasDTO obterResumo(Instant dataInicio, Instant dataFim) {
        if (dataInicio.isAfter(dataFim)) {
            throw new BadRequestException("Data de início deve ser anterior à data de fim");
        }
        
        // Buscar dados
        long totalVendas = vendaRepository.countByPeriodo(dataInicio, dataFim);
        BigDecimal valorTotal = vendaRepository.sumValorByPeriodo(dataInicio, dataFim);
        
        // Vendas por origem
        Map<OrigemVenda, Long> vendasPorOrigem = new HashMap<>();
        Map<OrigemVenda, BigDecimal> valoresPorOrigem = new HashMap<>();
        
        for (OrigemVenda origem : OrigemVenda.values()) {
            vendasPorOrigem.put(origem, vendaRepository.countByOrigem(origem));
            valoresPorOrigem.put(origem, vendaRepository.sumValorByOrigemAndPeriodo(origem, dataInicio, dataFim));
        }
        
        return ResumoVendasDTO.criar(dataInicio, dataFim, vendasPorOrigem, valoresPorOrigem, totalVendas, valorTotal);
    }
    
    @Override
    public ResumoVendasDTO obterResumoPorOrigem(OrigemVenda origem, Instant dataInicio, Instant dataFim) {
        if (dataInicio.isAfter(dataFim)) {
            throw new BadRequestException("Data de início deve ser anterior à data de fim");
        }
        
        // Buscar dados apenas da origem específica
        long totalVendas = vendaRepository.findByOrigemAndPeriodo(origem, dataInicio, dataFim).size();
        BigDecimal valorTotal = vendaRepository.sumValorByOrigemAndPeriodo(origem, dataInicio, dataFim);
        
        // Criar mapas apenas com a origem específica
        Map<OrigemVenda, Long> vendasPorOrigem = new HashMap<>();
        Map<OrigemVenda, BigDecimal> valoresPorOrigem = new HashMap<>();
        
        vendasPorOrigem.put(origem, totalVendas);
        valoresPorOrigem.put(origem, valorTotal);
        
        return ResumoVendasDTO.criar(dataInicio, dataFim, vendasPorOrigem, valoresPorOrigem, totalVendas, valorTotal);
    }
    
    // Métodos auxiliares
    private VendaDTO toDTO(Venda venda, String nomeAssociado) {
        return VendaDTO.builder()
                .id(venda.getId())
                .descricao(venda.getDescricao())
                .valor(venda.getValor())
                .origem(venda.getOrigem())
                .dataVenda(venda.getDataVenda())
                .observacoes(venda.getObservacoes())
                .associadoId(venda.getAssociadoId())
                .nomeAssociado(nomeAssociado)
                .criadoEm(venda.getCriadoEm())
                .atualizadoEm(venda.getAtualizadoEm())
                .build();
    }
    
    private VendaDTO toDTOComAssociado(Venda venda) {
        String nomeAssociado = associadoRepository.findById(venda.getAssociadoId())
                .map(Associado::getNomeCompleto)
                .orElse("Não encontrado");
        return toDTO(venda, nomeAssociado);
    }
}