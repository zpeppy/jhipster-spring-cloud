package com.example.common.config;

import com.example.common.exception.CustomFeignException;
import com.example.common.exception.CustomHystrixBadRequestException;
import com.example.common.exception.obj.ErrorInfo;
import com.example.common.web.rest.errors.BadRequestAlertException;
import com.example.common.web.rest.errors.ErrorConstants;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import liquibase.exception.LockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * @author peppy
 */
@ConditionalOnProperty(prefix = "application.global-exception-handler", name = "enabled", havingValue = "true")
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = {Controller.class, RestController.class})
public class GlobalExceptionHandler implements ProblemHandling, SecurityAdviceTrait {

    private static final String KEY_MESSAGE = "message";
    private static final String KEY_PATH = "path";
    private static final String KEY_CODE = "code";
    private static final String KEY_TIME = "time";
    private static final String KEY_METHOD_KEY = "methodKey";
    private static final String KEY_REASON = "reason";

    private static final String CALL_EXCEPTION_MESSAGE = "调用其他服务异常, 请稍后重试";
    private static final String SYSTEM_EXCEPTION_MESSAGE = "系统异常, 请稍后重试";

    /**
     * feign 调用其他服务时, 其他服务抛出的异常
     *
     * @param ex {@link HystrixBadRequestException} or {@link CustomHystrixBadRequestException} 异常类
     * @return 异常信息
     */
    @ExceptionHandler(HystrixBadRequestException.class)
    public ResponseEntity<Problem> hystrixBadRequestExceptionHandler(NativeWebRequest request, HystrixBadRequestException ex) {
        printLog(ex);
        Problem problem = getFeignHystrixProblem(request, ex);
        return create(ex, problem, request);
    }

    /**
     * 包装 Problem 类
     *
     * @param request NativeWebRequest
     * @param ex      RuntimeException
     * @return Problem
     */
    @SuppressWarnings("all")
    private Problem getFeignHystrixProblem(NativeWebRequest request, RuntimeException ex) {
        ProblemBuilder problemBuilder = Problem.builder()
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("调用服务异常")
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .with(KEY_MESSAGE, getErrorMessage(ex))
                .with(KEY_TIME, Instant.now())
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        if (ex instanceof CustomHystrixBadRequestException) {
            problemBuilder.withTitle("调用服务异常(Bad)");
            CustomHystrixBadRequestException exception = (CustomHystrixBadRequestException) ex;
            ErrorInfo errorInfo = exception.getErrorInfo();

            problemBuilder(problemBuilder, errorInfo);
        } else if (ex instanceof CustomFeignException) {
            problemBuilder.withTitle("调用服务异常(Run)");
            CustomFeignException exception = (CustomFeignException) ex;
            ErrorInfo errorInfo = exception.getErrorInfo();

            problemBuilder(problemBuilder, errorInfo);
        }
        return problemBuilder.build();
    }

    /**
     * 处理字段数据
     *
     * @param problemBuilder ProblemBuilder
     * @param info           ErrorInfo
     */
    private void problemBuilder(ProblemBuilder problemBuilder, ErrorInfo info) {
        Optional.ofNullable(info).ifPresent(errorInfo -> {
            Optional.ofNullable(errorInfo.getType()).ifPresent(type -> problemBuilder.withType(URI.create(type)));
            Optional.ofNullable(errorInfo.getTitle()).ifPresent(problemBuilder::withTitle);
            Optional.of(errorInfo.getStatus()).ifPresent(status -> problemBuilder.withStatus(Status.valueOf(status)));
            Optional.ofNullable(errorInfo.getDetail()).ifPresent(problemBuilder::withDetail);
            Optional.ofNullable(errorInfo.getMessage()).ifPresent(message -> problemBuilder.with(KEY_MESSAGE, message));
            Optional.ofNullable(errorInfo.getPath()).ifPresent(path -> problemBuilder.with(KEY_PATH, path));
            Optional.of(errorInfo.getStatus()).ifPresent(status -> problemBuilder.with(KEY_CODE, status));
            Optional.ofNullable(errorInfo.getMethodKey()).ifPresent(methodKey -> problemBuilder.with(KEY_METHOD_KEY, methodKey));
            Optional.ofNullable(errorInfo.getReason()).ifPresent(reason -> problemBuilder.with(KEY_REASON, reason));
        });
    }

    /**
     * feign 熔断异常, 其他服务没调通
     *
     * @param ex feign 熔断异常
     * @return 异常信息
     */
    @SuppressWarnings("all")
    @ExceptionHandler(HystrixRuntimeException.class)
    public ResponseEntity<Problem> hystrixRuntimeExceptionHandler(NativeWebRequest request, HystrixRuntimeException ex) {
        printLog(ex);
        Problem problem = Problem.builder()
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("调用服务熔断异常")
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .withDetail(ex.getMessage())
                .with(KEY_MESSAGE, CALL_EXCEPTION_MESSAGE)
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return create(ex, problem, request);
    }

