package com.yqlsc.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 读取 {@code application-${spring.profiles.active}.yml} 中 {@code application:} 下配置
 *
 * @author peppy
 */
@Data
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private boolean useUaa = false;
    
}
