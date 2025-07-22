package br.org.cecairbar.durvalcrm.application.usecase.venda;

import br.org.cecairbar.durvalcrm.application.dto.VendaDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoVendasDTO;
import br.org.cecairbar.durvalcrm.domain.model.Venda;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import br.org.cecairbar.durvalcrm.domain.repository.VendaRepository;

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
    
    
    @Override
    @Transactional
    public VendaDTO criar(VendaDTO vendaDTO) {
        // Criar venda
        Venda venda = Venda.criar(
            vendaDTO.getDescricao(),
            vendaDTO.getValor(),
            vendaDTO.getOrigem(),
            vendaDTO.getFormaPagamento()
        );
        
        // Salvar
        vendaRepository.save(venda);
        
        // Retornar DTO
        return toDTO(venda);
    }
    
    @Override
    public VendaDTO buscarPorId(UUID id) {
        Venda venda = vendaRepository.findById(id);
        if (venda == null) {
            throw new NotFoundException("Venda não encontrada");
        }
        
        return toDTO(venda);
    }
    
    @Override
    @Transactional
    public VendaDTO atualizar(UUID id, VendaDTO vendaDTO) {
        Venda venda = vendaRepository.findById(id);
        if (venda == null) {
            throw new NotFoundException("Venda não encontrada");
        }
        
        // Atualizar venda
        venda.atualizar(
            vendaDTO.getDescricao(),
            vendaDTO.getValor(),
            vendaDTO.getOrigem(),
            vendaDTO.getFormaPagamento()
        );
        
        // Salvar
        vendaRepository.update(venda);
        
        return toDTO(venda);
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
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VendaDTO> listarPorPeriodo(Instant dataInicio, Instant dataFim) {
        if (dataInicio.isAfter(dataFim)) {
            throw new BadRequestException("Data de início deve ser anterior à data de fim");
        }
        
        return vendaRepository.findByPeriodo(dataInicio, dataFim).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VendaDTO> listarPorOrigem(OrigemVenda origem) {
        return vendaRepository.findByOrigem(origem).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VendaDTO> listarRecentes() {
        return vendaRepository.findRecentes().stream()
                .map(this::toDTO)
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
    private VendaDTO toDTO(Venda venda) {
        return VendaDTO.builder()
                .id(venda.getId())
                .descricao(venda.getDescricao())
                .valor(venda.getValor())
                .origem(venda.getOrigem())
                .formaPagamento(venda.getFormaPagamento())
                .dataVenda(venda.getDataVenda())
                .criadoEm(venda.getCriadoEm())
                .atualizadoEm(venda.getAtualizadoEm())
                .build();
    }
}