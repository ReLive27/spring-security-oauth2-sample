### 相关信息:

在该模块中我们使用 Spring Authorization Server 创建了一个 OAuth2.0
授权服务器，当我们在浏览器中测试访问 [http://localhost:8080/oauth2/authorize?response_type=code&client_id=relive-client&scope=message.write%20message.read%20profile&state=some-state&redirect_uri=http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code](http://localhost:8080/oauth2/authorize?response_type=code&client_id=relive-client&scope=message.write%20message.read%20profile&state=some-state&redirect_uri=http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code)
, 在输入用户名密码（admin/password）通过认证后，我们将看到如下自定义授权页面：<br />

![](./image/custom-page.png)

### 相关文章:

- [自定义OAuth2授权同意页面](https://relive27.github.io/blog/custom-oauth2-consent-page)
