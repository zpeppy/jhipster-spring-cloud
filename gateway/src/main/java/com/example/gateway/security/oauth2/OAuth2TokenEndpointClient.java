package com.example.gateway.security.oauth2;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * 调用 uaa 认证或者刷新 token 的接口
 *
 * @author peppy
 */
public interface OAuth2TokenEndpointClient {
    /**
     * 通过用户名密码模式认证
     *
     * @param username 用户名
     * @param password 密码
     * @return access token 和 refresh token
     * @throws org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException 调用 uaa 异常
     */
    OAuth2AccessToken sendPasswordGrant(String username, String password);

    /**
     * 向 uaa 发送 refresh_token 刷新令牌
     *
     * @param refreshTokenValue old refresh token
     * @return 新的 access token 和 refresh token
     * @throws org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException 调用 uaa 异常
     */
    OAuth2AccessToken sendRefreshGrant(String refreshTokenValue);
}