    /**
     * feign 请求异常, {@link FeignClientErrorDecoder} 中抛出
     *
     * @param ex feign 请求异常
     * @return 异常信息
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Problem> feignExceptionHandler(NativeWebRequest request, FeignException ex) {
        printLog(ex);
        Problem problem = getFeignHystrixProblem(request, ex);
        return create(ex, problem, request);
    }

    /**
     * Controller 中校验 ID 抛出
     *
     * @param request 请求
     * @param ex      异常
     * @return 异常信息
     */
    @SuppressWarnings("all")
    @ExceptionHandler(BadRequestAlertException.class)
    public ResponseEntity<Problem> badRequestAlertExceptionHandler(NativeWebRequest request, BadRequestAlertException ex) {
        printLog(ex);
        Problem problem = Problem.builder()
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("请求参数绑定异常")
                .withStatus(Status.BAD_REQUEST)
                .with(KEY_MESSAGE, getErrorMessage(ex))
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.BAD_REQUEST.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return create(ex, problem, request);
    }

    /**
     * 重写方法, 自带的没有 message
     * 参数校验异常
     *
     * @param ex      异常
     * @param request 请求
     * @return 响应
     */
    @SuppressWarnings("all")
    @Override
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Problem> handleConstraintViolation(ConstraintViolationException ex, NativeWebRequest request) {
        printLog(ex);
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        Set<String> messages = constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
        Problem problem = Problem.builder()
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("请求参数校验异常")
                .withStatus(Status.BAD_REQUEST)
                .with(KEY_MESSAGE, joinErrorMessages(messages))
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.BAD_REQUEST.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return create(ex, problem, request);
    }

    /**
     * 连接错误信息集合
     *
     * @param messages 错误信息集合
     * @return 错误信息
     */
    private String joinErrorMessages(Collection<String> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return "";
        }
        return String.join(", ", messages);
    }

    /**
     * {@link javax.validation.Valid} 参数校验异常
     *
     * @param ex      参数校验异常
     * @param request 请求
     * @return 给前端的响应数据
     */
    @SuppressWarnings("all")
    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, NativeWebRequest request) {
        printLog(ex);
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        Set<String> messages = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.toSet());
        Problem problem = Problem.builder()
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("方法参数校验异常")
                .withStatus(Status.BAD_REQUEST)
                .with(KEY_MESSAGE, joinErrorMessages(messages))
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.BAD_REQUEST.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return this.create(ex, problem, request);
    }

    /**
     * 处理参数异常
     *
     * @param ex      MissingServletRequestParameterException
     * @param request NativeWebRequest
     * @return Problem
     */
    @SuppressWarnings("all")
    @Override
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Problem> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, NativeWebRequest request) {
        printLog(ex);
        Problem problem = Problem.builder()
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("处理请求参数异常")
                .withStatus(Status.BAD_REQUEST)
                .with(KEY_MESSAGE, ex.getMessage())
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.BAD_REQUEST.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return this.create(ex, problem, request);
    }

    /**
     * 处理分布式锁异常
     *
     * @param ex      LockException
     * @param request NativeWebRequest
     * @return Problem
     */
    @SuppressWarnings("all")
    @ExceptionHandler(LockException.class)
    public ResponseEntity<Problem> handleLockException(LockException ex, NativeWebRequest request) {
        printLog(ex);
        Problem problem = Problem.builder()
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("分布式锁异常")
                .withStatus(Status.BAD_REQUEST)
                .with(KEY_MESSAGE, ex.getMessage())
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.BAD_REQUEST.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return this.create(ex, problem, request);
    }

    /**
     * 运行时异常
     *
     * @param request 请求
     * @param ex      运行时异常
     * @return 异常信息
     */
    @SuppressWarnings("all")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Problem> runtimeExceptionHandler(NativeWebRequest request, RuntimeException ex) {
        printLog(ex);
        Problem problem = Problem.builder()
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("系统异常(Run)")
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .with(KEY_MESSAGE, SYSTEM_EXCEPTION_MESSAGE)
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return create(ex, problem, request);
    }

    /**
     * 根据业务规则, 统一处理异常。其他异常
     */
    @SuppressWarnings("all")
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> exceptionHandler(NativeWebRequest request, Exception ex) {
        printLog(ex);
        Problem problem = Problem.builder()
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("系统异常")
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .with(KEY_MESSAGE, SYSTEM_EXCEPTION_MESSAGE)
                .with(KEY_PATH, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
                .with(KEY_CODE, Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .with(KEY_TIME, Instant.now())
                .build();
        return create(ex, problem, request);
    }

    /**
     * 获取错误信息
     *
     * @param ex Throwable
     * @return 错误信息
     */
    private String getErrorMessage(Throwable ex) {
        return ex.getMessage();
    }

    /**
     * 打印异常日志
     *
     * @param ex Exception
     */
    private void printLog(Exception ex) {
        log.error("##### printLog -> exception info", ex);
    }

    /**
     * 获取堆栈信息
     *
     * @param ex Throwable
     * @return 堆栈信息
     */
    @SuppressWarnings("all")
    private String getStackTraceInfo(Throwable ex) {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            ex.printStackTrace(printWriter);
            stringWriter.flush();
            return stringWriter.toString();
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * 递归获取异常
     *
     * @param e Throwable
     * @return Throwable
     */
    private Throwable getCause(Throwable e) {
        Throwable cause = e.getCause();
        if (Objects.isNull(cause)) {
            return e;
        } else {
            return this.getCause(cause);
        }
    }

}
