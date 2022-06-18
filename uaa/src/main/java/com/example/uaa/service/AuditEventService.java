package com.example.uaa.service;

import com.example.common.config.audit.AuditEventConverter;
import com.example.uaa.repository.PersistenceAuditEventRepository;
import io.github.jhipster.config.JHipsterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * 审计处理
 *
 * @author peppy
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AuditEventService {

    private final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    private final JHipsterProperties jHipsterProperties;

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final AuditEventConverter auditEventConverter;

    public AuditEventService(PersistenceAuditEventRepository persistenceAuditEventRepository,
                             AuditEventConverter auditEventConverter, JHipsterProperties jhipsterProperties) {

        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
        this.jHipsterProperties = jhipsterProperties;
    }

    /**
     * 每天 12 点定时删除 30 天前的审计信息
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void removeOldAuditEvents() {
        persistenceAuditEventRepository
                .findByAuditEventDateBefore(Instant.now().minus(jHipsterProperties.getAuditEvents().getRetentionPeriod(), ChronoUnit.DAYS))
                .forEach(auditEvent -> {
                    log.debug("Deleting audit data {}", auditEvent);
                    persistenceAuditEventRepository.delete(auditEvent);
                });
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> findAll(Pageable pageable) {
        return persistenceAuditEventRepository.findAll(pageable)
                .map(auditEventConverter::convertToAuditEvent);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> findByDates(Instant fromDate, Instant toDate, Pageable pageable) {
        return persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate, toDate, pageable)
                .map(auditEventConverter::convertToAuditEvent);
    }

    @Transactional(readOnly = true)
    public Optional<AuditEvent> find(Long id) {
        return persistenceAuditEventRepository.findById(id)
                .map(auditEventConverter::convertToAuditEvent);
    }
}
