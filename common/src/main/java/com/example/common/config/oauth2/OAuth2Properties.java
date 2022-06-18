package com.example.common.config.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * OAuth2 属性配置
 *
 * @author peppy
 */
@Primary
@Component
@ConfigurationProperties(prefix = "oauth2", ignoreUnknownFields = false)
public class OAuth2Properties {
    private WebClientConfiguration webClientConfiguration = new WebClientConfiguration();

    private SignatureVerification signatureVerification = new SignatureVerification();

    public WebClientConfiguration getWebClientConfiguration() {
        return webClientConfiguration;
    }

    public SignatureVerification getSignatureVerification() {
        return signatureVerification;
    }

    public static class WebClientConfiguration {
        private String clientId = "web_app";
        private String secret = "changeit";
        /**
         * session 超时时间(单位: 秒)
         */
        private int sessionTimeoutInSeconds = 1800;
        /**
         * cookie 应用的域名
         */
        private String cookieDomain;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getSessionTimeoutInSeconds() {
            return sessionTimeoutInSeconds;
        }

        public void setSessionTimeoutInSeconds(int sessionTimeoutInSeconds) {
            this.sessionTimeoutInSeconds = sessionTimeoutInSeconds;
        }

        public String getCookieDomain() {
            return cookieDomain;
        }

        public void setCookieDomain(String cookieDomain) {
            this.cookieDomain = cookieDomain;
        }
    }

    public static class SignatureVerification {
        /**
         * 公钥更新频率(单位: 毫秒)
         */
        private long publicKeyRefreshRateLimit = 10 * 1000L;
        /**
         * 公钥有效时长(单位: 毫秒)
         */
        private long ttl = 24 * 60 * 60 * 1000L;
        /**
         * 获取 uaa 公钥地址
         */
        private String publicKeyEndpointUri = "http://uaa/oauth/token_key";

        public long getPublicKeyRefreshRateLimit() {
            return publicKeyRefreshRateLimit;
        }

        public void setPublicKeyRefreshRateLimit(long publicKeyRefreshRateLimit) {
            this.publicKeyRefreshRateLimit = publicKeyRefreshRateLimit;
        }

        public long getTtl() {
            return ttl;
        }

        public void setTtl(long ttl) {
            this.ttl = ttl;
        }

        public String getPublicKeyEndpointUri() {
            return publicKeyEndpointUri;
        }

        public void setPublicKeyEndpointUri(String publicKeyEndpointUri) {
            this.publicKeyEndpointUri = publicKeyEndpointUri;
        }
    }
}
