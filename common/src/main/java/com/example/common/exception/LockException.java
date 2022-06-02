package com.example.common.exception;

/**
 * 分布式锁异常
 *
 * @author peppy
 */
public class LockException extends RuntimeException {
    private static final long serialVersionUID = 1430199167545387792L;

    public LockException(String message) {
        super(message);
    }
}
