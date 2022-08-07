package com.example.gateway.security.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.gateway.config.oauth2.OAuth2Properties;
import com.example.gateway.web.rest.errors.InvalidClientException;
import com.example.gateway.web.rest.vm.LoginVM;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author peppy
 */
@Slf4j
@ConditionalOnProperty(prefix = "application", value = "useUaa", havingValue = "true")
@Component
public class TokenUaaProvider extends TokenProvider {

    public static final String AUTHORIZATION = "Authorization";

    public static final String BASIC = "Basic ";

    private final JHipsterProperties jHipsterProperties;

    @Resource
    private OAuth2Properties oAuth2Properties;

    @Resource(name = "loadBalancedRestTemplate")
    private RestTemplate restTemplate;

    private SignatureVerifier verifier;

    public TokenUaaProvider(JHipsterProperties jHipsterProperties) {
        super(jHipsterProperties);
        this.jHipsterProperties = jHipsterProperties;
    }

    @PostConstruct
    @Override
    public void init() {
        try {
            HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
            String key = (String) Optional.ofNullable(
                restTemplate.exchange(getPublicKeyEndpoint(), HttpMethod.GET, request, Map.class).getBody()
            ).orElseGet(Maps::newHashMap).get("value");
            verifier = new RsaVerifier(key);
        } catch (IllegalStateException ex) {
            log.warn("could not contact UAA to get public key");
        }
    }

    /**
     * 创建 jwt access_token
     *
     * @param authentication 认证信息
     * @param loginVm        登录信息
     * @return token
     */
    @Override
    public String createToken(Authentication authentication, LoginVM loginVm) {
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.set("username", loginVm.getUsername());
        formParams.set("password", loginVm.getPassword());
        formParams.set("grant_type", "password");
        addAuthentication(reqHeaders, formParams);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, reqHeaders);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(getTokenEndpoint(), entity, Map.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            log.debug("failed to authenticate user with OAuth2 token endpoint, status: {}", responseEntity.getStatusCodeValue());
            throw new HttpClientErrorException(responseEntity.getStatusCode());
        }
        // 可把 access_token 缓存在 redis 中 TODO
        return Optional.ofNullable(responseEntity.getBody())
            .map(map ->
                map.get("access_token")
            ).orElseThrow(() -> new BadCredentialsException("Invalid credentials"))
            .toString();
    }

    /**
     * 获取认证信息
     *
     * @param token access_token
     * @return 认证信息
     */
    @Override
    public Authentication getAuthentication(String token) {
        Jwt jwt = JwtHelper.decode(token);
        String claims = jwt.getClaims();

        JSONObject jsonObject = JSON.parseObject(claims);
        String username = jsonObject.getString("user_name");

        List<GrantedAuthority> auths = Lists.newArrayList();
        Optional.ofNullable(jsonObject.getJSONArray("authorities"))
            .ifPresent(authorities ->
                authorities.forEach(authority ->
                    auths.add(new SimpleGrantedAuthority(String.valueOf(authority)))
                )
            );
        User user = new User(username, StringUtils.EMPTY, auths);
        return new UsernamePasswordAuthenticationToken(user, token, auths);
    }

    /**
     * 校验 token
     *
     * @param authToken access_token
     * @return 是否校验成功
     */
    @Override
    public boolean validateToken(String authToken) {
        // 可从 redis 缓存中获取校验 TODO
        try {
            if (Objects.isNull(verifier)) {
                init();
            }
            JwtHelper.decodeAndVerify(authToken, verifier);
            return true;
        } catch (Exception e) {
            log.info("Invalid JWT token.");
            log.warn("Invalid JWT token trace.", e);
        }
        return false;
    }

    private String getTokenEndpoint() {
        String tokenEndpointUrl = jHipsterProperties.getSecurity().getClientAuthorization().getAccessTokenUri();
        if (tokenEndpointUrl == null) {
            throw new InvalidClientException("no token endpoint configured in application properties");
        }
        return tokenEndpointUrl;
    }

    private void addAuthentication(HttpHeaders reqHeaders, MultiValueMap<String, String> formParams) {
        reqHeaders.add(AUTHORIZATION, getAuthorizationHeader());
    }

    private String getAuthorizationHeader() {
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String authorization = clientId + ":" + clientSecret;
        return BASIC + Base64Utils.encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
    }

    private String getClientId() {
        String clientId = oAuth2Properties.getWebClientConfiguration().getClientId();
        if (clientId == null) {
            throw new InvalidClientException("no client-id configured in application properties");
        }
        return clientId;
    }

    private String getClientSecret() {
        String clientSecret = oAuth2Properties.getWebClientConfiguration().getSecret();
        if (clientSecret == null) {
            throw new InvalidClientException("no client-secret configured in application properties");
        }
        return clientSecret;
    }

    private String getPublicKeyEndpoint() {
        String tokenEndpointUrl = oAuth2Properties.getSignatureVerification().getPublicKeyEndpointUri();
        if (tokenEndpointUrl == null) {
            throw new InvalidClientException("no token endpoint configured in application properties");
        }
        return tokenEndpointUrl;
    }

}
