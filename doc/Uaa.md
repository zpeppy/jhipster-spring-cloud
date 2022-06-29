# UAA 使用说明

UAA(User Account and Authentication) 是使用 *OAuth2 授权协议* 的保护微服务安全的用户审计和授权服务。

UAA 是一个完全可配置的 OAuth2 授权服务器，其中包含用户管理和角色管理。

### 概述

##### 架构图

> ![microservice-architecture](images/microservice-architecture.png)

##### 中央认证

UAA 作为众多微服务的 SSO 单点登录服务，认证、授权管理中心。

##### 无状态

UAA 是由 Spring Security + OAuth2 + JWT 实现，通过 HTTP Header 传值方式代替 Session，从而达到无状态服务，方便弹性伸缩，可扩展。

##### 鉴权方式

支持用户 / 机器访问鉴权

```yaml
# application-dev.yml
# 用户名密码模式配置, 必须和其他微服务保持一致, 其他环境请自行修改
uaa:
  key-store:
    name: config/tls/keystore.p12
    password: password
    alias: selfsigned
  web-client-configuration:
    # Access Token is valid for 1 days
    access-token-validity-in-seconds: 86400
    # Refresh Token is valid for 7 days
    refresh-token-validity-in-seconds-for-remember-me: 604800
    client-id: web_app
    secret: changeit
```

```yaml
# application-dev.yml
# 客户端模式配置, 必须和其他微服务保持一致, 其他环境请自行修改
jhipster:
  security:
    client-authorization:
      client-id: internal
      client-secret: internal
```

##### 微服务调用链

> ![microservice-flows](images/microservice-flows.png)

- 对架构任何端点的每个请求都是通过 *client* 执行的
- 端点（包括 UAA）上服务资源的每个微服务都是资源服务器
- 蓝色箭头显示客户端在 OAuth2 授权服务器上进行身份验证
- 红色箭头显示客户端在资源服务器上执行的请求
- UAA 服务器是授权服务器和资源服务器的组合
- UAA 服务器是微服务应用程序内所有数据的所有者（它会自动批准对资源服务器的访问）
- 通过用户身份验证访问资源的客户端，使用带有客户端 ID 的 *password grant* 进行身份验证，并安全存储在网关配置文件中
- 在没有用户的情况下访问资源的客户端使用 *client credentials grant* 进行身份验证

### 使用 UAA

##### 了解组件

- 自带默认的用户域，其中包含用户和帐户资源（由 JWT 身份验证中的网关完成）以及角色资源
- 为 OAuth2 实现 `AuthorizationServerConfigurerAdapter`，并定义了基本客户端（*web_app* 和 *internal*）
- 在 `/oauth/token_key` 上提供 JWT 公钥，所有其他微服务都必须使用它

在其他微服务启动时，通常期望 UAA 服务已经启动以共享其公钥。该服务首先调 `/oauth/token_key` 来获取公钥并对其进行配置以进行密钥签名（`JwtAccessTokenConverter`）

如果 UAA 服务没有启动，则该服务将继续启动并在以后获取公钥。有两个属性 `oauth2.signature-verification.ttl`
控制密钥在再次获取之前的生存时间，`oauth2.signature-verification.public-key-refresh-rate-limit`
限制了对 UAA 的请求以避免发送无用请求。如果验证失败，则微服务将检查是否有新密钥。这样可以在 UAA 上更换密钥，并且服务将会同步。

在此基本设置中可能会发生两种调用方式：用户调用和机器调用

- 对于用户调用，登录请求将发送到网关(gateway)的 `/auth/login` 接口。 该接口使用 `OAuth2TokenEndpointClientAdapter` 将请求转发给 UAA，并使用 *password*
  授权进行身份验证。由于此请求发生在网关上，因此客户端 ID 和密码不会存储在任何客户端代码中，并且用户无法访问。网关返回一个包含 *token* 的新 Cookie，该 Cookie 与客户端执行的每个请求一起发送到微服务后端。
- 对于机器调用，机器必须使用客户端凭据授予作为 UAA 进行身份验证。可自行实现。

##### 刷新令牌

刷新访问令牌的一般在网关上进行，如下所示：

- 身份验证是通过 *gateway* 的 `AuthResource` 调用 `OAuth2AuthenticationService` 的身份验证来完成的，该身份验证将设置Cookie。
- 对于每个请求，*gateway* 的 `RefreshTokenFilter`（由 `RefreshTokenFilterConfigurer` 生成）检查访问令牌是否已过期以及它是否具有有效的刷新令牌。
- 如果令牌过期，则将通过 *gateway* 的 `OAuth2AuthenticationService.refreshToken()` 触发刷新过程。
- 这使用  *gateway* 的 `OAuth2TokenEndpointClient` 接口将刷新令牌授权发送到所选的 UAA 服务器（通过 `UaaTokenEndpointClient` 实现类）。
- 然后，刷新授予的结果将在下游用作新 cookie，并在上游（对于浏览器）将其设置为新 cookie。

