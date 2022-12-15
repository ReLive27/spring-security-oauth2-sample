
### 相关信息:
1.`oauth2-server`是一个使用*spring-security-oauth2-authorization-server*构建的授权服务器。<br>
2.`oauth2-server`注册了一个客户端：
  - clientId: relive-client
  - clientSecret: relive-client
  - redirectUri: http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code

3.`oauth2-server`注册了一个用户，`admin`/`password`<br>
4.`resource-server`是一个SpringBoot的资源服务，提供/resource/test受保护API。<br>
5.oauth2-client是一个客户端。<br>
6.数据库表创建使用[Flyway](https://flywaydb.org)数据库版本控制组件，只需更改数据库用户名密码启动程序。<br>
7.测试启动服务后，访问http://127.0.0.1:8070/client/test

### 相关文章:
- [将JWT与Spring Security OAuth2结合使用](https://relive27.github.io/blog/spring-security-oauth2-jwt)
