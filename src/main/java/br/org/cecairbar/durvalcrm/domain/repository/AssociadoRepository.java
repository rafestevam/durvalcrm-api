package br.org.cecairbar.durvalcrm.domain.repository;

import br.org.cecairbar.durvalcrm.domain.model.Associado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssociadoRepository {
    Associado save(Associado associado);
    Optional<Associado> findById(UUID id);
    List<Associado> findAll(String query);
    List<Associado> findAll();
    void deleteById(UUID id);
    Optional<Associado> findByCpf(String cpf);
    Optional<Associado> findByEmail(String email);
    List<Associado> findByAtivo(Boolean ativo);
    long count();
}
