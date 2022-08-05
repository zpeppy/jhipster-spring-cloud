package com.example.gateway.web.rest.vm;

import lombok.Data;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * View Model that stores a route managed by the Gateway.
 *
 * @author peppy
 */
@Data
public class RouteVM {

    private String path;

    private String serviceId;

    private List<ServiceInstance> serviceInstances;

}
