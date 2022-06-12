package com.example.common.aop;

import com.example.common.annotation.Lock;
import com.example.common.utils.MD5Utils;
import liquibase.exception.LockException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;

/**
 * 分布式锁处理
 *
 * @author peppy
 */
@Slf4j
@Component
@Aspect
public class LockAop {

    public static final String DELIMITER = ":";

    @Resource
    private RedissonClient redissonClient;

    @Pointcut("@within(com.example.common.annotation.Lock) || @annotation(com.example.common.annotation.Lock)")
    public void pointcut() {
        // 切入点方法
    }

    /**
     * 拦截 {@link Lock} 注解的类或方法
     *
     * @param joinPoint 拦截的方法
     * @return Object 方法返回值
     * @throws Throwable 异常
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        Lock lockAnnotation = method.getAnnotation(Lock.class);
        String lockName = getLockName(lockAnnotation, args);

        RLock lock = redissonClient.getLock(lockName);
        if (lockAnnotation.blocking()) {
            lock.lock(lockAnnotation.leaseTime(), lockAnnotation.unit());
        } else {
            if (!lock.tryLock()) {
                throw new LockException("操作过于频繁, 请稍后重试");
            }
        }
        Object result;
        try {
            result = joinPoint.proceed(args);
        } finally {
            lock.unlock();
        }
        return result;
    }

    /**
     * 获取锁名称
     *
     * @param annotation lock 注解
     * @param args       方法参数
     * @return 锁名称
     * @throws NoSuchAlgorithmException 不匹配算法异常
     */
    private String getLockName(Lock annotation, Object[] args) throws NoSuchAlgorithmException {
        return annotation.prefixName() + DELIMITER + MD5Utils.md5(args);
    }
}
