package br.org.cecairbar.durvalcrm.application.usecase.venda;

import br.org.cecairbar.durvalcrm.application.dto.VendaDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoVendasDTO;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface VendaUseCase {
    
    /**
     * Criar nova venda
     */
    VendaDTO criar(VendaDTO vendaDTO);
    
    /**
     * Buscar venda por ID
     */
    VendaDTO buscarPorId(UUID id);
    
    /**
     * Atualizar venda
     */
    VendaDTO atualizar(UUID id, VendaDTO vendaDTO);
    
    /**
     * Deletar venda
     */
    void deletar(UUID id);
    
    /**
     * Listar todas as vendas
     */
    List<VendaDTO> listarTodas();
    
    /**
     * Listar vendas por período
     */
    List<VendaDTO> listarPorPeriodo(Instant dataInicio, Instant dataFim);
    
    /**
     * Listar vendas por origem
     */
    List<VendaDTO> listarPorOrigem(OrigemVenda origem);
    
    /**
     * Listar vendas recentes (últimos 30 dias)
     */
    List<VendaDTO> listarRecentes();
    
    /**
     * Obter resumo de vendas por período
     */
    ResumoVendasDTO obterResumo(Instant dataInicio, Instant dataFim);
    
    /**
     * Obter resumo de vendas por origem e período
     */
    ResumoVendasDTO obterResumoPorOrigem(OrigemVenda origem, Instant dataInicio, Instant dataFim);
}