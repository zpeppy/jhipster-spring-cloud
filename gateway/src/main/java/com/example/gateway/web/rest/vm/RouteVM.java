package com.example.gateway.web.rest.vm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * View Model that stores a route managed by the Gateway.
 */
@ApiModel(value = "路由信息", description = "路由信息")
@Data
public class RouteVM {

    @ApiModelProperty("路径")
    private String path;

    @ApiModelProperty("服务 ID")
    private String serviceId;

    @ApiModelProperty("服务列表")
    private List<ServiceInstance> serviceInstances;

}
