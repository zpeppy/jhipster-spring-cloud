package com.example.common.config.oauth2;

import com.example.common.security.oauth2.OAuth2SignatureVerifierClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Map;

/**
 * jwt access token 增强处理
 *
 * @author peppy
 */
public class OAuth2JwtAccessTokenConverter extends JwtAccessTokenConverter {
    private final Logger log = LoggerFactory.getLogger(OAuth2JwtAccessTokenConverter.class);

    private final OAuth2Properties oAuth2Properties;
    private final OAuth2SignatureVerifierClient signatureVerifierClient;
    /**
     * 最后获取公钥时间戳
     */
    private long lastKeyFetchTimestamp;

    public OAuth2JwtAccessTokenConverter(OAuth2Properties oAuth2Properties, OAuth2SignatureVerifierClient signatureVerifierClient) {
        this.oAuth2Properties = oAuth2Properties;
        this.signatureVerifierClient = signatureVerifierClient;
        tryCreateSignatureVerifier();
    }

    /**
     * 使用公钥解码 token, 如果解码失败请重新获取公钥
     *
     * @param token jwt token
     * @return 解码后的结果
     * @throws InvalidTokenException 解码异常
     */
    @Override
    protected Map<String, Object> decode(String token) {
        try {
            // 检查公钥是否过期
            long ttl = oAuth2Properties.getSignatureVerification().getTtl();
            if (ttl > 0 && System.currentTimeMillis() - lastKeyFetchTimestamp > ttl) {
                throw new InvalidTokenException("public key expired");
            }
            return super.decode(token);
        } catch (InvalidTokenException ex) {
            if (tryCreateSignatureVerifier()) {
                return super.decode(token);
            }
            throw ex;
        }
    }

    /**
     * 尝试从 uaa 获取新的公钥
     *
     * @return 是否成功获取
     */
    private boolean tryCreateSignatureVerifier() {
        long t = System.currentTimeMillis();
        if (t - lastKeyFetchTimestamp < oAuth2Properties.getSignatureVerification().getPublicKeyRefreshRateLimit()) {
            return false;
        }
        try {
            SignatureVerifier verifier = signatureVerifierClient.getSignatureVerifier();
            if (verifier != null) {
                setVerifier(verifier);
                lastKeyFetchTimestamp = t;
                log.debug("Public key retrieved from OAuth2 server to create SignatureVerifier");
                return true;
            }
        } catch (Throwable ex) {
            log.error("could not get public key from OAuth2 server to create SignatureVerifier", ex);
        }
        return false;
    }

    /**
     * 获取解码后的明文信息
     * <p>
     * 获取方式:
     * <pre>
     * <code>
     *  SecurityContext securityContext = SecurityContextHolder.getContext();
     *  Authentication authentication = securityContext.getAuthentication();
     *  if (authentication != null) {
     *      Object details = authentication.getDetails();
     *      if (details instanceof OAuth2AuthenticationDetails) {
     *          Object decodedDetails = ((OAuth2AuthenticationDetails) details).getDecodedDetails();
     *          if (decodedDetails != null &amp;&amp; decodedDetails instanceof Map) {
     *             String detailFoo = ((Map) decodedDetails).get("foo");
     *          }
     *      }
     *  }
     * </code>
     *  </pre>
     *
     * @param claims token 明文信息
     * @return {@link OAuth2Authentication} 对象
     */
    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
        OAuth2Authentication authentication = super.extractAuthentication(claims);
        authentication.setDetails(claims);
        return authentication;
    }
}
