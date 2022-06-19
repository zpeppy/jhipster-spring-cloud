package com.example.common.exception;

/**
 * 无效密码异常
 *
 * @author peppy
 */
public class InvalidPasswordException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super("Incorrect password");
    }

}
