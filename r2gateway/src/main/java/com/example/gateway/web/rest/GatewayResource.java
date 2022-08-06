package com.example.gateway.web.rest;

import com.example.gateway.security.AuthoritiesConstants;
import com.example.gateway.web.rest.vm.RouteVM;
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
 * REST controller for managing Gateway configuration.
 *
 * @author peppy
 */
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

    /**
     * {@code GET  /routes} : get the active routes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the list of routes.
     */
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
