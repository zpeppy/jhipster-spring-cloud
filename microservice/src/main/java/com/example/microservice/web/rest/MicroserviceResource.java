package com.example.microservice.web.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
@RequestMapping("/api/microservice")
public class MicroserviceResource {

    @Value("${application.name}")
    private String appConfig;

    @GetMapping("/app-config")
    public ResponseEntity<String> getApplication(){
        return ResponseEntity.ok(appConfig);
    }

}
