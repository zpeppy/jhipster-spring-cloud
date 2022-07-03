package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.security.oauth2.OAuth2AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户认证接口
 *
 * @author peppy
 */
@Api(value = "认证", tags = "认证")
@RestController
@RequestMapping("/auth")
public class AuthResource {

    private final Logger log = LoggerFactory.getLogger(AuthResource.class);

    private OAuth2AuthenticationService authenticationService;

    public AuthResource(OAuth2AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "账号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "rememberMe", value = "是否记住")
    })
    @ApiOperation(value = "登录", tags = "认证")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OAuth2AccessToken> authenticate(HttpServletRequest request, HttpServletResponse response,
                                                          @RequestBody Map<String, String> params) {
        return authenticationService.authenticate(request, response, params);
    }

    @ApiOperation(value = "退出", tags = "认证")
    @PostMapping(value = "/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("logging out user {}", SecurityContextHolder.getContext().getAuthentication().getName());
        authenticationService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}
