package com.yqlsc.common.security.oauth2;

import com.google.common.collect.Maps;
import com.yqlsc.common.config.oauth2.OAuth2Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * 从 uaa 获取公钥实现类
 *
 * @author peppy
 */
@Component
public class UaaSignatureVerifierClient implements OAuth2SignatureVerifierClient {
    private final Logger log = LoggerFactory.getLogger(UaaSignatureVerifierClient.class);

    private final RestTemplate restTemplate;
    protected final OAuth2Properties oAuth2Properties;

    public UaaSignatureVerifierClient(DiscoveryClient discoveryClient, @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                                      OAuth2Properties oAuth2Properties) {
        this.restTemplate = restTemplate;
        this.oAuth2Properties = oAuth2Properties;
        // Load available UAA servers
        discoveryClient.getServices();
    }

    /**
     * 从 uaa 获取公钥
     *
     * @return 用于验证 jwt token 的公钥, 可能为空
     */
    @Override
    public SignatureVerifier getSignatureVerifier() {
        try {
            HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
            String key = (String) Optional.ofNullable(
                    restTemplate.exchange(getPublicKeyEndpoint(), HttpMethod.GET, request, Map.class).getBody()
            ).orElseGet(Maps::newHashMap).get("value");
            return new RsaVerifier(key);
        } catch (IllegalStateException ex) {
            log.warn("could not contact UAA to get public key");
            return null;
        }
    }

    /**
     * 获取配置的 uaa 公钥地址
     *
     * @return 公钥地址
     */
    private String getPublicKeyEndpoint() {
        String tokenEndpointUrl = oAuth2Properties.getSignatureVerification().getPublicKeyEndpointUri();
        if (tokenEndpointUrl == null) {
            throw new InvalidClientException("no token endpoint configured in application properties");
        }
        return tokenEndpointUrl;
    }
}
