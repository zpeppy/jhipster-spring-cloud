package com.yqlsc.gateway.security.oauth2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 存储 access token 和 refresh token 的 cookies
 *
 * @author peppy
 */
class OAuth2Cookies {
    private Cookie accessTokenCookie;
    private Cookie refreshTokenCookie;

    public Cookie getAccessTokenCookie() {
        return accessTokenCookie;
    }

    public Cookie getRefreshTokenCookie() {
        return refreshTokenCookie;
    }

    public void setCookies(Cookie accessTokenCookie, Cookie refreshTokenCookie) {
        this.accessTokenCookie = accessTokenCookie;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    /**
     * 将 token 添加到响应中
     *
     * @param response 响应对象
     */
    void addCookiesTo(HttpServletResponse response) {
        response.addCookie(getAccessTokenCookie());
        response.addCookie(getRefreshTokenCookie());
    }
}
