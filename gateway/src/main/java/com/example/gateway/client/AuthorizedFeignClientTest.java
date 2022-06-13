package com.example.gateway.client;

import com.example.common.client.AuthorizedFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author peppy
 */
@AuthorizedFeignClient(name = "microservice")
public interface AuthorizedFeignClientTest {

    @ApiOperation(value = "查询动态修改的配置", tags = "测试")
    @GetMapping("/api/test/app-name")
    ResponseEntity<String> getApplicationName();

}
