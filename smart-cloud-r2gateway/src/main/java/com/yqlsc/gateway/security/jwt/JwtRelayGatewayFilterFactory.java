package com.yqlsc.gateway.security.jwt;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Gateway 过滤器配置
 *
 * @author peppy
 */
@Component
public class JwtRelayGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final TokenProvider tokenProvider;

    public JwtRelayGatewayFilterFactory(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String token = this.extractJwtToken(exchange.getRequest());
            if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header(JwtFilter.AUTHORIZATION_HEADER, JwtFilter.BEARER + token)
                        .build();

                return chain.filter(exchange.mutate().request(request).build());
            }
            return chain.filter(exchange);
        };
    }

    /**
     * 解析出 token
     *
     * @param request request
     * @return token
     */
    private String extractJwtToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(JwtFilter.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtFilter.BEARER)) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Invalid token in Authorization header");
    }
}
