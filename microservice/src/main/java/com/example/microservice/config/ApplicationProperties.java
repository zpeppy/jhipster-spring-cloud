package com.example.microservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Microservice.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 *
 * @author peppy
 */
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

}