##### 常见问题

- 建议不同环境尽可能使用不同的签名密钥。一旦签名密钥被人使用，就有可能在不知道任何用户登录凭据的情况下生成完全访问授权密钥。
- 建议使用 TLS。如果攻击者设法拦截访问令牌，则他将获得对该令牌授权的所有权限，直到令牌过期。有很多方法可以实现这一点，尤其是在没有 TLS 加密的情况下。
- 建议不要在 URL 中使用访问令牌。访问令牌可以通过 URL，HTTP 头部或 Cookie 传递。从 TLS 的角度来看，所有三种方式都是安全的。实际上，通过 URL 传递令牌的安全性较差，因为存在几种从记录中获取 URL 的方法。
- 建议使用非对称签名密钥。JWT 签名不需要 RSA，Spring Security 也提供对称令牌签名。这也解决了一些使开发更加困难的问题。但这是不安全的，因为攻击者需要进入一个微服务就可以生成自己的 JWT 令牌。

### 使用 Feign 客户端进行安全的服务间通信

UAA 提供了一种可扩展的安全服务间通信方法。

在不手动将 JWT 从请求转发到内部请求的情况下, 使用 JWT 身份验证会迫使微服务通过网关调用其他微服务，这涉及到每个主请求中的其他内部请求。但是即使进行转发，也无法完全区分用户身份验证和计算机身份验证。

UAA 基于 OAuth2，因此所有这些问题都可以通过协议定义解决。

##### 使用 Eureka / Nacos, Ribbon, Feign 和 Hystrix

当一个服务要向另一个服务请求数据时，最终这几个组件都开始起作用。

- Eureka: 这是服务（取消）注册的地方，因此您可以询问 *provider-service*，并获取在 **Eureka / Nacos** 中注册的 *provider-service* 实例的一组 IP。

- Ribbon: 当有要 *provider-service* 并已经检索到一组 IP 时，Ribbon 会在这些 IP 上进行负载均衡。

