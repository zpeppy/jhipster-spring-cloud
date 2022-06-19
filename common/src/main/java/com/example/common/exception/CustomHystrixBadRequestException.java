package com.example.common.exception;

import com.example.common.exception.obj.ErrorInfo;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import lombok.Getter;
import org.springframework.lang.NonNull;

/**
 * Hystrix 熔断异常
 *
 * @author peppy
 */
@Getter
public class CustomHystrixBadRequestException extends HystrixBadRequestException {

    private static final long serialVersionUID = 7361092391901913892L;

    private ErrorInfo errorInfo;

    public CustomHystrixBadRequestException(@NonNull ErrorInfo errorInfo) {
        super(errorInfo.getMessage());
        this.errorInfo = errorInfo;
    }

    public CustomHystrixBadRequestException(String message) {
        super(message);
    }

    public CustomHystrixBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
