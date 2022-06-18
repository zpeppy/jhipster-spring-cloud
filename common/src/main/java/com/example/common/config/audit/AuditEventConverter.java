package com.example.common.config.audit;

import com.example.common.domain.PersistentAuditEvent;
import com.google.common.collect.Maps;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 审计事件处理
 *
 * @author peppy
 */
@Component
public class AuditEventConverter {

    /**
     * 转换审计对象
     *
     * @param persistentAuditEvents {@link PersistentAuditEvent} 列表
     * @return {@link AuditEvent} 列表
     */
    public List<AuditEvent> convertToAuditEvent(Iterable<PersistentAuditEvent> persistentAuditEvents) {
        if (persistentAuditEvents == null) {
            return Collections.emptyList();
        }
        List<AuditEvent> auditEvents = new ArrayList<>();
        for (PersistentAuditEvent persistentAuditEvent : persistentAuditEvents) {
            auditEvents.add(convertToAuditEvent(persistentAuditEvent));
        }
        return auditEvents;
    }

    /**
     * 转换审计对象
     *
     * @param persistentAuditEvent {@link PersistentAuditEvent} 对象
     * @return {@link AuditEvent} 对象
     */
    public AuditEvent convertToAuditEvent(PersistentAuditEvent persistentAuditEvent) {
        if (persistentAuditEvent == null) {
            return null;
        }
        return new AuditEvent(persistentAuditEvent.getAuditEventDate(), persistentAuditEvent.getPrincipal(),
                persistentAuditEvent.getAuditEventType(), convertDataToObjects(persistentAuditEvent.getData()));
    }

    /**
     * 转换对象
     *
     * @param data string 值
     * @return object 值
     */
    public Map<String, Object> convertDataToObjects(Map<String, String> data) {
        Map<String, Object> results = Maps.newHashMap();

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }

    /**
     * 转换对象
     *
     * @param data object 值
     * @return string 值
     */
    public Map<String, String> convertDataToStrings(Map<String, Object> data) {
        Map<String, String> results = Maps.newHashMap();

        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (entry.getValue() instanceof WebAuthenticationDetails) {
                    WebAuthenticationDetails authenticationDetails = (WebAuthenticationDetails) entry.getValue();
                    results.put("remoteAddress", authenticationDetails.getRemoteAddress());
                    results.put("sessionId", authenticationDetails.getSessionId());
                } else {
                    results.put(entry.getKey(), Objects.toString(entry.getValue()));
                }
            }
        }
        return results;
    }
}
