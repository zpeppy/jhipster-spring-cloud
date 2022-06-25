# Gateway 使用说明

网关是普通的应用程序，因此您可以在该项目上使用常规的开发工作流，但它也充当微服务的统一入口。更具体地说，它为所有微服务提供 HTTP 路由和负载均衡，服务质量，安全性和 API 文档。

### 架构图

> ![microservice-architecture](images/microservice-architecture.png)

### 微服务调用链

> ![microservice-flows](images/microservice-flows.png)

### HTTP 请求使用网关进行路由

启动 UAA、网关和微服务后，它们将在 **Eureka / Nacos**
中注册自己（使用 `classpath:config/application.yml` 文件中的 `eureka.client.serviceUrl.defaultZone`
或者 `spring.cloud.nacos.discovery.server-addr` 地址值）。

网关将使用其应用程序名字自动将所有请求代理到微服务：例如，注册微服务 `microservice` 时，该请求在网关上的 `/gateway/microservice` URL 上可用。 如需修改前缀 `/gateway`
请修改 `zuul.prefix=/path` 值

例如，如果您的网关运行在 [http://localhost:8080](http://localhost:8080)
上，则可以指向 [http://localhost:8080/gateway/microservice/api/resources](http://localhost:8080/gateway/microservice/api/resources)
来获取微服务 `microservice` 服务的 `/api/resources` 资源。 如果您尝试使用 Web 浏览器执行此操作，请不要忘记 REST 资源在微服务中是默认保护的，因此您需要发送正确的 JWT
头（请参见下面的安全性要点），或在微服务的 `MicroserviceSecurityConfiguration` 类删除这些 URL 安全保护。

如果同一服务有多个运行的实例，则网关将从 **Eureka / Nacos** 获取这些实例，并将：

- 使用 **Spring Coud Load Balancer / Ribbon** 负载均衡 HTTP 请求。
- 使用 **Hystrix** 提供断路器，以便快速，安全地删除发生故障的实例。

网关可以在其中监视打开的 HTTP 路由和微服务实例。

如果同一服务有多个运行的实例，则网关将从 **Eureka / Nacos** 获取这些实例，并将： 使用 **Ribbon** 负载均衡 HTTP 请求。

### 限流

使用 **Bucket4j** 和 **Hazelcast** 提供微服务上的服务质量。

网关提供速率限制功能，因此可以限制 REST 请求的数量：

- 通过 IP 地址（对于匿名用户）
- 通过用户登录（对于已登录的用户）

然后，网关将使用 **Bucket4j** 和 **Hazelcast** 请求计数，并在超出限制时发送 HTTP 429（请求过多）错误。每个用户的默认限制是每小时 100000 个 API 调用。

这样可以保护微服务架构免于被特定用户的请求所雪崩。

网关在保护 REST 端点安全时，可以完全访问用户的安全信息，因此可以扩展它，以根据用户的安全角色提供特定的速率限制。

要启用速率限制，请打开 `application-*.yml` 文件，并将 `jhipster.gateway.rate-limiting.enabled` 设置为 `true`：

```yaml
# application-dev.yml
jhipster:
  gateway:
    rate-limiting:
      enabled: true # 开启
      limit: 100000 # 限制请求数量
      duration-in-seconds: 3600 # 持续时间(单位: 秒)
```

数据存储在 **Hazelcast** 中，因此，只要配置了 **Hazelcast** 分布式缓存，便可以扩展网关，该网关可以直接使用：

- 默认情况下，网关都配置了 **Hazelcast**
- 如果使用 **JHipster Registry(Eureka)**，则网关的所有实例都应自动在分布式缓存中注册自己

如果要添加更多规则或修改现有规则，则需要在 `RateLimitingFilter` 类中对其进行编码。修改示例：

- 降低 HTTP 调用的限制
- 增加每分钟或每天限制
- 取消 *admin* 用户的所有限制

### 访问控制策略

默认情况下，所有已注册的微服务都可以通过网关来访问。如果要排除通过网关公开访问的特定 API，可以使用网关的特定访问控制策略过滤器。可以使用 `application-*.yml`
文件中的 `jhipster.gateway.authorized-microservices-endpoints` 对其进行配置：

```yaml
# application-*.yml
jhipster:
  gateway:
    authorized-microservices-endpoints: # Access Control Policy, if left empty for a route, all endpoints will be accessible
      app1: /api,/v2/api-docs # recommended dev configuration
```

例如，如果您只希望微服务 `microservice` 的 `/api/resources` API 端点可用：

```yaml
jhipster:
  gateway:
    authorized-microservices-endpoints:
      microservice: /api/resources
```

### 主要类说明

##### OAuth2AuthenticationConfiguration

资源服务器配置

##### ApplicationProperties

同 [uaa.ApplicationProperties](Uaa.md#ApplicationProperties)

##### CacheConfiguration

同 [uaa.CacheConfiguration](Uaa.md#CacheConfiguration)

##### GatewayConfiguration

gateway 网关过滤器配置, 包含: swagger 路径过滤器, 鉴权访问控制过滤器, 限流过滤器

##### SecurityConfiguration

作为资源服务器, Web Security, JWT 安全配置

##### AccessControlFilter

鉴权访问控制过滤器实现类

##### RateLimitingFilter

限流过滤器实现类

##### CookieCollection

Cookie 集合处理类

##### CookiesHttpServletRequestWrapper

HttpServletRequest 以及 Cookie 包装类

##### CookieTokenExtractor

从 Cookie 中或者 HttpServletRequest 中获取 token 工具类

##### OAuth2AuthenticationService

oauth2 认证 service, 包含: 用户登陆认证转发, 退出登陆, 刷新 token 实现

##### OAuth2CookieHelper

Cookie 工具类, 用于从 HttpServletRequest 中获取 cookie, 创建 cookie, 清除 cookie, 校验 cookie 等操作

##### OAuth2TokenEndpointClientAdapter, UaaTokenEndpointClient

用于调用 uaa 进行用户认证, 刷新 token 实现类

##### RefreshTokenFilter, RefreshTokenFilterConfigurer

用于刷新 token 的 servlet 过滤器

### 主要配置说明

```yaml
# application.yml
zuul:
  sensitive-headers: Cookie,Set-Cookie
  host:
    max-total-connections: 1000
    max-per-route-connections: 100
  prefix: /gateway # url 路径前缀, 如需修改时, 需要同时修改 WebConfigurer.corsFilter 方法中 /gateway 值
  semaphore:
    max-semaphores: 500

---

# application-dev.yml
jhipster:
  gateway:
    rate-limiting:
      enabled: false # 是否需要开启限流
      limit: 100000
      duration-in-seconds: 3600
    authorized-microservices-endpoints: # 需要鉴权的接口地址
      app1: /api,/v2/api-docs
  security:
    client-authorization: # 客户端模式
      access-token-uri: http://uaa/oauth/token # 从 uaa 获取 token 地址
      token-service-id: uaa
      client-id: internal # 需要和 uaa 保持一致
      client-secret: internal # 需要和 uaa 保持一致

oauth2:
  signature-verification:
    public-key-endpoint-uri: http://uaa/oauth/token_key # 从 uaa 获取公钥地址
    ttl: 3600000 # 用户校验 JWT 的公钥的过期时间(单位 毫秒)
    public-key-refresh-rate-limit: 10000 # 最大限度获取公钥的速率(单位 毫秒)
  web-client-configuration: # 用户名密码模式
    client-id: web_app # 保持和 uaa 一致
    secret: changeit # 保持和 uaa 一致
    session-timeout-in-seconds: 1800 # 如果登陆时 rememberMe=false, 则不生成 refresh token 并把 access token 存放在 session 中的过期时间(单位: 秒)
```

### 接口服务说明

##### 认证管理接口: `AuthResource`

包含统一用户登陆认证接口 `/auth/login` 和 统一用户退出登录接口 `/auth/logout`

##### 路由管理接口: `GatewayResource`

提供查询 gateway 路由地址列表接口 `/api/gateway/routes`

详情请查看 [swagger](http://localhost:8080) 文档