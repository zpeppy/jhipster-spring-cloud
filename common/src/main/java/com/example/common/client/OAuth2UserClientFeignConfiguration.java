package com.example.common.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * feign 请求拦截器配置, 携带用户登录 token 信息
 *
 * @author peppy
 */
public class OAuth2UserClientFeignConfiguration {

    @Bean(name = "userFeignClientInterceptor")
    public RequestInterceptor getUserFeignClientInterceptor() {
        return new UserFeignClientInterceptor();
    }
}
