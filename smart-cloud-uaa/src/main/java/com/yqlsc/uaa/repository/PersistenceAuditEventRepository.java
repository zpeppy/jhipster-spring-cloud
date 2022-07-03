package com.yqlsc.uaa.repository;

import com.yqlsc.common.domain.PersistentAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link PersistentAuditEvent} entity.
 *
 * @author peppy
 */
public interface PersistenceAuditEventRepository extends JpaRepository<PersistentAuditEvent, Long>, JpaSpecificationExecutor<PersistentAuditEvent> {

    List<PersistentAuditEvent> findByPrincipal(String principal);

    List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfterAndAuditEventType(String principal, Instant after, String type);

    Page<PersistentAuditEvent> findAllByAuditEventDateBetween(Instant fromDate, Instant toDate, Pageable pageable);

    List<PersistentAuditEvent> findByAuditEventDateBefore(Instant before);
}
