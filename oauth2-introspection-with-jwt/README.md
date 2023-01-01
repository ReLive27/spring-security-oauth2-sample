
### 相关信息:
1.`oauth2-server-introspction-with-jwt`是一个使用*spring-security-oauth2-authorization-server*构建的授权服务器。

2.`oauth2-server-introspction-with-jwt`注册了一个 OAuth2.0 客户端：
- clientId: relive-client
- clientSecret: relive-client
- redirectUri: http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code

3.`oauth2-server-introspction-with-jwt`注册了一个用户，用户名密码为：`admin`/`password` 。

4.`resourceserver-introspection-with-jwt`是一个SpringBoot的资源服务，您可以修改`application.yml`数据库配置以连接您的MySQL数据库。

5.`oauth2-client-introspection-with-jwt`是一个 OAuth2.0 客户端服务。

6.启动所有SpringBoot服务后，浏览器访问 http://127.0.0.1:8070/client/test 进行测试。

### 相关文章:

- [Spring Security OAuth2 内省协议与 JWT 结合使用指南](https://relive27.github.io/blog/oauth2-introspection-with-jwt)
