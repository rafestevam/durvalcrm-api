package br.org.cecairbar.durvalcrm.infrastructure.persistence.repository;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.MensalidadeEntity;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
        // Usar update direto via HQL para garantir que seja persistido
        int updated = MensalidadeEntity.update(
            "associadoId = ?1, mesReferencia = ?2, anoReferencia = ?3, valor = ?4, " +
            "status = ?5, dataVencimento = ?6, dataPagamento = ?7, qrCodePix = ?8, identificadorPix = ?9 " +
            "WHERE id = ?10",
            mensalidade.getAssociadoId(),
            mensalidade.getMesReferencia(),
            mensalidade.getAnoReferencia(),
            mensalidade.getValor(),
            mensalidade.getStatus(),
            mensalidade.getDataVencimento(),
            mensalidade.getDataPagamento(),
            mensalidade.getQrCodePix(),
            mensalidade.getIdentificadorPix(),
            mensalidade.getId()
        );
        
        if (updated == 0) {
            throw new RuntimeException("Mensalidade não encontrada para atualização: " + mensalidade.getId());
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
    
    @Override
    public BigDecimal obterValorArrecadadoPorPeriodo(int mes, int ano) {
        List<MensalidadeEntity> mensalidadesPagas = MensalidadeEntity.find(
            "status = ?1 and mesReferencia = ?2 and anoReferencia = ?3",
            StatusMensalidade.PAGA, mes, ano
        ).list();
        
        return mensalidadesPagas.stream()
            .map(entity -> entity.getValor())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public List<String> obterAssociadosComStatusPorPeriodo(int mes, int ano, StatusMensalidade status) {
        return MensalidadeEntity.<MensalidadeEntity>find(
            "status = ?1 and mesReferencia = ?2 and anoReferencia = ?3",
            status, mes, ano
        ).stream()
        .map(entity -> entity.getAssociadoId().toString())
        .collect(Collectors.toList());
    }
    
    @Override
    public List<String> obterAssociadosComMensalidadesVencidas(int mes, int ano) {
        LocalDate hoje = LocalDate.now();
        return MensalidadeEntity.<MensalidadeEntity>find(
            "(status = ?1 or status = ?2) and mesReferencia = ?3 and anoReferencia = ?4 and dataVencimento <= ?5",
            StatusMensalidade.PENDENTE, StatusMensalidade.ATRASADA, mes, ano, hoje
        ).stream()
        .map(entity -> entity.getAssociadoId().toString())
        .collect(Collectors.toList());
    }
}