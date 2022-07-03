package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.client.AuthorizedFeignClientTest;
import com.yqlsc.gateway.client.AuthorizedUserFeignClientTest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 *
 * @author peppy
 */
@RequiredArgsConstructor
@Slf4j
@Api(value = "测试", tags = "测试")
@RestController
@RequestMapping("/api/test")
public class TestResource {

    private final AuthorizedFeignClientTest authorizedFeignClientTest;

    private final AuthorizedUserFeignClientTest authorizedUserFeignClientTest;

    @ApiOperation(value = "测试 feign 调用", tags = "测试")
    @GetMapping("/feign-call")
    public ResponseEntity<String> getFeignCall() {
        return authorizedFeignClientTest.getApplicationName();
    }

    @ApiOperation(value = "测试 user feign 调用", tags = "测试")
    @GetMapping("/user-feign-call")
    public ResponseEntity<String> getUserFeignCall() {
        return authorizedUserFeignClientTest.getApplicationName();
    }

}
