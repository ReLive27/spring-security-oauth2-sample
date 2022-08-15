## Gateway Combined With Spring Security OAuth2

### Relevant information:

1.`auth-server`是一个使用[Spring Authorization Server](https://spring.io/projects/spring-authorization-server) 构建的授权服务器。

2.`auth-server`默认注册了一个OAuth2客户端：
 - **clientId**: relive-client
 - **clientSecret**: relive-client
 - **redirectUri**: http://127.0.0.1:8070/login/oauth2/code/messaging-gateway-oidc
 - **scope**: openid profile email read


3.`auth-server`默认认证方式使用Form表单认证，用户名密码为admin/password。

4.`auth-server`启动之初需要修改MySQL数据库用户名密码，数据库表初始化使用[Flyway](https://flywaydb.org/) 数据库版本控制组件。

5.`gateway-login`由[Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) 构建的API网关，启动前需要修改`application.yml`中**redis**配置。

6.`resourceserver`是一个简单的Spring Boot资源服务。

### Related database table structure:

以下提供了`auth-server`数据库表结构，相关SQL语句从[这里](https://github.com/ReLive27/spring-security-oauth2-sample/tree/main/gateway-oauth2-login/auth-server/src/main/resources/db/migration) 获取

![](./images/drawSQL-gateway-oauth2.png)
