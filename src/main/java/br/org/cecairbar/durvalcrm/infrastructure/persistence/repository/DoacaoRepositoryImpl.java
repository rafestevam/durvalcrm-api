package br.org.cecairbar.durvalcrm.infrastructure.persistence.repository;

import br.org.cecairbar.durvalcrm.application.doacao.DoacaoEntityMapper;
import br.org.cecairbar.durvalcrm.domain.model.Doacao;
import br.org.cecairbar.durvalcrm.domain.model.StatusDoacao;
import br.org.cecairbar.durvalcrm.domain.repository.DoacaoRepository;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.DoacaoEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DoacaoRepositoryImpl implements DoacaoRepository {
    
    @Inject
    DoacaoEntityMapper mapper;
    
    @Override
    @Transactional
    public Doacao save(Doacao doacao) {
        if (doacao.getId() == null) {
            DoacaoEntity entity = mapper.toEntity(doacao);
            entity.persist();
            return mapper.toDomain(entity);
        } else {
            DoacaoEntity entity = DoacaoEntity.findById(doacao.getId());
            if (entity != null) {
                mapper.updateEntityFromDomain(doacao, entity);
                return mapper.toDomain(entity);
            } else {
                throw new IllegalArgumentException("Doação com ID " + doacao.getId() + " não encontrada para atualização.");
            }
        }
    }
    
    @Override
    public Optional<Doacao> findById(UUID id) {
        return DoacaoEntity.<DoacaoEntity>findByIdOptional(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Doacao> findAll() {
        List<DoacaoEntity> entities = DoacaoEntity.<DoacaoEntity>listAll();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public List<Doacao> findByAssociado(UUID associadoId) {
        List<DoacaoEntity> entities = DoacaoEntity.<DoacaoEntity>find(
                "associado.id = :associadoId", 
                Parameters.with("associadoId", associadoId)
        ).list();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public List<Doacao> findByStatus(StatusDoacao status) {
        List<DoacaoEntity> entities = DoacaoEntity.<DoacaoEntity>find(
                "status = :status", 
                Parameters.with("status", status)
        ).list();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public List<Doacao> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<DoacaoEntity> entities = DoacaoEntity.<DoacaoEntity>find(
                "dataDoacao >= :inicio AND dataDoacao <= :fim", 
                Parameters.with("inicio", inicio).and("fim", fim)
        ).list();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public List<Doacao> findByAssociadoAndPeriodo(UUID associadoId, LocalDateTime inicio, LocalDateTime fim) {
        List<DoacaoEntity> entities = DoacaoEntity.<DoacaoEntity>find(
                "associado.id = :associadoId AND dataDoacao >= :inicio AND dataDoacao <= :fim", 
                Parameters.with("associadoId", associadoId)
                         .and("inicio", inicio)
                         .and("fim", fim)
        ).list();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public BigDecimal sumByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Object result = DoacaoEntity.find(
                "SELECT SUM(d.valor) FROM DoacaoEntity d WHERE d.dataDoacao >= :inicio " +
                "AND d.dataDoacao <= :fim AND d.status = :status",
                Parameters.with("inicio", inicio)
                         .and("fim", fim)
                         .and("status", StatusDoacao.CONFIRMADA)
        ).project(BigDecimal.class).singleResult();
        
        return result != null ? (BigDecimal) result : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal sumByAssociadoAndPeriodo(UUID associadoId, LocalDateTime inicio, LocalDateTime fim) {
        Object result = DoacaoEntity.find(
                "SELECT SUM(d.valor) FROM DoacaoEntity d WHERE d.associado.id = :associadoId " +
                "AND d.dataDoacao >= :inicio AND d.dataDoacao <= :fim AND d.status = :status",
                Parameters.with("associadoId", associadoId)
                         .and("inicio", inicio)
                         .and("fim", fim)
                         .and("status", StatusDoacao.CONFIRMADA)
        ).project(BigDecimal.class).singleResult();
        
        return result != null ? (BigDecimal) result : BigDecimal.ZERO;
    }
    
    @Override
    public long countByStatus(StatusDoacao status) {
        return DoacaoEntity.count("status = :status", Parameters.with("status", status));
    }
    
    @Override
    @Transactional
    public void delete(Doacao doacao) {
        DoacaoEntity entity = DoacaoEntity.findById(doacao.getId());
        if (entity != null) {
            entity.delete();
        }
    }
    
    @Override
    public boolean existsById(UUID id) {
        return DoacaoEntity.count("id = :id", Parameters.with("id", id)) > 0;
    }
    
    @Override
    public BigDecimal obterTotalDoacoesPorPeriodo(Instant inicio, Instant fim) {
        // Converter Instant para LocalDateTime
        LocalDateTime inicioLDT = LocalDateTime.ofInstant(inicio, java.time.ZoneId.systemDefault());
        LocalDateTime fimLDT = LocalDateTime.ofInstant(fim, java.time.ZoneId.systemDefault());
        
        List<DoacaoEntity> doacoes = DoacaoEntity.list("dataDoacao >= ?1 and dataDoacao <= ?2 and status = ?3", 
            inicioLDT, fimLDT, StatusDoacao.CONFIRMADA);
        
        return doacoes.stream()
            .map(DoacaoEntity::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}