package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.config.Constants;
import com.yqlsc.gateway.security.jwt.JwtFilter;
import com.yqlsc.gateway.security.jwt.TokenProvider;
import com.yqlsc.gateway.service.AuditEventService;
import com.yqlsc.gateway.web.rest.vm.JwtTokenVM;
import com.yqlsc.gateway.web.rest.vm.LoginVM;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * 使用自带 jwt 鉴权
 *
 * @author peppy
 */
@ConditionalOnProperty(prefix = "application", value = "useUaa", havingValue = "false", matchIfMissing = true)
@Api(value = "用户认证授权管理", tags = "用户认证授权管理")
@RestController
@RequestMapping("/api")
public class UserJwtController {

    private final TokenProvider tokenProvider;

    private final ReactiveAuthenticationManager authenticationManager;

    private final AuditEventService auditEventService;

    public UserJwtController(TokenProvider tokenProvider, ReactiveAuthenticationManager authenticationManager, AuditEventService auditEventService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.auditEventService = auditEventService;
    }

    @ApiOperation(value = "用户登录认证", tags = "用户认证授权管理")
    @PostMapping("/authenticate")
    public Mono<ResponseEntity<JwtTokenVM>> authorize(@Valid @RequestBody Mono<LoginVM> loginVm) {
        return loginVm
                .flatMap(login -> authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()))
                        .onErrorResume(throwable -> onAuthenticationError(login, throwable))
                        .flatMap(auth -> onAuthenticationSuccess(login, auth))
                        .flatMap(auth -> Mono.fromCallable(() -> tokenProvider.createToken(auth, login)))
                )
                .map(jwt -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, JwtFilter.BEARER + jwt);
                    return new ResponseEntity<>(new JwtTokenVM(jwt), httpHeaders, HttpStatus.OK);
                });
    }

    private Mono<? extends Authentication> onAuthenticationSuccess(LoginVM login, Authentication auth) {
        return Mono.just(login)
                .map(LoginVM::getUsername)
                .filter(username -> !Constants.ANONYMOUS_USER.equals(username))
                .flatMap(auditEventService::saveAuthenticationSuccess)
                .thenReturn(auth);
    }

    private Mono<? extends Authentication> onAuthenticationError(LoginVM login, Throwable throwable) {
        return Mono.just(login)
                .map(LoginVM::getUsername)
                .filter(username -> !Constants.ANONYMOUS_USER.equals(username))
                .flatMap(username -> auditEventService.saveAuthenticationError(username, throwable))
                .then(Mono.error(throwable));
    }

}
