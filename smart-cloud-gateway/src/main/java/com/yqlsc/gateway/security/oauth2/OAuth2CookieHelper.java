package com.yqlsc.gateway.security.oauth2;

import com.yqlsc.common.config.oauth2.OAuth2Properties;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.http.conn.util.InetAddressUtils.isIPv4Address;
import static org.apache.http.conn.util.InetAddressUtils.isIPv6Address;

/**
 * OAuth2 cookie 工具类
 *
 * @author peppy
 */
public class OAuth2CookieHelper {
    /**
     * access token cookie 名称
     */
    public static final String ACCESS_TOKEN_COOKIE = OAuth2AccessToken.ACCESS_TOKEN;
    /**
     * 在 remember=true 情况下的 refresh token cookie 名称
     */
    public static final String REFRESH_TOKEN_COOKIE = OAuth2AccessToken.REFRESH_TOKEN;
    /**
     * 在 remember=false 情况下的 session token cookie 名称
     */
    public static final String SESSION_TOKEN_COOKIE = "session_token";
    /**
     * cookies 列表
     */
    private static final List<String> COOKIE_NAMES = Arrays.asList(ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE,
            SESSION_TOKEN_COOKIE);
    /**
     * 刷新 token 过期时间(单位: 秒)
     */
    private static final long REFRESH_TOKEN_EXPIRATION_WINDOW_SECS = 3L;

    /**
     * 公共后缀匹配
     */
    PublicSuffixMatcher suffixMatcher;

    private final Logger log = LoggerFactory.getLogger(OAuth2CookieHelper.class);

    private OAuth2Properties oAuth2Properties;

    /**
     * 解析 jwt 信息
     */
    private JsonParser jsonParser = JsonParserFactory.getJsonParser();

    public OAuth2CookieHelper(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;

        // Alternatively, always get an up-to-date list by passing an URL
        this.suffixMatcher = PublicSuffixMatcherLoader.getDefault();
    }

    public static Cookie getAccessTokenCookie(HttpServletRequest request) {
        return getCookie(request, ACCESS_TOKEN_COOKIE);
    }

    public static Cookie getRefreshTokenCookie(HttpServletRequest request) {
        Cookie cookie = getCookie(request, REFRESH_TOKEN_COOKIE);
        if (cookie == null) {
            cookie = getCookie(request, SESSION_TOKEN_COOKIE);
        }
        return cookie;
    }


    /**
     * 按名称获取 cookie
     *
     * @param request    请求对象
     * @param cookieName cookie 名称(区分大小写)
     * @return cookie 对象可能为 {@code null}
     */
    private static Cookie getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    String value = cookie.getValue();
                    if (StringUtils.hasText(value)) {
                        return cookie;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 创建 cookie
     *
     * @param request     请求对象
     * @param accessToken access token
     * @param rememberMe  remember
     * @param result      缓存的 cookie
     */
    public void createCookies(HttpServletRequest request, OAuth2AccessToken accessToken, boolean rememberMe,
                              OAuth2Cookies result) {
        String domain = getCookieDomain(request);
        log.debug("creating cookies for domain {}", domain);
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken.getValue());
        setCookieProperties(accessTokenCookie, request.isSecure(), domain);
        log.debug("created access token cookie '{}'", accessTokenCookie.getName());

        OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
        Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken, rememberMe);
        setCookieProperties(refreshTokenCookie, request.isSecure(), domain);
        log.debug("created refresh token cookie '{}', age: {}", refreshTokenCookie.getName(), refreshTokenCookie
                .getMaxAge());

