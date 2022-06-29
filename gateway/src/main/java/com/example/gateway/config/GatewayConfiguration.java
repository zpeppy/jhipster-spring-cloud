package com.example.gateway.config;

import com.example.gateway.gateway.accesscontrol.AccessControlFilter;
import com.example.gateway.gateway.ratelimiting.RateLimitingFilter;
import com.example.gateway.gateway.responserewriting.SwaggerBasePathRewritingFilter;
import io.github.jhipster.config.JHipsterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gateway 过滤器配置
 *
 * @author peppy
 */
@Configuration
public class GatewayConfiguration {

    @Configuration
    public static class SwaggerBasePathRewritingConfiguration {

        @Bean
        public SwaggerBasePathRewritingFilter swaggerBasePathRewritingFilter() {
            return new SwaggerBasePathRewritingFilter();
        }
    }

    @Configuration
    public static class AccessControlFilterConfiguration {

        @Bean
        public AccessControlFilter accessControlFilter(RouteLocator routeLocator, JHipsterProperties jHipsterProperties) {
            return new AccessControlFilter(routeLocator, jHipsterProperties);
        }
    }

    /**
     * 配置限制每个用户的 API 调用次数的 Zuul 过滤器
     * <p>
     * 这使用 Bucket4J 来限制 API 调用，请参阅 {@link RateLimitingFilter}
     */
    @ConditionalOnProperty(prefix = "jhipster.gateway.rate-limiting", name = "enabled", havingValue = "true")
    @Configuration
    public static class RateLimitingConfiguration {

        private final JHipsterProperties jHipsterProperties;

        public RateLimitingConfiguration(JHipsterProperties jHipsterProperties) {
            this.jHipsterProperties = jHipsterProperties;
        }

        @Bean
        public RateLimitingFilter rateLimitingFilter() {
            return new RateLimitingFilter(jHipsterProperties);
        }
    }
}
