package br.org.cecairbar.durvalcrm.application.usecase.impl;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;
import br.org.cecairbar.durvalcrm.application.mapper.AssociadoMapper;
import br.org.cecairbar.durvalcrm.application.usecase.AssociadoUseCase;
import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AssociadoUseCaseImpl implements AssociadoUseCase {

    @Inject
    AssociadoRepository associadoRepository;

    @Inject
    AssociadoMapper mapper;

    @Override
    public List<AssociadoDTO> findAll(String search) {
        return associadoRepository.findAll(search).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AssociadoDTO findById(UUID id) {
        return associadoRepository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new NotFoundException("Associado não encontrado"));
    }

    @Override
    public AssociadoDTO create(AssociadoDTO associadoDTO) {
        // Validações de negócio
        associadoRepository.findByCpf(associadoDTO.getCpf())
                .ifPresent(a -> { throw new WebApplicationException("CPF já cadastrado", Response.Status.CONFLICT); });
        associadoRepository.findByEmail(associadoDTO.getEmail())
                .ifPresent(a -> { throw new WebApplicationException("E-mail já cadastrado", Response.Status.CONFLICT); });

        Associado associado = mapper.toDomain(associadoDTO);
        associado.setAtivo(true); // Garante que novos associados sejam ativos
        return mapper.toDTO(associadoRepository.save(associado));
    }

    @Override
    public AssociadoDTO update(UUID id, AssociadoDTO associadoDTO) {
        return associadoRepository.findById(id)
            .map(existingAssociado -> {
                // Atualiza os campos do associado existente
                existingAssociado.setNomeCompleto(associadoDTO.getNomeCompleto());
                existingAssociado.setTelefone(associadoDTO.getTelefone());
                // Adicionar outras lógicas de atualização se necessário (CPF e email geralmente não mudam)
                return mapper.toDTO(associadoRepository.save(existingAssociado));
            })
            .orElseThrow(() -> new NotFoundException("Associado não encontrado"));
    }

    @Override
    public void delete(UUID id) {
        associadoRepository.findById(id)
            .ifPresentOrElse(
                associado -> associadoRepository.deleteById(id),
                () -> { throw new NotFoundException("Associado não encontrado"); }
            );
    }
}
