package com.example.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to r2gateway.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 *
 * @author peppy
 */
@Data
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private boolean useUaa = false;

}
