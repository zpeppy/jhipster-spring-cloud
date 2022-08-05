package com.example.gateway.repository;

import com.example.gateway.domain.Authority;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * Spring Data R2DBC repository for the {@link Authority} entity.
 *
 * @author peppy
 */
public interface AuthorityRepository extends R2dbcRepository<Authority, String> {

}
