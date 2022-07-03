package com.yqlsc.gateway.security.oauth2;

import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 从 cookie 中提取 token
 *
 * @author peppy
 */
public class CookieTokenExtractor extends BearerTokenExtractor {
    /**
     * 从 request 中获取 jwt access token
     *
     * @param request request 对象
     * @return jwt access token 或者 {@code null}
     */
    @Override
    protected String extractToken(HttpServletRequest request) {
        String result;
        Cookie accessTokenCookie = OAuth2CookieHelper.getAccessTokenCookie(request);
        if (accessTokenCookie != null) {
            result = accessTokenCookie.getValue();
        } else {
            result = super.extractToken(request);
        }
        return result;
    }

}
