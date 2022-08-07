# 网关服务

##### 网关过滤器配置

- 自定义过滤器，需修改 `application.yml` 配置
  ```yaml
  spring:
    cloud:
      gateway:
        default-filters:
          - JwtRelay # 对应 JwtRelayGatewayFilterFactory 类前缀
  ```

##### 鉴权配置

- 服务默认使用自身 JWT 认证，但支持切换至 UAA 鉴权，需修改 `application-dev.yml` 配置
  ```yaml
  application:
    useUaa: true # 使用 uaa 进行鉴权
  ```

##### 注意

- 启动报错可忽略, swagger 问题:
  > Skipping process path[/management/audits/events], method[getAll] as it has an error.

  > Skipping process path[/management/audits/events], method[getByDates] as it has an error.

  > Skipping process path[/api/users], method[getAllUsers] as it has an error.
