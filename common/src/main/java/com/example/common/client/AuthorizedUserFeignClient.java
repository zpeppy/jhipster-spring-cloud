package com.example.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * FeignClient 自定义注解, 用户名密码模式调用, 携带 web 上下文中 token 信息
 *
 * @author peppy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@FeignClient
public @interface AuthorizedUserFeignClient {

    @AliasFor(annotation = FeignClient.class, attribute = "name")
    String name() default "";

    /**
     * A custom {@code @Configuration} for the feign client.
     * <p>
     * Can contain override {@code @Bean} definition for the pieces that make up the client, for instance {@link
     * feign.codec.Decoder}, {@link feign.codec.Encoder}, {@link feign.Contract}.
     *
     * @see FeignClientsConfiguration for the defaults.
     */
    @AliasFor(annotation = FeignClient.class, attribute = "configuration")
    Class<?>[] configuration() default OAuth2UserClientFeignConfiguration.class;

    /**
     * An absolute URL or resolvable hostname (the protocol is optional).
     */
    String url() default "";

    /**
     * Whether 404s should be decoded instead of throwing FeignExceptions.
     */
    boolean decode404() default false;

    /**
     * Fallback class for the specified Feign client interface. The fallback class must implement the interface
     * annotated by this annotation and be a valid Spring bean.
     */
    Class<?> fallback() default void.class;

    /**
     * Path prefix to be used by all method-level mappings. Can be used with or without {@code @RibbonClient}.
     */
    String path() default "";
}
