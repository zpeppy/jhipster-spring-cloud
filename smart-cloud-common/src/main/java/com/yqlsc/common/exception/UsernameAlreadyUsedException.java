package com.yqlsc.common.exception;

/**
 * 账号已存在异常
 *
 * @author peppy
 */
public class UsernameAlreadyUsedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UsernameAlreadyUsedException() {
        super("Login name already used!");
    }

}
