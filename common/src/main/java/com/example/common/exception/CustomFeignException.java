package com.example.common.exception;

import com.example.common.exception.obj.ErrorInfo;
import feign.FeignException;
import lombok.Getter;
import org.springframework.lang.NonNull;

/**
 * 自定义 feign 异常
 *
 * @author peppy
 */
@Getter
public class CustomFeignException extends FeignException {

    private static final long serialVersionUID = -4460271218937052486L;

    private ErrorInfo errorInfo;

    public CustomFeignException(@NonNull ErrorInfo errorInfo) {
        super(errorInfo.getStatus(), errorInfo.getMessage());
        this.errorInfo = errorInfo;
    }

    protected CustomFeignException(int status, String message) {
        super(status, message);
    }

    protected CustomFeignException(int status, String message, Throwable cause) {
        super(status, message, cause);
    }

    protected CustomFeignException(int status, String message, Throwable cause, byte[] content) {
        super(status, message, cause, content);
    }

    protected CustomFeignException(int status, String message, byte[] content) {
        super(status, message, content);
    }
}
