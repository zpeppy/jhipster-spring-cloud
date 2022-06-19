package com.example.gateway.client;

import com.example.common.client.AuthorizedUserFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 测试 UserFeignClient 调用
 *
 * @author peppy
 */
@AuthorizedUserFeignClient(name = "microservice")
public interface AuthorizedUserFeignClientTest {

    @ApiOperation(value = "查询动态修改的配置", tags = "测试")
    @GetMapping("/api/test/app-name")
    ResponseEntity<String> getApplicationName();

}
