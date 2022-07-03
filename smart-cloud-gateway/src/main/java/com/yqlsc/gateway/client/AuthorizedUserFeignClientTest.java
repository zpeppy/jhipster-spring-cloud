package com.yqlsc.gateway.client;

import com.yqlsc.common.client.AuthorizedUserFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 测试 FeignClient
 *
 * @author peppy
 */
@AuthorizedUserFeignClient(name = "microservice")
public interface AuthorizedUserFeignClientTest {

    @ApiOperation(value = "查询动态修改的配置", tags = "测试")
    @GetMapping("/api/test/app-name")
    ResponseEntity<String> getApplicationName();

}
