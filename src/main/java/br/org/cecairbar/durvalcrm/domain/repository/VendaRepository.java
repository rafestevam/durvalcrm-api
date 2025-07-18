package br.org.cecairbar.durvalcrm.domain.repository;

import br.org.cecairbar.durvalcrm.domain.model.Venda;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * Repository interface para operações com Vendas
 */
public interface VendaRepository {
    
    /**
     * Salva uma venda
     */
    void save(Venda venda);
    
    /**
     * Busca venda por ID
     */
    Venda findById(UUID id);
    
    /**
     * Lista todas as vendas
     */
    List<Venda> findAll();
    
    /**
     * Lista vendas por período
     */
    List<Venda> findByPeriodo(Instant dataInicio, Instant dataFim);
    
    /**
     * Lista vendas por associado
     */
    List<Venda> findByAssociadoId(UUID associadoId);
    
    /**
     * Lista vendas por origem
     */
    List<Venda> findByOrigem(OrigemVenda origem);
    
    /**
     * Lista vendas por associado e período
     */
    List<Venda> findByAssociadoIdAndPeriodo(UUID associadoId, Instant dataInicio, Instant dataFim);
    
    /**
     * Lista vendas por origem e período
     */
    List<Venda> findByOrigemAndPeriodo(OrigemVenda origem, Instant dataInicio, Instant dataFim);
    
    /**
     * Conta vendas por período
     */
    long countByPeriodo(Instant dataInicio, Instant dataFim);
    
    /**
     * Conta vendas por origem
     */
    long countByOrigem(OrigemVenda origem);
    
    /**
     * Soma valor total das vendas por período
     */
    BigDecimal sumValorByPeriodo(Instant dataInicio, Instant dataFim);
    
    /**
     * Soma valor total das vendas por origem
     */
    BigDecimal sumValorByOrigem(OrigemVenda origem);
    
    /**
     * Soma valor total das vendas por origem e período
     */
    BigDecimal sumValorByOrigemAndPeriodo(OrigemVenda origem, Instant dataInicio, Instant dataFim);
    
    /**
     * Atualiza uma venda
     */
    void update(Venda venda);
    
    /**
     * Remove uma venda
     */
    void delete(UUID id);
    
    /**
     * Verifica se existe venda com o ID
     */
    boolean existsById(UUID id);
    
    /**
     * Lista vendas recentes (últimos 30 dias)
     */
    List<Venda> findRecentes();
    
    /**
     * Lista vendas por associado (últimos 30 dias)
     */
    List<Venda> findRecentesByAssociadoId(UUID associadoId);
}