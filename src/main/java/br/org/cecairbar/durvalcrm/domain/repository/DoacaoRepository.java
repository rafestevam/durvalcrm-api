package br.org.cecairbar.durvalcrm.domain.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.org.cecairbar.durvalcrm.domain.model.Doacao;
import br.org.cecairbar.durvalcrm.domain.model.StatusDoacao;

public interface DoacaoRepository {
    Doacao save(Doacao doacao);
    
    Optional<Doacao> findById(UUID id);
    
    List<Doacao> findAll();
    
    List<Doacao> findByAssociado(UUID associadoId);
    
    List<Doacao> findByStatus(StatusDoacao status);
    
    List<Doacao> findByPeriodo(LocalDateTime inicio, LocalDateTime fim);
    
    List<Doacao> findByAssociadoAndPeriodo(UUID associadoId, LocalDateTime inicio, LocalDateTime fim);
    
    BigDecimal sumByPeriodo(LocalDateTime inicio, LocalDateTime fim);
    
    BigDecimal sumByAssociadoAndPeriodo(UUID associadoId, LocalDateTime inicio, LocalDateTime fim);
    
    long countByStatus(StatusDoacao status);
    
    void delete(Doacao doacao);
    
    boolean existsById(UUID id);
}