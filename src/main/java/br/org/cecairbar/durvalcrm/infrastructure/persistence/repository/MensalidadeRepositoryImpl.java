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
    public void save(Mensalidade mensalidade) {
        MensalidadeEntity entity = MensalidadeEntity.fromDomain(mensalidade);
        entity.persist();
    }

    @Override
    public Mensalidade findById(UUID id) {
        return MensalidadeEntity.<MensalidadeEntity>findByIdOptional(id)
            .map(MensalidadeEntity::toDomain)
            .orElse(null);
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
    public List<Mensalidade> findByStatus(String status) {
        try {
            StatusMensalidade statusEnum = StatusMensalidade.valueOf(status.toUpperCase());
            return MensalidadeEntity.<MensalidadeEntity>find("status = ?1", statusEnum)
                .stream()
                .map(MensalidadeEntity::toDomain)
                .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            // Se o status não for válido, retorna lista vazia
            return List.of();
        }
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

    // Note: These methods are not in the interface, removing @Override annotations
    public void deleteById(UUID id) {
        MensalidadeEntity.deleteById(id);
    }

    public long countByMesEAno(int mes, int ano) {
        return MensalidadeEntity.count(
            "mesReferencia = ?1 and anoReferencia = ?2", 
            mes, ano
        );
    }

    public long countByMesEAnoEStatus(int mes, int ano, StatusMensalidade status) {
        return MensalidadeEntity.count(
            "mesReferencia = ?1 and anoReferencia = ?2 and status = ?3",
            mes, ano, status
        );
    }

    @Override
    public Mensalidade findByIdentificadorPix(String identificadorPix) {
        return MensalidadeEntity.<MensalidadeEntity>find("identificadorPix = ?1", identificadorPix)
            .firstResultOptional()
            .map(MensalidadeEntity::toDomain)
            .orElse(null);
    }

    @Override
    public List<Mensalidade> findByAssociadoIdAndMesEAno(UUID associadoId, int mes, int ano) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "associadoId = ?1 and mesReferencia = ?2 and anoReferencia = ?3", 
            associadoId, mes, ano
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }

    @Override
    public long countByStatusAndMesEAno(String status, int mes, int ano) {
        try {
            StatusMensalidade statusEnum = StatusMensalidade.valueOf(status.toUpperCase());
            return MensalidadeEntity.count(
                "status = ?1 and mesReferencia = ?2 and anoReferencia = ?3",
                statusEnum, mes, ano
            );
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    @Override
    public List<Mensalidade> findAll() {
        return MensalidadeEntity.<MensalidadeEntity>findAll()
            .stream()
            .map(MensalidadeEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Mensalidade> findByAno(int ano) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "anoReferencia = ?1 order by mesReferencia", 
            ano
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }

    @Override
    public void update(Mensalidade mensalidade) {
        Optional<MensalidadeEntity> entityOpt = MensalidadeEntity.findByIdOptional(mensalidade.getId());
        if (entityOpt.isPresent()) {
            MensalidadeEntity entity = entityOpt.get();
            // Update fields individually since updateFromDomain may not exist
            entity.associadoId = mensalidade.getAssociadoId();
            entity.mesReferencia = mensalidade.getMesReferencia();
            entity.anoReferencia = mensalidade.getAnoReferencia();
            entity.valor = mensalidade.getValor();
            entity.status = mensalidade.getStatus();
            entity.dataVencimento = mensalidade.getDataVencimento();
            entity.dataPagamento = mensalidade.getDataPagamento();
            entity.qrCodePix = mensalidade.getQrCodePix();
            entity.identificadorPix = mensalidade.getIdentificadorPix();
            entity.persist();
        }
    }

    @Override
    public void delete(UUID id) {
        MensalidadeEntity.deleteById(id);
    }

    @Override
    public List<Mensalidade> findPendentesByMesEAno(int mes, int ano) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "status = ?1 and mesReferencia = ?2 and anoReferencia = ?3",
            StatusMensalidade.PENDENTE, mes, ano
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }

    @Override
    public List<Mensalidade> findPagasByMesEAno(int mes, int ano) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "status = ?1 and mesReferencia = ?2 and anoReferencia = ?3",
            StatusMensalidade.PAGA, mes, ano
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }

    @Override
    public List<Mensalidade> findAtrasadasByMesEAno(int mes, int ano) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "status = ?1 and mesReferencia = ?2 and anoReferencia = ?3",
            StatusMensalidade.ATRASADA, mes, ano
        ).stream()
        .map(MensalidadeEntity::toDomain)
        .collect(Collectors.toList());
    }
}