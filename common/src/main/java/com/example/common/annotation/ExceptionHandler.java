package com.example.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 拦截异常统一处理
 *
 * @author peppy
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
public @interface ExceptionHandler {

}
