package com.yqlsc.gateway.security.oauth2;

import com.yqlsc.common.config.oauth2.OAuth2Properties;
import io.github.jhipster.config.JHipsterProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * 客户端访问 uaa token 接口的实现类
 *
 * @author peppy
 */
@Component
public class UaaTokenEndpointClient extends OAuth2TokenEndpointClientAdapter implements OAuth2TokenEndpointClient {

    public UaaTokenEndpointClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                                  JHipsterProperties jHipsterProperties, OAuth2Properties oAuth2Properties) {
        super(restTemplate, jHipsterProperties, oAuth2Properties);
    }

    @Override
    protected void addAuthentication(HttpHeaders reqHeaders, MultiValueMap<String, String> formParams) {
        reqHeaders.add("Authorization", getAuthorizationHeader());
    }

    /**
     * 获取请求 uaa token 接口的 clientId 和 clientSecret
     *
     * @return clientId:clientSecret
     */
    protected String getAuthorizationHeader() {
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String authorization = clientId + ":" + clientSecret;
        return "Basic " + Base64Utils.encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
    }

}