当我们获得一个 URL，例如 [http://uaa/oauth/token/](http://uaa/oauth/token/) ，其中有两个运行在 `10.10.10.1:9999` 和 `10.10.10.2:9999` 上的 UAA
服务器实例时，我们可以使用 **Eureka / Nacos** 和 **Ribbon** 使用 **Round Robin**
算法将该网址快速转换为 [http://10.10.10.1:9999/oauth/token](http://10.10.10.1:9999/oauth/token)
或 [http://10.10.10.2:9999/oauth/token](http://10.10.10.2:9999/oauth/token) 。

- Feign: 以接口声明式使用服务提供者接口
- Hystrix: 解决服务故障的断路器组件, 支持限流, 熔断, 降级

在真实环境中，不能保证所有服务的所有实例都可用。因此，**Hystrix** 充当断路器，使用后备以明确定义的方式处理故障情况。

但是，手动实现所有这些逻辑并进行编码将会带来很多工作：**Feign** 提供了为在 **Eureka / Nacos** 中注册的端点负载均衡 REST 客户端的选项，其后备实现由 **Hystrix**
控制，仅使用带有一些注解的 Java 接口即可。

因此，对于服务间通信，**Feign** 客户端非常有帮助。当一项服务需要 REST 客户端访问提供某些 *其他资源* 的 *其他服务* 时，可以声明如下接口：

```java

@FeignClient(name = "provider-service")
public interface ProviderFeignClient {

    @GetMapping(value = "/api/resources")
    List<ProviderResource> getResourcesFromProviderService();

}
```

然后，通过依赖项注入使用它，例如：

```java

@Service
public class ConsumerService {

    @Resource
    private ProviderFeignClient providerFeignClient;

}
```

与 Spring Data JPA 相似，不需要实现该接口。但是，如果使用 **Hystrix**，则可以这样做。**Feign** 客户端接口的已实现类并充当后备实现。

使用 UAA 确保这种通信的安全性。为此，应该有一些 **Feign** 的请求拦截器，该拦截器实现了来自 OAuth2 的客户端凭据流，以授权当前服务请求其他服务。可以使用 `@AuthorizedFeignClient`
或者 `@AuthorizedUserFeignClient`。

##### 使用 `@AuthorizedFeignClients`

考虑到上述 **Feign** 客户端应用于提供受保护资源的 *provider-service*，因此必须对接口进行如下注解：

```java
// 当内存中没有有效的访问令牌时，REST 客户端会自动获得 UAA 服务器的授权。
// 此方法解决了以下情况：机器请求在单独的 OAuth2 客户端上运行而不引用用户会话。这一点很重要，尤其是当对另一个服务中另一个请求发出的请求使用实体审核时。或者，可以将初始请求的访问令牌转发到其他服务。可自行实现。
@AuthorizedFeignClient(name = "provider-service")
public interface ProviderFeignClient {

    @GetMapping(value = "/api/resources")
    List<ProviderResource> getResourcesFromProviderService();

}

// 或者

// 用户访问携带访问令牌， 已实现
@AuthorizedUserFeignClient(name = "provider-service")
public interface ProviderFeignClient {

    @GetMapping(value = "/api/resources")
    List<ProviderResource> getResourcesFromProviderService();

}
```

### 主要类说明

##### ApplicationProperties

微服务自定义配置属性类, 自动填充 `application-dev.yml` 文件中 `application:` 下配置值

##### CacheConfiguration

缓存配置类, 方便使用 `@Cacheable`、`@CacheEvict`、 `@CachePut` 等缓存注解

##### UaaConfiguration

UAA 作为授权服务器相关配置

##### UaaProperties

uaa 属性配置类, 自动填充 `application-dev.yml` 文件中 `uaa:` 下配置值

##### UaaWebSecurityConfiguration

uaa 对 Web Security 认证相关配置

##### DomainUserDetailsService

实现用户账号认证逻辑

##### IatTokenEnhancer

实现生成 token 颁发的时间戳, 并填充到 JWT 中

### 主要配置说明

```yaml
# bootstrap.yml
spring:
  application:
    name: uaa # 微服务名称
  nacos:
    discovery: # nacos 注册中心
      server-addr: nacos:8848
      username: nacos
      password: nacos
      namespace: public
      group: DEFAULT_GROUP
    config: # nacos 配置中心, 注: 配置中心相关配置必须放在 bootstrap-*.yml 文件中
      server-addr: nacos:8848
      username: nacos
      password: nacos
      namespace: public
      group: DEFAULT_GROUP
      prefix: uaa
      file-extension: yml

---

# application.yml
management:
  endpoints:
    web:
      base-path: /management
      exposure: # 暴露 actuate 相关指标, 注: 其中包含了 prometheus
        include: [ 'configprops', 'env', 'health', 'info', 'jhimetrics', 'logfile', 'loggers', 'prometheus', 'threaddump' ]

spring:
  jpa:
    hibernate:
      ddl-auto: none # 默认程序启动不自动创建表, 需自动创建表改为 update, 那么需把 spring.liquibase.enabled 改为 false, 否则建表会有冲突

---

# application-dev.yml
spring:
  liquibase:
    enabled: true # liquibase 默认为开启
  sleuth:
    sampler:
      probability: 1 # sleuth 收集率为 100%
  zipkin:
    base-url: http://zipkin:9411
    enabled: true # 开启 zipkin 链路收集
    locator:
      discovery:
        enabled: true

jhipster:
  security:
    client-authorization: # 客户端模式, 机器内部调用配置, 须同其他微服务一致
      client-id: internal
      client-secret: internal

uaa:
  key-store:
    name: config/tls/keystore.p12 # TLS 证书配置, 须同其他微服务一致
    password: password
    alias: selfsigned
  web-client-configuration: # web 端访问, 用户名密码模式
    access-token-validity-in-seconds: 86400 # token 有效期 1天
    refresh-token-validity-in-seconds-for-remember-me: 604800 # refresh token 有效期 7 天
    client-id: web_app
    secret: changeit
```

### 使用 Liquibase 初始化数据

Liquibase 是一个数据库变更的版本控制工具。项目中通过 Liquibase 解析用户编写的 Liquibase 的配置文件，生成 sql 语句，并执行和记录。执行是根据记录确定 sql
语句是否曾经执行过，和配置文件里的预判断语句确定 sql 是否执行。

因项目自带 `Authority` 权限实体类和 `User` 用户实体类

配置好 Liquibase, 在项目启动时能帮我们自动初始化数据, 可查看 common 包中 `LiquibaseConfiguration` 配置类

- 程序会读取 `classpath:config/liquibase/master.xml` 文件,
- 然后按照 `classpath:config/liquibase/changelog/00000000000000_initial_schema.xml` 数据库约束文件创建表
- 最后按照 `classpath:config/liquibase/data/*.csv` 数据文件导入数据
- 在 `classpath:config/liquibase/data/user.csv` 中包含(账号密码: admin/admin) 用户

注: 使用该功能需修改 `spring.jpa.hibernate.ddl-auto=none`, 禁止 hibernate 自动建表, 否则会有冲突

### 接口服务说明

##### 账号管理接口: `AccountResource`

提供了用户账号管理, 包括: 账号注册, 修改密码等接口

##### 用户管理接口: `UserResource`

提供了用户管理, 包括: 新增用户, 修改用户, 查询用户列表, 删除用户, 查询所有角色等接口

详情请查看 [swagger](http://localhost:9999) 文档