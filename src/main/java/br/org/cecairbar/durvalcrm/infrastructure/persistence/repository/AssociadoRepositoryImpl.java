package br.org.cecairbar.durvalcrm.infrastructure.persistence.repository;

import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.AssociadoEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class AssociadoRepositoryImpl implements AssociadoRepository {

    @Override
    @Transactional
    public Associado save(Associado associado) {
        if (associado.getId() == null) {
            // Lógica de Criação
            AssociadoEntity entity = new AssociadoEntity();
            entity.nomeCompleto = associado.getNomeCompleto();
            entity.cpf = associado.getCpf();
            entity.email = associado.getEmail();
            entity.telefone = associado.getTelefone();
            entity.ativo = associado.isAtivo();
            entity.persist();
            return toDomain(entity);
        } else {
            // Lógica de Atualização
            AssociadoEntity entity = AssociadoEntity.findById(associado.getId());
            if (entity != null) {
                entity.nomeCompleto = associado.getNomeCompleto();
                entity.cpf = associado.getCpf();
                entity.email = associado.getEmail();
                entity.telefone = associado.getTelefone();
                entity.ativo = associado.isAtivo();
                return toDomain(entity);
            } else {
                throw new NotFoundException("Associado com ID " + associado.getId() + " não encontrado para atualização.");
            }
        }
    }

    @Override
    public Optional<Associado> findById(UUID id) {
        // Correção: Usando lambda explícito
        return AssociadoEntity.<AssociadoEntity>findByIdOptional(id).map(this::toDomain);
    }

    @Override
    public List<Associado> findAll(String query) {
        String searchPattern = "%" + (query == null ? "" : query) + "%";
        return AssociadoEntity.<AssociadoEntity>find(
                "ativo = true and (lower(nomeCompleto) like lower(:query) or cpf like :query)",
                Parameters.with("query", searchPattern)
        ).list().stream()
         // Correção: Usando lambda explícito
         .map(entity -> toDomain(entity))
         .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        AssociadoEntity.update("ativo = false where id = ?1", id);
    }

    @Override
    public Optional<Associado> findByCpf(String cpf) {
        // Correção: Usando lambda explícito
        return AssociadoEntity.<AssociadoEntity>find("cpf", cpf).firstResultOptional().map(this::toDomain);
    }

    @Override
    public Optional<Associado> findByEmail(String email) {
        // Correção: Usando lambda explícito
        return AssociadoEntity.<AssociadoEntity>find("email", email).firstResultOptional().map(this::toDomain);
    }
    
    private Associado toDomain(AssociadoEntity entity) {
        return new Associado(
            entity.id,
            entity.nomeCompleto,
            entity.cpf,
            entity.email,
            entity.telefone,
            entity.ativo
        );
    }
}