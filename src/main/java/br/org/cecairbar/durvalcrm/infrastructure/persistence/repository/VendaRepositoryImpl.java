package br.org.cecairbar.durvalcrm.infrastructure.persistence.repository;

import br.org.cecairbar.durvalcrm.domain.model.Venda;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import br.org.cecairbar.durvalcrm.domain.repository.VendaRepository;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.VendaEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@ApplicationScoped
public class VendaRepositoryImpl implements VendaRepository {
    
    @Inject
    VendaPanacheRepository panacheRepository;
    
    @Inject
    EntityManager entityManager;
    
    @Override
    public void save(Venda venda) {
        VendaEntity entity = toEntity(venda);
        panacheRepository.persist(entity);
    }
    
    @Override
    public Venda findById(UUID id) {
        VendaEntity entity = panacheRepository.find("id", id).firstResult();
        return entity != null ? toDomain(entity) : null;
    }
    
    @Override
    public List<Venda> findAll() {
        return panacheRepository.listAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Venda> findByPeriodo(Instant dataInicio, Instant dataFim) {
        return panacheRepository.find("dataVenda >= ?1 and dataVenda <= ?2", dataInicio, dataFim)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Venda> findByOrigem(OrigemVenda origem) {
        return panacheRepository.find("origem", origem)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Venda> findByOrigemAndPeriodo(OrigemVenda origem, Instant dataInicio, Instant dataFim) {
        return panacheRepository.find("origem = ?1 and dataVenda >= ?2 and dataVenda <= ?3", 
                   origem, dataInicio, dataFim)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countByPeriodo(Instant dataInicio, Instant dataFim) {
        return panacheRepository.count("dataVenda >= ?1 and dataVenda <= ?2", dataInicio, dataFim);
    }
    
    @Override
    public long countByOrigem(OrigemVenda origem) {
        return panacheRepository.count("origem", origem);
    }
    
    @Override
    public BigDecimal sumValorByPeriodo(Instant dataInicio, Instant dataFim) {
        BigDecimal result = entityManager
                .createQuery("SELECT SUM(v.valor) FROM VendaEntity v WHERE v.dataVenda >= :inicio AND v.dataVenda <= :fim", BigDecimal.class)
                .setParameter("inicio", dataInicio)
                .setParameter("fim", dataFim)
                .getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal sumValorByOrigem(OrigemVenda origem) {
        BigDecimal result = entityManager
                .createQuery("SELECT SUM(v.valor) FROM VendaEntity v WHERE v.origem = :origem", BigDecimal.class)
                .setParameter("origem", origem)
                .getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal sumValorByOrigemAndPeriodo(OrigemVenda origem, Instant dataInicio, Instant dataFim) {
        BigDecimal result = entityManager
                .createQuery("SELECT SUM(v.valor) FROM VendaEntity v WHERE v.origem = :origem AND v.dataVenda >= :inicio AND v.dataVenda <= :fim", BigDecimal.class)
                .setParameter("origem", origem)
                .setParameter("inicio", dataInicio)
                .setParameter("fim", dataFim)
                .getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }
    
    @Override
    public void update(Venda venda) {
        VendaEntity entity = toEntity(venda);
        entity.setAtualizadoEm(Instant.now());
        panacheRepository.persist(entity);
    }
    
    @Override
    public void delete(UUID id) {
        panacheRepository.delete("id", id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return panacheRepository.find("id", id).firstResult() != null;
    }
    
    @Override
    public List<Venda> findRecentes() {
        Instant trintaDiasAtras = Instant.now().minus(30, ChronoUnit.DAYS);
        return findByPeriodo(trintaDiasAtras, Instant.now());
    }
    
    // Métodos de conversão
    private VendaEntity toEntity(Venda venda) {
        return VendaEntity.builder()
                .id(venda.getId())
                .descricao(venda.getDescricao())
                .valor(venda.getValor())
                .origem(venda.getOrigem())
                .dataVenda(venda.getDataVenda())
                .criadoEm(venda.getCriadoEm())
                .atualizadoEm(venda.getAtualizadoEm())
                .build();
    }
    
    private Venda toDomain(VendaEntity entity) {
        return Venda.builder()
                .id(entity.getId())
                .descricao(entity.getDescricao())
                .valor(entity.getValor())
                .origem(entity.getOrigem())
                .dataVenda(entity.getDataVenda())
                .criadoEm(entity.getCriadoEm())
                .atualizadoEm(entity.getAtualizadoEm())
                .build();
    }
}