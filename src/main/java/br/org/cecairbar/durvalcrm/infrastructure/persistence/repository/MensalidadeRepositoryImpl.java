package br.org.cecairbar.durvalcrm.infrastructure.persistence.repository;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.MensalidadeEntity;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class MensalidadeRepositoryImpl implements MensalidadeRepository {

    @Override
    public Mensalidade save(Mensalidade mensalidade) {
        MensalidadeEntity entity = MensalidadeEntity.fromDomain(mensalidade);
        entity.persist();
        return entity.toDomain();
    }

    @Override
    public Optional<Mensalidade> findById(UUID id) {
        return MensalidadeEntity.<MensalidadeEntity>findByIdOptional(id)
            .map(MensalidadeEntity::toDomain);
    }

    @Override
    public List<Mensalidade> findByMesEAno(int mes, int ano) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "mesReferencia = ?1 and anoReferencia = ?2 order by associadoId", 
            mes, ano
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }

    @Override
    public List<Mensalidade> findByAssociadoId(UUID associadoId) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "associadoId = ?1 order by anoReferencia desc, mesReferencia desc", 
            associadoId
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }

    @Override
    public boolean existsByAssociadoEPeriodo(UUID associadoId, int mes, int ano) {
        return MensalidadeEntity.count(
            "associadoId = ?1 and mesReferencia = ?2 and anoReferencia = ?3",
            associadoId, mes, ano
        ) > 0;
    }

    @Override
    public List<Mensalidade> findByStatus(StatusMensalidade status) {
        return MensalidadeEntity.<MensalidadeEntity>find("status = ?1", status)
            .stream()
            .map(MensalidadeEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Mensalidade> findVencidas() {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "status = 'PENDENTE' and dataVencimento < ?1", 
            LocalDate.now()
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        MensalidadeEntity.deleteById(id);
    }

    @Override
    public long countByMesEAno(int mes, int ano) {
        return MensalidadeEntity.count(
            "mesReferencia = ?1 and anoReferencia = ?2", 
            mes, ano
        );
    }

    @Override
    public long countByMesEAnoEStatus(int mes, int ano, StatusMensalidade status) {
        return MensalidadeEntity.count(
            "mesReferencia = ?1 and anoReferencia = ?2 and status = ?3",
            mes, ano, status
        );
    }

    @Override
    public Optional<Mensalidade> findByIdentificadorPix(String identificadorPix) {
        return MensalidadeEntity.<MensalidadeEntity>find("identificadorPix = ?1", identificadorPix)
            .firstResultOptional()
            .map(MensalidadeEntity::toDomain);
    }
}