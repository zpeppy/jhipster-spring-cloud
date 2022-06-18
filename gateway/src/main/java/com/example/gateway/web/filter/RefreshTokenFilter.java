package com.example.gateway.web.filter;

import com.example.gateway.security.oauth2.OAuth2AuthenticationService;
import com.example.gateway.security.oauth2.OAuth2CookieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 在 token 过期前, 刷新 access token 的过滤器
 *
 * @author peppy
 */
public class RefreshTokenFilter extends GenericFilterBean {
    /**
     * 刷新时间(单位: 秒)
     */
    private static final int REFRESH_WINDOW_SECS = 30;

    private final Logger log = LoggerFactory.getLogger(RefreshTokenFilter.class);

    private final OAuth2AuthenticationService authenticationService;
    private final TokenStore tokenStore;

    public RefreshTokenFilter(OAuth2AuthenticationService authenticationService, TokenStore tokenStore) {
        this.authenticationService = authenticationService;
        this.tokenStore = tokenStore;
    }

    /**
     * 如果 token 过期则刷新
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        try {
            httpServletRequest = refreshTokensIfExpiring(httpServletRequest, httpServletResponse);
        } catch (ClientAuthenticationException ex) {
            log.warn("Security exception: could not refresh tokens", ex);
            httpServletRequest = authenticationService.stripTokens(httpServletRequest);
        }
        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    /**
     * 如果 access token 和 refresh token 过期则刷新
     *
     * @param httpServletRequest  请求对象
     * @param httpServletResponse 响应对象
     * @return 处理后的请求对象
     * @throws InvalidTokenException 如果刷新失败则抛异常
     */
    public HttpServletRequest refreshTokensIfExpiring(HttpServletRequest httpServletRequest, HttpServletResponse
            httpServletResponse) {
        HttpServletRequest newHttpServletRequest = httpServletRequest;
        //get access token from cookie
        Cookie accessTokenCookie = OAuth2CookieHelper.getAccessTokenCookie(httpServletRequest);
        //we either have no access token, or it is expired, or it is about to expire
        if (mustRefreshToken(accessTokenCookie)) {
            //get the refresh token cookie and, if present, request new tokens
            Cookie refreshCookie = OAuth2CookieHelper.getRefreshTokenCookie(httpServletRequest);
            if (refreshCookie != null) {
                try {
                    newHttpServletRequest = authenticationService.refreshToken(httpServletRequest, httpServletResponse, refreshCookie);
                } catch (HttpClientErrorException ex) {
                    throw new UnauthorizedClientException("could not refresh OAuth2 token", ex);
                }
            } else if (accessTokenCookie != null) {
                log.warn("access token found, but no refresh token, stripping them all");
                OAuth2AccessToken token = tokenStore.readAccessToken(accessTokenCookie.getValue());
                if (token.isExpired() || token.getExpiresIn() < REFRESH_WINDOW_SECS) {
                    throw new InvalidTokenException("access token has expired or expires within " + REFRESH_WINDOW_SECS + " seconds, but there's no refresh token");
                }
            }
        }
        return newHttpServletRequest;
    }

    /**
     * 校验是否必须刷新 token
     *
     * @param accessTokenCookie 包含 access token 的 cookie
     * @return 是否必须刷新
     */
    private boolean mustRefreshToken(Cookie accessTokenCookie) {
        if (accessTokenCookie == null) {
            return true;
        }
        OAuth2AccessToken token = tokenStore.readAccessToken(accessTokenCookie.getValue());
        //check if token is expired or about to expire
        if (token.isExpired() || token.getExpiresIn() < REFRESH_WINDOW_SECS) {
            return true;
        }
        //access token is still fine
        return false;
    }
}
