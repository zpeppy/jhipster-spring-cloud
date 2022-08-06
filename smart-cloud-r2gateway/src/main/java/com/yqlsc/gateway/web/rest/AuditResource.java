package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.service.AuditEventService;
import io.github.jhipster.web.util.PaginationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * @author peppy
 */
@Api(value = "审计信息", tags = "审计信息")
@RestController
@RequestMapping("/management/audits")
public class AuditResource {

    private final AuditEventService auditEventService;

    public AuditResource(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @ApiOperation(value = "查询审计信息", tags = "审计信息")
    @GetMapping("/events")
    public Mono<ResponseEntity<Flux<AuditEvent>>> getAll(ServerHttpRequest request, Pageable pageable) {
        return auditEventService.count()
                .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
                .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
                .map(headers -> ResponseEntity.ok().headers(headers).body(auditEventService.findAll(pageable)));
    }

    @ApiOperation(value = "通过时间区间查询审计信息", tags = "审计信息")
    @GetMapping(value = "/events", params = {"fromDate", "toDate"})
    public Mono<ResponseEntity<Flux<AuditEvent>>> getByDates(@RequestParam(value = "fromDate") LocalDate fromDate,
                                                             @RequestParam(value = "toDate") LocalDate toDate,
                                                             ServerHttpRequest request, Pageable pageable) {

        Instant from = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to = toDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();

        Flux<AuditEvent> events = auditEventService.findByDates(from, to, pageable);
        return auditEventService.countByDates(from, to)
                .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
                .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
                .map(headers -> ResponseEntity.ok().headers(headers).body(events));
    }

    @ApiOperation(value = "通过 ID 查询审计信息", tags = "审计信息")
    @GetMapping("/{id:.+}")
    public Mono<AuditEvent> get(@PathVariable Long id) {
        return auditEventService.find(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}
