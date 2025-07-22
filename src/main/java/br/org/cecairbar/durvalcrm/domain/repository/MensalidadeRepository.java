package br.org.cecairbar.durvalcrm.domain.repository;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface para operações com Mensalidades
 */
public interface MensalidadeRepository {
    
    /**
     * Salva uma mensalidade
     */
    void save(Mensalidade mensalidade);
    
    /**
     * Busca mensalidade por ID
     */
    Mensalidade findById(UUID id);
    
    /**
     * Lista mensalidades por mês e ano
     */
    List<Mensalidade> findByMesEAno(int mes, int ano);
    
    /**
     * Lista mensalidades por status
     */
    List<Mensalidade> findByStatus(String status);
    
    /**
     * Lista mensalidades por associado
     */
    List<Mensalidade> findByAssociadoId(UUID associadoId);
    
    /**
     * Lista mensalidades por associado e período
     */
    List<Mensalidade> findByAssociadoIdAndMesEAno(UUID associadoId, int mes, int ano);
    
    /**
     * Verifica se existe mensalidade para associado no período
     */
    boolean existsByAssociadoEPeriodo(UUID associadoId, int mes, int ano);
    
    /**
     * Conta mensalidades por status e período
     */
    long countByStatusAndMesEAno(String status, int mes, int ano);
    
    /**
     * Lista todas as mensalidades
     */
    List<Mensalidade> findAll();
    
    /**
     * Lista mensalidades por ano
     */
    List<Mensalidade> findByAno(int ano);
    
    /**
     * Lista mensalidades vencidas (para job de atualização de status)
     */
    List<Mensalidade> findVencidas();
    
    /**
     * Atualiza uma mensalidade
     */
    void update(Mensalidade mensalidade);
    
    /**
     * Remove uma mensalidade
     */
    void delete(UUID id);
    
    /**
     * Busca mensalidades por identificador PIX
     */
    Mensalidade findByIdentificadorPix(String identificadorPix);
    
    /**
     * Lista mensalidades pendentes de um período
     */
    List<Mensalidade> findPendentesByMesEAno(int mes, int ano);
    
    /**
     * Lista mensalidades pagas de um período
     */
    List<Mensalidade> findPagasByMesEAno(int mes, int ano);
    
    /**
     * Lista mensalidades atrasadas de um período
     */
    List<Mensalidade> findAtrasadasByMesEAno(int mes, int ano);
    
    /**
     * Obtém o valor total arrecadado em um período
     */
    BigDecimal obterValorArrecadadoPorPeriodo(int mes, int ano);
    
    /**
     * Obtém a lista de IDs de associados com determinado status de mensalidade em um período
     */
    List<String> obterAssociadosComStatusPorPeriodo(int mes, int ano, StatusMensalidade status);
}