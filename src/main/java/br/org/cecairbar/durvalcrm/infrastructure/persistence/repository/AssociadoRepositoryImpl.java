package br.org.cecairbar.durvalcrm.infrastructure.persistence.repository;

import br.org.cecairbar.durvalcrm.application.mapper.AssociadoMapper;
import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.AssociadoEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AssociadoRepositoryImpl implements AssociadoRepository {

    @Inject
    AssociadoMapper mapper;

    @Override
    @Transactional
    public Associado save(Associado associado) {
        if (associado.getId() == null) {
            // Lógica de Criação
            AssociadoEntity entity = mapper.toEntity(associado);
            entity.persist();
            return mapper.toDomain(entity);
        } else {
            // Lógica de Atualização
            AssociadoEntity entity = AssociadoEntity.findById(associado.getId());
            if (entity != null) {
                // Atualiza os campos da entidade existente
                entity.nomeCompleto = associado.getNomeCompleto();
                entity.cpf = associado.getCpf();
                entity.email = associado.getEmail();
                entity.telefone = associado.getTelefone();
                entity.ativo = associado.isAtivo();
                // A entidade já está gerenciada pelo Hibernate, então as mudanças serão persistidas automaticamente
                return mapper.toDomain(entity);
            } else {
                throw new NotFoundException("Associado com ID " + associado.getId() + " não encontrado para atualização.");
            }
        }
    }

    @Override
    public Optional<Associado> findById(UUID id) {
        // Corrigido: Filtra apenas associados ativos
        return AssociadoEntity.<AssociadoEntity>find("id = ?1 and ativo = true", id)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<Associado> findAll(String query) {
        String searchPattern = "%" + (query == null ? "" : query) + "%";
        List<AssociadoEntity> entities = AssociadoEntity.<AssociadoEntity>find(
                "ativo = true and (lower(nomeCompleto) like lower(:query) or cpf like :query)",
                Parameters.with("query", searchPattern)
        ).list();
        
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        // Soft delete: marca como inativo ao invés de deletar fisicamente
        AssociadoEntity.update("ativo = false where id = ?1", id);
    }

    @Override
    public Optional<Associado> findByCpf(String cpf) {
        return AssociadoEntity.<AssociadoEntity>find("cpf = ?1", cpf)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Associado> findByEmail(String email) {
        return AssociadoEntity.<AssociadoEntity>find("email = ?1 and ativo = true", email)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<Associado> findByAtivo(Boolean ativo) {
        List<AssociadoEntity> entities = AssociadoEntity.<AssociadoEntity>find("ativo = ?1", ativo).list();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public List<Associado> findAll() {
        List<AssociadoEntity> entities = AssociadoEntity.listAll();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public long count() {
        return AssociadoEntity.count();
    }
}