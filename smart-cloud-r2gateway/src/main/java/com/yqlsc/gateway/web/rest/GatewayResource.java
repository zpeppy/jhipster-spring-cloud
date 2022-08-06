package com.yqlsc.gateway.web.rest;

import com.yqlsc.gateway.security.AuthoritiesConstants;
import com.yqlsc.gateway.web.rest.vm.RouteVM;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peppy
 */
@Api(value = "网关路由管理", tags = "网关路由管理")
@RestController
@RequestMapping("/api/gateway")
public class GatewayResource {

    private final RouteLocator routeLocator;

    private final DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String appName;

    public GatewayResource(RouteLocator routeLocator, DiscoveryClient discoveryClient) {
        this.routeLocator = routeLocator;
        this.discoveryClient = discoveryClient;
    }

    @ApiOperation(value = "查询启用的路由", tags = "网关路由管理")
    @GetMapping("/routes")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<List<RouteVM>> activeRoutes() {
        Flux<Route> routes = routeLocator.getRoutes();
        List<RouteVM> routeVms = new ArrayList<>();
        routes.subscribe(route -> {
            RouteVM routeVm = new RouteVM();
            // Manipulate strings to make Gateway routes look like Zuul's
            String predicate = route.getPredicate().toString();
            String path = predicate.substring(predicate.indexOf("[") + 1, predicate.indexOf("]"));
            routeVm.setPath(path);
            String serviceId = route.getId().substring(route.getId().indexOf("_") + 1).toLowerCase();
            routeVm.setServiceId(serviceId);
            // Exclude gateway app from routes
            if (!serviceId.equalsIgnoreCase(appName)) {
                routeVm.setServiceInstances(discoveryClient.getInstances(serviceId));
                routeVms.add(routeVm);
            }
        });
        return ResponseEntity.ok(routeVms);
    }
}
