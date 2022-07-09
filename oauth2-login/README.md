## Spring Security OAuth2 Login

### Relevant information:
1.`authorizationserver`作为授权服务器，提供自定义/userInfo用户信息端点。
<br><br>
2.`authorizationserver`注册了一个客户端：
- clientId: relive-client
- clientSecret: relive-client
- redirectUri: http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code
- scope: profile

3.`authorizationserver`使用Form表单认证，用户名密码：admin/password。
<br><br>
4.`oauth2-login-client`是一个OAuth2客户端，它包含Form表单登录和OAuth2登录。
<br><br>
5.`oauth2-login-client`Form登录用户（admin/password）赋予*ROLE_SYSTEM*角色，拥有该角色的用户登录成功将看到以下内容：
![](./images/form.png)
<br><br>
6.`oauth2-login-client`还将支持OAuth2登录，使用*GrantedAuthoritiesMapper*将`authorizationserver`授权服务角色*ROLE_ADMIN*映射为客户端服务*ROLE_OPERATION*角色，
登录成功后拥有*ROLE_OPERATION*角色的用户将看到以下内容：
![](./images/oauth2-login.png)
<br><br>
7.启动服务，访问http://127.0.0.1:8070/home ,首先使用Form表单登录，其次退出登录，使用OAuth2登录，您将看到不同的展示信息。