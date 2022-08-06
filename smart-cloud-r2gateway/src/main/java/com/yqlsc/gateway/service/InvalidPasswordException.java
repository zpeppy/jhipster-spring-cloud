package com.yqlsc.gateway.service;

/**
 * 无效的密码异常
 *
 * @author peppy
 */
public class InvalidPasswordException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super("Incorrect password");
    }

}
