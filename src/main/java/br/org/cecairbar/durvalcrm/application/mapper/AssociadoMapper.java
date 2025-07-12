package br.org.cecairbar.durvalcrm.application.mapper;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.AssociadoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "cdi") // Usar injeção de dependência do Quarkus
public interface AssociadoMapper {

    AssociadoMapper INSTANCE = Mappers.getMapper(AssociadoMapper.class);

    // Mapeamento de Entidade para DTO
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    // @Mapping(source = "criadoEm", target = "dataInscricao") // Removido pois dataInscricao não existe em AssociadoDTO
    AssociadoDTO toDTO(AssociadoEntity entity);

    // Mapeamento de DTO para Entidade
    @Mapping(source = "nomeCompleto", target = "nomeCompleto")
    @Mapping(target = "criadoEm", ignore = true) // Ignora campos gerenciados pelo BD
    @Mapping(target = "ativo", ignore = true)
    AssociadoEntity toEntity(AssociadoDTO dto);

    // --- SOLUÇÃO PARA O ERRO ---
    // Adicionar explicitamente o método de mapeamento de lista
    List<AssociadoDTO> toDTOList(List<AssociadoEntity> entities);
}
