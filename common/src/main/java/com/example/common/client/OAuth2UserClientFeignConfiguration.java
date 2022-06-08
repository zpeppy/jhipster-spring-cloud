package com.example.common.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class OAuth2UserClientFeignConfiguration {

    @Bean(name = "userFeignClientInterceptor")
    public RequestInterceptor getUserFeignClientInterceptor() {
        return new UserFeignClientInterceptor();
    }
}
