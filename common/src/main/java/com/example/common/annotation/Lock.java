package com.example.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 分布式锁
 *
 * @author peppy
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
public @interface Lock {

    /**
     * 锁名称前缀
     *
     * @return 锁名称前缀
     */
    String prefixName();

    /**
     * 是否使用阻塞锁
     *
     * @return 默认为非阻塞
     */
    boolean blocking() default false;

    /**
     * 过期时间
     *
     * @return -1, 不过期
     */
    long leaseTime() default -1;

    /**
     * 过期时间单位
     *
     * @return {@link TimeUnit#MILLISECONDS}, 毫秒
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
}
