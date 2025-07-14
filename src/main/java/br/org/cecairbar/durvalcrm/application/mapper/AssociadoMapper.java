package br.org.cecairbar.durvalcrm.application.mapper;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;
import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.AssociadoEntity;
import jakarta.enterprise.context.ApplicationScoped;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "cdi")
@ApplicationScoped
public interface AssociadoMapper {

    // =============================
    // MAPEAMENTOS DOMAIN <-> DTO (Principais)
    // =============================
    
    // Mapeamento de Domain para DTO
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    AssociadoDTO toDTO(Associado domain);

    // Mapeamento de DTO para Domain
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    @Mapping(target = "ativo", ignore = true) // Será definido na lógica de negócio
    Associado toDomain(AssociadoDTO dto);

    // Lista de Domain para lista de DTOs - MÉTODO PRINCIPAL PARA O USE CASE
    List<AssociadoDTO> toDTOList(List<Associado> domains);

    // =============================
    // MAPEAMENTOS ENTITY <-> DOMAIN (Para Repository)
    // =============================
    
    // Mapeamento de Entity para Domain
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    Associado toDomain(AssociadoEntity entity);

    // Mapeamento de Domain para Entity
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    @Mapping(target = "criadoEm", ignore = true)
    AssociadoEntity toEntity(Associado domain);

    // Lista de Entities para lista de Domains
    List<Associado> toDomainList(List<AssociadoEntity> entities);

    // =============================
    // MAPEAMENTOS DIRETOS ENTITY <-> DTO (Se necessário)
    // =============================
    
    // Mapeamento de Entidade para DTO
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    AssociadoDTO entityToDTO(AssociadoEntity entity);

    // Mapeamento de DTO para Entidade
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    AssociadoEntity dtoToEntity(AssociadoDTO dto);

    // Lista de Entidades para lista de DTOs
    List<AssociadoDTO> entityToDTOList(List<AssociadoEntity> entities);
}