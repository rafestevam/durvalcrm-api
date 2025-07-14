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

@ApplicationScoped
public class AssociadoUseCaseImpl implements AssociadoUseCase {

    @Inject
    AssociadoRepository associadoRepository;

    @Inject
    AssociadoMapper mapper;

    @Override
    public List<AssociadoDTO> findAll(String search) {
        // Usando o método toDTOList que recebe List<Associado>
        List<Associado> associados = associadoRepository.findAll(search);
        return mapper.toDTOList(associados);
    }

    @Override
    public AssociadoDTO findById(UUID id) {
        return associadoRepository.findById(id)
                .map(mapper::toDTO)  // toDTO(Associado)
                .orElseThrow(() -> new NotFoundException("Associado não encontrado"));
    }

    @Override
    public AssociadoDTO create(AssociadoDTO associadoDTO) {
        // Validações de negócio
        associadoRepository.findByCpf(associadoDTO.getCpf())
                .ifPresent(a -> { 
                    throw new WebApplicationException("CPF já cadastrado", Response.Status.CONFLICT); 
                });
                
        associadoRepository.findByEmail(associadoDTO.getEmail())
                .ifPresent(a -> { 
                    throw new WebApplicationException("E-mail já cadastrado", Response.Status.CONFLICT); 
                });

        // Converte DTO para Domain, define como ativo e salva
        Associado associado = mapper.toDomain(associadoDTO);
        associado.setAtivo(true); // Garante que novos associados sejam ativos
        
        Associado savedAssociado = associadoRepository.save(associado);
        return mapper.toDTO(savedAssociado);
    }

    @Override
    public AssociadoDTO update(UUID id, AssociadoDTO associadoDTO) {
        return associadoRepository.findById(id)
        .map(existingAssociado -> {
            // Atualiza os campos do associado existente
            existingAssociado.setNomeCompleto(associadoDTO.getNomeCompleto());
            existingAssociado.setTelefone(associadoDTO.getTelefone());
            
            // Permite atualização de email se fornecido
            if (associadoDTO.getEmail() != null && !associadoDTO.getEmail().isEmpty()) {
                // Verifica se o novo email já existe em outro registro
                associadoRepository.findByEmail(associadoDTO.getEmail())
                    .ifPresent(existingWithEmail -> {
                        // Se o email já existe em outro associado (não o atual), lança exceção
                        if (!existingWithEmail.getId().equals(id)) {
                            throw new WebApplicationException("E-mail já cadastrado", Response.Status.CONFLICT);
                        }
                    });
                existingAssociado.setEmail(associadoDTO.getEmail());
            }
            
            // CPF geralmente não deve ser alterado após criação
            // Mas se necessário, pode ser adicionado aqui com validação similar
            
            Associado updatedAssociado = associadoRepository.save(existingAssociado);
            return mapper.toDTO(updatedAssociado);
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