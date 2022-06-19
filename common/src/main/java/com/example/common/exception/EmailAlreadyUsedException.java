package com.example.common.exception;

/**
 * 邮箱已被使用异常
 *
 * @author peppy
 */
public class EmailAlreadyUsedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyUsedException() {
        super("Email is already in use!");
    }

}
