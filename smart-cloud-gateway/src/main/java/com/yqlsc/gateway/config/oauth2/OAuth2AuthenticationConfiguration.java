package com.yqlsc.gateway.config.oauth2;

import com.yqlsc.common.config.oauth2.OAuth2Properties;
import com.yqlsc.gateway.security.oauth2.CookieTokenExtractor;
import com.yqlsc.gateway.security.oauth2.OAuth2AuthenticationService;
import com.yqlsc.gateway.security.oauth2.OAuth2CookieHelper;
import com.yqlsc.gateway.security.oauth2.OAuth2TokenEndpointClient;
import com.yqlsc.gateway.web.filter.RefreshTokenFilterConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * 资源服务器配置
 *
 * @author peppy
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class OAuth2AuthenticationConfiguration extends ResourceServerConfigurerAdapter {
    private final OAuth2Properties oAuth2Properties;
    private final OAuth2TokenEndpointClient tokenEndpointClient;
    private final TokenStore tokenStore;

    public OAuth2AuthenticationConfiguration(OAuth2Properties oAuth2Properties, OAuth2TokenEndpointClient tokenEndpointClient, TokenStore tokenStore) {
        this.oAuth2Properties = oAuth2Properties;
        this.tokenEndpointClient = tokenEndpointClient;
        this.tokenStore = tokenStore;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/auth/login").permitAll()
                .antMatchers("/auth/logout").authenticated()
                .and()
                .apply(refreshTokenSecurityConfigurerAdapter());
    }

    /**
     * 获取 {@link org.springframework.security.config.annotation.SecurityConfigurerAdapter} 刷新 token
     */
    private RefreshTokenFilterConfigurer refreshTokenSecurityConfigurerAdapter() {
        return new RefreshTokenFilterConfigurer(uaaAuthenticationService(), tokenStore);
    }

    @Bean
    public OAuth2CookieHelper cookieHelper() {
        return new OAuth2CookieHelper(oAuth2Properties);
    }

    @Bean
    public OAuth2AuthenticationService uaaAuthenticationService() {
        return new OAuth2AuthenticationService(tokenEndpointClient, cookieHelper());
    }

    /**
     * 资源服务器配置 {@link TokenExtractor}
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenExtractor(tokenExtractor());
    }

    /**
     * 注入从 cookie 中提取 token 的工具类
     *
     * @return {@link CookieTokenExtractor} 组件
     */
    @Bean
    public TokenExtractor tokenExtractor() {
        return new CookieTokenExtractor();
    }

}
