package com.yqlsc.uaa.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Contract for a generic dto to entity mapper.
 * <p>
 * {@code @Mapper(componentModel = "spring", uses = {})
 * public interface TestMapper extends EntityMapper<TestDTO, Test> {}
 * }
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 * @author peppy
 */
public interface EntityMapper<D, E> {
    E toEntity(D dto);

    D toDto(E entity);

    List<E> toEntity(List<D> dtoList);

    List<D> toDto(List<E> entityList);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
}