        result.setCookies(accessTokenCookie, refreshTokenCookie);
    }

    /**
     * 创建 refresh token
     *
     * @param refreshToken refresh token
     * @param refreshToken rememberMe 是否为记住我选项
     */
    private Cookie createRefreshTokenCookie(OAuth2RefreshToken refreshToken, boolean rememberMe) {
        int maxAge = -1;
        String name = SESSION_TOKEN_COOKIE;
        String value = refreshToken.getValue();
        if (rememberMe) {
            name = REFRESH_TOKEN_COOKIE;
            //get expiration in seconds from the token's "exp" claim
            Integer exp = getClaim(refreshToken.getValue(), AccessTokenConverter.EXP, Integer.class);
            if (exp != null) {
                int now = (int) (System.currentTimeMillis() / 1000L);
                maxAge = exp - now;
                log.debug("refresh token valid for another {} secs", maxAge);
                //let cookie expire a bit earlier than the token to avoid race conditions
                maxAge -= REFRESH_TOKEN_EXPIRATION_WINDOW_SECS;
            }
        }
        Cookie refreshTokenCookie = new Cookie(name, value);
        refreshTokenCookie.setMaxAge(maxAge);
        return refreshTokenCookie;
    }

    /**
     * 判断是否记住我选项
     *
     * @param refreshTokenCookie 保存的 refresh token cookie
     * @return 是否记住我选项
     */
    public static boolean isRememberMe(Cookie refreshTokenCookie) {
        return refreshTokenCookie.getName().equals(REFRESH_TOKEN_COOKIE);
    }

    /**
     * 从 cookie 中获取 refresh token
     *
     * @param refreshCookie cookie
     * @return 从 cookie 获取的 jwt refresh token
     */
    public static String getRefreshTokenValue(Cookie refreshCookie) {
        String value = refreshCookie.getValue();
        int i = value.indexOf('|');
        if (i > 0) {
            return value.substring(i + 1);
        }
        return value;
    }

    /**
     * 检查 cookie 中 refresh token 是否过期
     *
     * @param refreshCookie cookie
     * @return 是否过期
     */
    public boolean isSessionExpired(Cookie refreshCookie) {
        //no session expiration for "remember me"
        if (isRememberMe(refreshCookie)) {
            return false;
        }
        //read non-remember-me session length in secs
        int validity = oAuth2Properties.getWebClientConfiguration().getSessionTimeoutInSeconds();
        //no session expiration configured
        if (validity < 0) {
            return false;
        }
        Integer iat = getClaim(refreshCookie.getValue(), "iat", Integer.class);
        //token creating timestamp in secs is missing, session does not expire
        if (iat == null) {
            return false;
        }
        int now = (int) (System.currentTimeMillis() / 1000L);
        int sessionDuration = now - iat;
        log.debug("session duration {} secs, will timeout at {}", sessionDuration, validity);
        //session has expired
        return sessionDuration > validity;
    }

    /**
     * 从 refresh token 中获取明文信息
     *
     * @param refreshToken refresh token
     * @param claimName    json key 值
     * @param clazz        结果类型
     * @return 明文信息
     * @throws InvalidTokenException 如果找不到则表示该 token 无效
     */
    @SuppressWarnings("unchecked")
    private <T> T getClaim(String refreshToken, String claimName, Class<T> clazz) {
        Jwt jwt = JwtHelper.decode(refreshToken);
        String claims = jwt.getClaims();
        Map<String, Object> claimsMap = jsonParser.parseMap(claims);
        Object claimValue = claimsMap.get(claimName);
        if (claimValue == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(claimValue.getClass())) {
            throw new InvalidTokenException("claim is not of expected type: " + claimName);
        }
        return (T) claimValue;
    }

    /**
     * 设置 cookie 属性
     *
     * @param cookie   cookie
     * @param isSecure 是否为安全请求
     * @param domain   有效的域名
     */
    private void setCookieProperties(Cookie cookie, boolean isSecure, String domain) {
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        //if the request comes per HTTPS set the secure option on the cookie
        cookie.setSecure(isSecure);
        if (domain != null) {
            cookie.setDomain(domain);
        }
    }

    /**
     * 清除所有 cookies
     *
     * @param httpServletRequest  请求对象
     * @param httpServletResponse 响应对象
     */
    public void clearCookies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String domain = getCookieDomain(httpServletRequest);
        for (String cookieName : COOKIE_NAMES) {
            clearCookie(httpServletRequest, httpServletResponse, domain, cookieName);
        }
    }

    private void clearCookie(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                             String domain, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        setCookieProperties(cookie, httpServletRequest.isSecure(), domain);
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);
        log.debug("clearing cookie {}", cookie.getName());
    }

    /**
     * 从请求中获取顶级域名
     *
     * @param request 请求对象
     * @return 顶级域名
     */
    private String getCookieDomain(HttpServletRequest request) {
        String domain = oAuth2Properties.getWebClientConfiguration().getCookieDomain();
        if (domain != null) {
            return domain;
        }
        // if not explicitly defined, use top-level domain
        domain = request.getServerName().toLowerCase();
        // strip off leading www.
        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }
        // if it isn't an IP address
        if (!isIPv4Address(domain) && !isIPv6Address(domain)) {
            // strip off private subdomains, leaving public TLD only
            String suffix = suffixMatcher.getDomainRoot(domain);
            if (suffix != null && !suffix.equals(domain)) {
                // preserve leading dot
                return "." + suffix;
            }
        }
        // no top-level domain, stick with default domain
        return null;
    }

    /**
     * 清除包含所有 token 的 cookies
     *
     * @param cookies cookies
     * @return 清除后的 cookies
     */
    Cookie[] stripCookies(Cookie[] cookies) {
        CookieCollection cc = new CookieCollection(cookies);
        if (cc.removeAll(COOKIE_NAMES)) {
            return cc.toArray();
        }
        return cookies;
    }
}
