package com.yqlsc.gateway.security;

import org.springframework.security.core.AuthenticationException;

/**
 * 如果用户为激活状态访问则抛此异常
 *
 * @author peppy
 */
public class UserNotActivatedException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public UserNotActivatedException(String message) {
        super(message);
    }

    public UserNotActivatedException(String message, Throwable t) {
        super(message, t);
    }
}
