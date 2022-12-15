
### 相关信息:
1.`pkce-oauth2-server`是一个使用[spring-security-oauth2-authorization-server](https://spring.io/projects/spring-authorization-server)构建的授权服务器。<br> 

2.`pkce-oauth2-server`注册了一个客户端：
- clientId: relive-client
- ClientAuthenticationMethod: 认证方式NONE，以支持PKCE
- redirectUri: http://127.0.0.1:8070/login/oauth2/code/messaging-client-pkce
- scope: message.read

3.`pkce-oauth2-server`使用Form表单认证，用户名密码：admin/password <br>

4.`pkce-client`是一个OAuth2客户端，它将发起带有用于代码交换的证明密钥 (PKCE) 的授权码流。<br>

5.`pkce-resourceserver`是一个资源服务器，提供/resource/article 端点。<br>

6.启动服务后，访问http://127.0.0.1:8070/client/test， `pkce-client` 将通过WebClient请求资源服务/resource/article。

### 相关文章:
- [Spring Security OAuth2 带有用于代码交换的证明密钥 (PKCE) 的授权码流](https://relive27.github.io/blog/oauth2-pkce)
