package br.org.cecairbar.durvalcrm.domain.repository;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensalidadeRepository {
    Mensalidade save(Mensalidade mensalidade);
    Optional<Mensalidade> findById(UUID id);
    List<Mensalidade> findByMesEAno(int mes, int ano);
    List<Mensalidade> findByAssociadoId(UUID associadoId);
    boolean existsByAssociadoEPeriodo(UUID associadoId, int mes, int ano);
    List<Mensalidade> findByStatus(StatusMensalidade status);
    List<Mensalidade> findVencidas();
    void deleteById(UUID id);
    long countByMesEAno(int mes, int ano);
    long countByMesEAnoEStatus(int mes, int ano, StatusMensalidade status);
    Optional<Mensalidade> findByIdentificadorPix(String identificadorPix);
}