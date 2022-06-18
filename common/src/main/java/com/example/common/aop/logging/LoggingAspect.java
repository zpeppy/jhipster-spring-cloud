package com.example.common.aop.logging;

import io.github.jhipster.config.JHipsterConstants;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 日志统一拦截处理
 *
 * @author peppy
 */
@Aspect
public class LoggingAspect {

    private final Environment env;

    public LoggingAspect(Environment env) {
        this.env = env;
    }

    /**
     * 拦截 {@link Repository}, {@link Service}, {@link RestController} 标注的类下所有方法
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)" +
            " || within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        //
    }

    /**
     * 拦截 {@code com.example.*.repository}, {@code com.example.*.service}, {@code com.example.*.web.rest} 包下所有类及方法
     */
    @Pointcut("within(com.example.*.repository..*)" +
            " || within(com.example.*.service..*)" +
            " || within(com.example.*.web.rest..*)")
    public void applicationPackagePointcut() {
        //
    }

    /**
     * 获取 {@link Logger} 对象
     *
     * @param joinPoint 拦截点信息
     * @return {@link Logger} 对象
     */
    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    }

    /**
     * 拦截异常通知
     *
     * @param joinPoint 拦截点信息
     * @param e         异常
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))) {
            logger(joinPoint)
                    .error(
                            "Exception in {}() with cause = '{}' and exception = '{}'",
                            joinPoint.getSignature().getName(),
                            e.getCause() != null ? e.getCause() : "NULL",
                            e.getMessage(),
                            e
                    );
        } else {
            logger(joinPoint)
                    .error(
                            "Exception in {}() with cause = {}",
                            joinPoint.getSignature().getName(),
                            e.getCause() != null ? e.getCause() : "NULL"
                    );
        }
    }

    /**
     * 环绕通知
     *
     * @param joinPoint 拦截点信息
     * @return 目标方法执行结果
     * @throws Throwable 抛出 {@link IllegalArgumentException} 异常
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = logger(joinPoint);
        if (log.isDebugEnabled()) {
            log.debug("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        try {
            Object result = joinPoint.proceed();
            if (log.isDebugEnabled()) {
                log.debug("Exit: {}() with result = {}", joinPoint.getSignature().getName(), result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getName());
            throw e;
        }
    }
}
