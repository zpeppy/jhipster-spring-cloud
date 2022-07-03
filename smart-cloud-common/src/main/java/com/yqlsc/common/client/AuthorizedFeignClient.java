package com.yqlsc.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * FeignClient 自定义注解, 机器内部服务调用, 提供客户端模式下 token 信息, 以下配置保持和 uaa 一致
 * <p>
 * jhipster.security.client-authorization.client-id=internal
 * <p>
 * jhipster.security.client-authorization.client-secret=internal
 *
 * @author peppy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@FeignClient
public @interface AuthorizedFeignClient {

    /**
     * @return 服务名称
     */
    @AliasFor(annotation = FeignClient.class, attribute = "name")
    String name() default "";

    /**
     * feign 请求拦截器配置类
     */
    @AliasFor(annotation = FeignClient.class, attribute = "configuration")
    Class<?>[] configuration() default OAuth2InterceptedFeignConfiguration.class;

    /**
     * @return 请求的 url
     */
    String url() default "";

    /**
     * @return 是否抛出解码404, 而不是抛出 {@link feign.FeignException} 异常
     */
    boolean decode404() default false;

    /**
     * @return 降级回调类
     */
    Class<?> fallback() default void.class;

    /**
     * @return 请求的路径前缀
     */
    String path() default "";
}
