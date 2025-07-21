package br.org.cecairbar.durvalcrm.application.doacao;

import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.model.Doacao;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.AssociadoEntity;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.DoacaoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "cdi", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DoacaoEntityMapper {
    
    @Mapping(source = "associado", target = "associado", qualifiedByName = "associadoToEntity")
    DoacaoEntity toEntity(Doacao doacao);
    
    @Mapping(source = "associado", target = "associado", qualifiedByName = "entityToAssociado")
    Doacao toDomain(DoacaoEntity entity);
    
    List<Doacao> toDomainList(List<DoacaoEntity> entities);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "associado", ignore = true)
    void updateEntityFromDomain(Doacao doacao, @MappingTarget DoacaoEntity entity);
    
    @Named("associadoToEntity")
    default AssociadoEntity associadoToEntity(Associado associado) {
        if (associado == null || associado.getId() == null) {
            return null;
        }
        // Get reference to existing entity instead of creating a new one
        return AssociadoEntity.getEntityManager().getReference(AssociadoEntity.class, associado.getId());
    }
    
    @Named("entityToAssociado")
    default Associado entityToAssociado(AssociadoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Associado(
            entity.id,
            entity.nomeCompleto,
            entity.cpf,
            entity.email,
            entity.telefone,
            entity.ativo
        );
    }
}