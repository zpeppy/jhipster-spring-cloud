package com.example.gateway.security.jwt;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.example.gateway.security.jwt.JwtFilter.AUTHORIZATION_HEADER;
import static com.example.gateway.security.jwt.JwtFilter.BEARER;

/**
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
                    .header(AUTHORIZATION_HEADER, BEARER + token)
                    .build();

                return chain.filter(exchange.mutate().request(request).build());
            }
            return chain.filter(exchange);
        };
    }

    private String extractJwtToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Invalid token in Authorization header");
    }
}
