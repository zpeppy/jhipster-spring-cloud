package com.example.common.exception;

/**
 * 邮箱已存在异常
 *
 * @author peppy
 */
public class EmailAlreadyUsedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyUsedException() {
        super("Email is already in use!");
    }

}
