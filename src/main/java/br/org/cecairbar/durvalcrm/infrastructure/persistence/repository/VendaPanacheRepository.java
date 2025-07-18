package br.org.cecairbar.durvalcrm.infrastructure.persistence.repository;

import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.VendaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VendaPanacheRepository implements PanacheRepository<VendaEntity> {
    // Todos os métodos do PanacheRepository são herdados automaticamente
}