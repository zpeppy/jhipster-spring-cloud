package com.example.uaa.service.mapper;

import com.example.uaa.domain.Authority;
import com.example.uaa.domain.User;
import com.example.uaa.service.dto.UserDTO;
import com.google.common.collect.Sets;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author peppy
 */
@Mapper(componentModel = "spring")
public interface UserEntityMapper extends EntityMapper<UserDTO, User> {

    @Mapping(target = "authorities", expression = "java(authorities2Object(dto.getAuthorities()))")
    @Override
    User toEntity(UserDTO dto);

    @Mapping(target = "authorities", expression = "java(authorities2String(entity.getAuthorities()))")
    @Override
    UserDTO toDto(User entity);

    default Set<Authority> authorities2Object(Set<String> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return Sets.newHashSet();
        }
        return authorities.stream().map(authName -> {
            Authority authority = new Authority();
            authority.setName(authName);
            return authority;
        }).collect(Collectors.toSet());
    }

    default Set<String> authorities2String(Set<Authority> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return Sets.newHashSet();
        }
        return authorities.stream().map(Authority::getName).collect(Collectors.toSet());
    }

}
