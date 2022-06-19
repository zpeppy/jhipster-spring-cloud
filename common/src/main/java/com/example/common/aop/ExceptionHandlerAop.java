package com.example.common.aop;

import com.example.common.service.ExceptionHandlerService;
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
 * 拦截异常统一处理
 * <p>
 * {@link ExceptionHandlerService} 实现类必须使用 {@link Component} 注解注入, 不能使用 {@link Bean} 注入
 *
 * @author peppy
 */
@Order
@ConditionalOnBean(ExceptionHandlerService.class)
@Slf4j
@Component
@Aspect
public class ExceptionHandlerAop {

    @Resource
    private ExceptionHandlerService exceptionHandlerService;

    @Pointcut("@within(com.example.common.annotation.ExceptionHandler) || @annotation(com.example.common.annotation.ExceptionHandler)")
    public void pointcut() {
        // 切入点方法
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
            exceptionHandlerService.exceptionHandler(method, args, ex);
        }
        return proceed;
    }

}
