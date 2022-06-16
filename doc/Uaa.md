# UAA 使用说明

UAA 是使用 OAuth2 授权协议的保护微服务安全的用户审计和授权服务。

UAA 是一个完全可配置的 OAuth2 授权服务器，其中包含用户和角色端点。

### 架构图

> ![microservice-architecture](images/microservice-architecture.png)

### 用户账号认证和授权服务

账号管理接口: `com.example.uaa.web.rest.AccountResource`

用户管理接口: `com.example.uaa.web.rest.UserResource`

详情请查看 [swagger](http://localhost:9999) 文档