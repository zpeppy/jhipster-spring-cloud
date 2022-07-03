package com.yqlsc.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * FeignClient 自定义注解, 浏览器用户名密码模式调用服务携带 web 上下文中 token 信息, 以下配置保持和 uaa 一致
 * <p>
 * uaa.web-client-configuration.client-id=web_app
 * <p>
 * uaa.web-client-configuration.secret=changeit
 *
 * @author peppy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@FeignClient
public @interface AuthorizedUserFeignClient {

    /**
     * @return 服务名称
     */
    @AliasFor(annotation = FeignClient.class, attribute = "name")
    String name() default "";

    /**
     * feign 请求拦截器配置类
     */
    @AliasFor(annotation = FeignClient.class, attribute = "configuration")
    Class<?>[] configuration() default OAuth2UserClientFeignConfiguration.class;

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
