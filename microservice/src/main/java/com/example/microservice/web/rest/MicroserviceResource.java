package com.example.microservice.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试动态更新配置
 *
 * @author peppy
 */
@Api(value = "测试", tags = "测试")
@RefreshScope
@RestController
@RequestMapping("/api/microservice")
public class MicroserviceResource {

    @Value("${application.name}")
    private String appConfig;

    @ApiOperation(value = "查询动态修改的配置", tags = "测试")
    @GetMapping("/app-config")
    public ResponseEntity<String> getApplication() {
        return ResponseEntity.ok(appConfig);
    }

}
