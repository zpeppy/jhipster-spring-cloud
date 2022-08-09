package com.yqlsc.microservice.web.rest;

import com.yqlsc.common.security.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试动态更新配置
 * <p>
 * 如需实时拉取配置中心配置需加 {@code @RefreshScope} 注解标注
 *
 * @author peppy
 */
@Slf4j
@Api(value = "测试", tags = "测试")
@RefreshScope
@RestController
@RequestMapping("/api/test")
public class TestResource {

    @Value("${application.name}")
    private String appName;

    @ApiOperation(value = "查询动态修改的配置", tags = "测试")
    @GetMapping("/app-name")
    public ResponseEntity<String> getApplicationName() {
        log.info("##### getApplicationName -> current user login: {}", SecurityUtils.getCurrentUserLogin().orElse("获取不到当前账号"));
        return ResponseEntity.ok(appName);
    }

}
