# Microservice 使用说明

Microservice 为业务微服务模板，如需新建其他微服务时只需拷贝并修改项目名, 微服务名, 包名, 配置即可。

注: 有以下修改点:

- 项目名称
- `com.example.microservice` 中 `microservice` 包名
- `com.example.microservice.MicroserviceApp` 中 `MicroserviceApp` 类名
- `bootstrap-*.yml`, `application-*.yml` 中包含 `microservice` 的值
- `pom.xml` 中包含 `microservice` 的值
- `classpath:templates/index.html` 中包含 `microservice` 的值

### 业务微服务

详情请查看 [swagger](http://localhost:8081) 文档