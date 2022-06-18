package com.example.gateway.security.oauth2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 存储修改后的 cookies
 *
 * @author peppy
 */
class CookiesHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final Cookie[] cookies;

    public CookiesHttpServletRequestWrapper(HttpServletRequest request, Cookie[] cookies) {
        super(request);
        this.cookies = cookies;
    }

    /**
     * 获取修改后的 cookies
     *
     * @return cookies
     */
    @Override
    public Cookie[] getCookies() {
        return cookies;
    }
}
