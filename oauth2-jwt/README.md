## Spring Security OAuth2 with JWT

---
### Relevant information:
1.`oauth2-server`是一个使用*spring-security-oauth2-authorization-server*构建的授权服务器。<br>
2.`oauth2-server`注册了一个客户端：
  - clientId: relive-client
  - clientSecret: relive-client
  - redirectUri: http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code

3.`oauth2-server`注册了一个用户，`admin`/`password`<br>
4.`resource-server`是一个SpringBoot的资源服务，提供/resource/test受保护API。
5.oauth2-client是一个客户端。<br>
6.测试启动服务后，访问http://127.0.0.1:8070/client/test
