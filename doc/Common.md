# 公共类使用说明

### 主要类说明

##### LoggingAspect, LoggingAspectConfiguration

统一日志拦截配置

- 拦截 `@Repository`, `@Service`, `@RestController` 标注的类下所有方法
- 拦截 `com.yqlsc.*.repository`, `com.yqlsc.*.service`, `com.yqlsc.*.web.rest` 包下所有类及方法

##### @AuthorizedFeignClient, OAuth2InterceptedFeignConfiguration

- 提供服务内部机器调用 **FeignClient** 自定义注解
- **FeignClient** 客户端模式鉴权默认配置类, 可自行扩展

##### @AuthorizedUserFeignClient, OAuth2UserClientFeignConfiguration, UserFeignClientInterceptor

- 提供浏览器 Web 调用 **FeignClient** 自定义注解
- **FeignClient** Web token 传递处理配置类以及 **feign** 拦截器实现类

##### AuditEventConverter

`actuate` 审计处理类

##### OAuth2JwtAccessTokenConverter

JWT access token 处理类

##### OAuth2Properties

oauth2 配置属性类, 自动设置 `application-*.yml` 中 `oauth2` 下的配置值

##### AsyncConfiguration

- 异步配置, 可方便使用 `@Async` 注解
- 任务调度配置, 可方便使用 `@Scheduled` 注解

##### DatabaseConfiguration

数据库配置类

- `@EntityScan("com.yqlsc.*.domain")` 自动扫描指定包下标注 `@Entity` 的实体类
- `@EnableJpaRepositories("com.yqlsc.*.repository")` 自动扫描指定包下标注 `@Repository` 的仓库类

##### DateTimeFormatConfiguration

WebMvc 时间格式化配置类

##### FeignConfiguration

feign 相关配置类

- `@EnableFeignClients(basePackages = "com.yqlsc")` 开启自动扫描自定包下标注 `@FeignClient` 的接口
- 注入 **feign** 调用的日志级别配置

##### JacksonConfiguration

注入 **jackson** 各种序列化器

##### LiquibaseConfiguration

Liquibase 配置类

##### OpenApiConfiguration

swagger 相关配置

##### RestTemplateConfiguration

RestTemplate 相关配置

##### UaaSignatureVerifierClient

从 uaa 获取公钥实现类

##### SecurityUtils

获取当前登陆的用户信息工具类