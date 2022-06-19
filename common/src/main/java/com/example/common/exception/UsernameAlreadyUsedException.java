package com.example.common.exception;

/**
 * 用户名已被使用异常
 *
 * @author peppy
 */
public class UsernameAlreadyUsedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UsernameAlreadyUsedException() {
        super("Login name already used!");
    }

}
