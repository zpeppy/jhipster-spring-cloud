package com.example.gateway.web.rest;

import com.example.common.security.AuthoritiesConstants;
import com.example.gateway.web.rest.vm.RouteVM;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for managing Gateway configuration.
 *
 * @author peppy
 */
@Api(value = "路由", tags = "路由")
@RestController
@RequestMapping("/api/gateway")
public class GatewayResource {

    private final RouteLocator routeLocator;

    private final DiscoveryClient discoveryClient;

    public GatewayResource(RouteLocator routeLocator, DiscoveryClient discoveryClient) {
        this.routeLocator = routeLocator;
        this.discoveryClient = discoveryClient;
    }

    /**
     * {@code GET  /routes} : get the active routes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the list of routes.
     */
    @ApiOperation(value = "查询路由列表", tags = "路由")
    @GetMapping("/routes")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<List<RouteVM>> activeRoutes() {
        List<Route> routes = routeLocator.getRoutes();
        List<RouteVM> routeVms = new ArrayList<>();
        routes.forEach(route -> {
            RouteVM routeVm = new RouteVM();
            routeVm.setPath(route.getFullPath());
            routeVm.setServiceId(route.getId());
            routeVm.setServiceInstances(discoveryClient.getInstances(route.getLocation()));
            routeVms.add(routeVm);
        });
        return ResponseEntity.ok(routeVms);
    }
}
