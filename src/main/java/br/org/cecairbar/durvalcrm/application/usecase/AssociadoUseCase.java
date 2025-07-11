package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;

import java.util.List;
import java.util.UUID;

public interface AssociadoUseCase {
    List<AssociadoDTO> findAll(String search);
    AssociadoDTO findById(UUID id);
    AssociadoDTO create(AssociadoDTO associadoDTO);
    AssociadoDTO update(UUID id, AssociadoDTO associadoDTO);
    void delete(UUID id);
}
