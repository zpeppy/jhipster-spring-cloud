package com.example.common.security.oauth2;

import org.springframework.security.jwt.crypto.sign.SignatureVerifier;

/**
 * 从 uaa 获取公钥接口
 *
 * @author peppy
 */
public interface OAuth2SignatureVerifierClient {
    /**
     * 获取公钥信息
     *
     * @return 公钥
     * @throws Exception 获取失败异常
     */
    SignatureVerifier getSignatureVerifier() throws Exception;
}
