# 网关服务

##### maven 配置

```xml

<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>aliyun</name>
        <url>https://maven.aliyun.com/repository/central/</url>
    </mirror>

    <mirror>
        <id>spring-milestones</id>
        <mirrorOf>milestone</mirrorOf>
        <name>spring-milestones</name>
        <url>https://repo.spring.io/milestone/</url>
    </mirror>

    <mirror>
        <id>alfresco</id>
        <mirrorOf>public</mirrorOf>
        <name>alfresco</name>
        <url>https://artifacts.alfresco.com/nexus/content/repositories/public/</url>
    </mirror>
</mirrors>
```

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
