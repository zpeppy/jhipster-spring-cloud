package com.example.gateway.web.filter;

import com.example.gateway.security.oauth2.OAuth2AuthenticationService;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.DefaultSecurityFilterChain;

/**
 * 配置刷新令牌的过滤器
 *
 * @author peppy
 * @see RefreshTokenFilter
 */
public class RefreshTokenFilterConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private OAuth2AuthenticationService authenticationService;
    private final TokenStore tokenStore;

    public RefreshTokenFilterConfigurer(OAuth2AuthenticationService authenticationService, TokenStore tokenStore) {
        this.authenticationService = authenticationService;
        this.tokenStore = tokenStore;
    }

    /**
     * 将 {@link RefreshTokenFilter} 过滤器添加到 servlet 过滤器链中
     */
    @Override
    public void configure(HttpSecurity http) {
        RefreshTokenFilter customFilter = new RefreshTokenFilter(authenticationService, tokenStore);
        http.addFilterBefore(customFilter, OAuth2AuthenticationProcessingFilter.class);
    }
}
