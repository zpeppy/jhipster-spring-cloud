package com.example.common.service;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 全局异常统一处理
 *
 * @author peppy
 */
public interface GlobalExceptionHandlerService {

    /**
     * 处理错误信息
     *
     * @param method method
     * @param args   args
     * @param ex     ex
     */
    void exceptionHandler(Method method, Object[] args, Exception ex);

    /**
     * 获取参数字符串数据
     *
     * @param args 参数数组
     * @return 字符串
     */
    default String getArgInfo(Object[] args) {
        if (Objects.isNull(args)) {
            return null;
        }
        Object arg = args[0];
        if (arg instanceof byte[]) {
            return StringUtils.toEncodedString((byte[]) arg, StandardCharsets.UTF_8);
        }
        return String.valueOf(arg);
    }

}
