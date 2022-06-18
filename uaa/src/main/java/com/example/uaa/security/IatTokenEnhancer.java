package com.example.uaa.security;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将 "iat" 放入 token 中, 表示 token 的创建时间, 用于 session 过期判断
 * 如果 "remember-me" 则可以忽略
 *
 * @author peppy
 */
@Component
public class IatTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        addClaims((DefaultOAuth2AccessToken) accessToken);
        return accessToken;
    }

    private void addClaims(DefaultOAuth2AccessToken accessToken) {
        DefaultOAuth2AccessToken token = accessToken;
        Map<String, Object> additionalInformation = token.getAdditionalInformation();
        if (additionalInformation.isEmpty()) {
            additionalInformation = new LinkedHashMap<>();
        }
        //add "iat" claim with current time in secs
        //this is used for an inactive session timeout
        additionalInformation.put("iat", (int) (System.currentTimeMillis() / 1000L));
        token.setAdditionalInformation(additionalInformation);
    }
}
