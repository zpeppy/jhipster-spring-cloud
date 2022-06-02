package com.example.common.config;

import com.alibaba.fastjson.JSON;
import com.example.common.exception.CustomFeignException;
import com.example.common.exception.CustomHystrixBadRequestException;
import com.example.common.exception.obj.ErrorInfo;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * 处理 feign 调用异常
 *
 * @author peppy
 */
@ConditionalOnProperty(prefix = "application.feign-client-error-decoder", name = "enabled", havingValue = "true")
@Slf4j
@Primary
@Configuration
public class FeignClientErrorDecoder extends ErrorDecoder.Default {

    /**
     * 是否需要开启熔断
     */
    @Value("${application.feign-client-error-decoder.hystrix.enabled:false}")
    private boolean hystrixEnabled;

    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception = super.decode(methodKey, response);

        if (exception instanceof RetryableException) {
            return exception;
        }
        try {
            if (exception instanceof FeignException) {
                FeignException feignException = (FeignException) exception;
                int status = feignException.status();
                String content = feignException.contentUTF8();

                ErrorInfo errorInfo = JSON.parseObject(content, ErrorInfo.class);
                if (Objects.isNull(errorInfo)) {
                    errorInfo = new ErrorInfo();
                    errorInfo.setStatus(status);
                    errorInfo.setMessage(content);
                }
                if (errorInfo.getStatus() == 0) {
                    errorInfo.setStatus(status);
                }
                if (StringUtils.isEmpty(errorInfo.getMessage())) {
                    errorInfo.setMessage(content);
                }
                errorInfo.setMethodKey(methodKey);
                errorInfo.setReason(response.reason());
                log.warn("##### decode -> errorInfo: [{}]", errorInfo);

                if (hystrixEnabled) {
                    if (status < HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        // 4xx 错误码, 不走熔断
                        exception = new CustomHystrixBadRequestException(errorInfo);
                    } else {
                        // 5xx 错误码, 走熔断
                        exception = new CustomFeignException(errorInfo);
                    }
                } else {
                    exception = new CustomHystrixBadRequestException(errorInfo);
                }
            }
        } catch (Exception ex) {
            log.warn("##### decode -> exception info", ex);
        }
        return exception;
    }

}
