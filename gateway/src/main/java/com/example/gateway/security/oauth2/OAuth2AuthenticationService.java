package com.example.gateway.security.oauth2;

import io.github.jhipster.security.PersistentTokenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 更新 cookies
 *
 * @author peppy
 */
public class OAuth2AuthenticationService {

    private final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationService.class);

    /**
     * 刷新 token 毫秒值
     */
    private static final long REFRESH_TOKEN_VALIDITY_MILLIS = 10000L;

    /**
     * 访问 OAuth2 token 接口
     */
    private final OAuth2TokenEndpointClient authorizationClient;

    /**
     * 处理 cookie
     */
    private final OAuth2CookieHelper cookieHelper;

    /**
     * 缓存刷新的 token
     */
    private final PersistentTokenCache<OAuth2Cookies> recentlyRefreshed;

    public OAuth2AuthenticationService(OAuth2TokenEndpointClient authorizationClient, OAuth2CookieHelper cookieHelper) {
        this.authorizationClient = authorizationClient;
        this.cookieHelper = cookieHelper;
        recentlyRefreshed = new PersistentTokenCache<>(REFRESH_TOKEN_VALIDITY_MILLIS);
    }

    /**
     * 用户名密码认证
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param params   请求参数: username, password, rememberMe
     * @return 返回 {@link OAuth2AccessToken} 实现类 {@link org.springframework.security.oauth2.common.DefaultOAuth2AccessToken} token 信息
     */
    public ResponseEntity<OAuth2AccessToken> authenticate(HttpServletRequest request, HttpServletResponse response,
                                                          Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            boolean rememberMe = Boolean.valueOf(params.get("rememberMe"));
            OAuth2AccessToken accessToken = authorizationClient.sendPasswordGrant(username, password);
            OAuth2Cookies cookies = new OAuth2Cookies();
            cookieHelper.createCookies(request, accessToken, rememberMe, cookies);
            cookies.addCookiesTo(response);
            if (log.isDebugEnabled()) {
                log.debug("successfully authenticated user {}", params.get("username"));
            }
            return ResponseEntity.ok(accessToken);
        } catch (HttpClientErrorException ex) {
            log.error("failed to get OAuth2 tokens from UAA", ex);
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    /**
     * 刷新 token
     *
     * @param request       请求对象
     * @param response      响应对象
     * @param refreshCookie 刷新 token 的 cookie
     * @return 处理后的请求对象
     */
    public HttpServletRequest refreshToken(HttpServletRequest request, HttpServletResponse response, Cookie refreshCookie) {
        //check if non-remember-me session has expired
        if (cookieHelper.isSessionExpired(refreshCookie)) {
            log.info("session has expired due to inactivity");
            //logout to clear cookies in browser
            logout(request, response);
            //don't include cookies downstream
            return stripTokens(request);
        }
        OAuth2Cookies cookies = getCachedCookies(refreshCookie.getValue());
        synchronized (cookies) {
            //check if we have a result from another thread already
            //no, we are first!
            if (cookies.getAccessTokenCookie() == null) {
                //send a refresh_token grant to UAA, getting new tokens
                String refreshCookieValue = OAuth2CookieHelper.getRefreshTokenValue(refreshCookie);
                OAuth2AccessToken accessToken = authorizationClient.sendRefreshGrant(refreshCookieValue);
                boolean rememberMe = OAuth2CookieHelper.isRememberMe(refreshCookie);
                cookieHelper.createCookies(request, accessToken, rememberMe, cookies);
                //add cookies to response to update browser
                cookies.addCookiesTo(response);
            } else {
                log.debug("reusing cached refresh_token grant");
            }
            //replace cookies in original request with new ones
            CookieCollection requestCookies = new CookieCollection(request.getCookies());
            requestCookies.add(cookies.getAccessTokenCookie());
            requestCookies.add(cookies.getRefreshTokenCookie());
            return new CookiesHttpServletRequestWrapper(request, requestCookies.toArray());
        }
    }

    /**
     * 从缓存中获取 token
     *
     * @param refreshTokenValue 需要刷新的 token
     * @return token cookies
     */
    private OAuth2Cookies getCachedCookies(String refreshTokenValue) {
        synchronized (recentlyRefreshed) {
            OAuth2Cookies ctx = recentlyRefreshed.get(refreshTokenValue);
            if (ctx == null) {
                ctx = new OAuth2Cookies();
                recentlyRefreshed.put(refreshTokenValue, ctx);
            }
            return ctx;
        }
    }

    /**
     * 退出清除缓存中的 token cookie
     *
     * @param httpServletRequest  请求对象
     * @param httpServletResponse 响应对象
     */
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        cookieHelper.clearCookies(httpServletRequest, httpServletResponse);
    }

    /**
     * 清除含 token 的 cookies
     *
     * @param httpServletRequest 请求对象
     * @return 清除 cookies 后的请求对象
     */
    public HttpServletRequest stripTokens(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = cookieHelper.stripCookies(httpServletRequest.getCookies());
        return new CookiesHttpServletRequestWrapper(httpServletRequest, cookies);
    }
}
