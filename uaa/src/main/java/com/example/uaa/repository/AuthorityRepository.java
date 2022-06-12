package com.example.uaa.repository;

import com.example.uaa.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 *
 * @author peppy
 */
public interface AuthorityRepository extends JpaRepository<Authority, String>, JpaSpecificationExecutor<Authority> {

}
