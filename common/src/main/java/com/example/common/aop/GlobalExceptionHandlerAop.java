package com.example.common.aop;

import com.example.common.service.GlobalExceptionHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 全局异常统一处理
 * <p>
 * {@link GlobalExceptionHandlerService} 实现类必须使用 {@link Component} 注解注入, 不能使用 {@link Bean} 注入.
 *
 * @author peppy
 */
@Order
@ConditionalOnBean(GlobalExceptionHandlerService.class)
@Slf4j
@Component
@Aspect
public class GlobalExceptionHandlerAop {

    @Resource
    private GlobalExceptionHandlerService globalExceptionHandlerService;

    @Pointcut("@within(com.example.common.annotation.GlobalExceptionHandler) || @annotation(com.example.common.annotation.GlobalExceptionHandler)")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Object[] args = pjp.getArgs();

        Object proceed = null;
        try {
            proceed = pjp.proceed(args);
        } catch (Exception ex) {
            // 只处理 Exception 级别异常
            globalExceptionHandlerService.exceptionHandler(method, args, ex);
        }
        return proceed;
    }

}
