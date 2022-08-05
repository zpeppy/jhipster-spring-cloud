package com.example.gateway.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @author peppy
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Data
@Table("gateway_persistent_audit_event")
public class PersistentAuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("event_id")
    private Long id;

    @NotNull
    private String principal;

    @Column("event_date")
    private Instant auditEventDate;

    @Column("event_type")
    private String auditEventType;

    @Transient
    private Map<String, String> data = new HashMap<>();

}
