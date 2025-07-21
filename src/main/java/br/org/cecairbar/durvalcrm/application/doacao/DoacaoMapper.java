package br.org.cecairbar.durvalcrm.application.doacao;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import br.org.cecairbar.durvalcrm.domain.model.Doacao;

import java.util.List;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DoacaoMapper {
    
    @Mapping(source = "associado.id", target = "associadoId")
    @Mapping(source = "associado.nomeCompleto", target = "nomeAssociado")
    DoacaoDTO toDTO(Doacao doacao);
    
    @Mapping(source = "associadoId", target = "associado.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Doacao toEntity(DoacaoDTO dto);
    
    List<DoacaoDTO> toDTOList(List<Doacao> doacoes);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "associado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(DoacaoDTO dto, @MappingTarget Doacao doacao);
}