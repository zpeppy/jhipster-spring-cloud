package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.security.jwt.JwtFilter;
import com.yqlsc.gateway.security.jwt.TokenProvider;
import com.yqlsc.gateway.web.rest.vm.JwtTokenVM;
import com.yqlsc.gateway.web.rest.vm.LoginVM;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 使用 uaa 进行鉴权
 *
 * @author peppy
 */
@Slf4j
@ConditionalOnProperty(prefix = "application", value = "useUaa", havingValue = "true")
@Api(value = "用户认证授权管理", tags = "用户认证授权管理")
@RestController
@RequestMapping("/api")
public class UserJwtUaaController {

    @Resource
    private TokenProvider tokenProvider;

    @ApiOperation(value = "用户登录认证", tags = "用户认证授权管理")
    @PostMapping("/authenticate")
    public Mono<ResponseEntity<JwtTokenVM>> authorize(@Valid @RequestBody Mono<LoginVM> loginVm) {
        return loginVm.map(login ->
                tokenProvider.createToken(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()), login)
        ).map(jwt -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, JwtFilter.BEARER + jwt);
            return new ResponseEntity<>(new JwtTokenVM(jwt), httpHeaders, HttpStatus.OK);
        });
    }

}
