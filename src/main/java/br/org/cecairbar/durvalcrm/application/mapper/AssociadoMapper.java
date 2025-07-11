package br.org.cecairbar.durvalcrm.application.mapper;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;
import br.org.cecairbar.durvalcrm.domain.model.Associado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Usando MapStruct para o mapeamento
@Mapper(componentModel = "jakarta")
public interface AssociadoMapper {
    AssociadoDTO toDTO(Associado associado);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    Associado toDomain(AssociadoDTO dto);
}
