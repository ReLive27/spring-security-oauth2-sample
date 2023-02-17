### 相关信息：

1.`client-model-oauth2-server`是一个使用 Spring Authorization Server 构建的授权服务器。<br>
2.`client-model-resource-server`由 Spring Security 保护的 SpringBoot 资源服务，提供resource/article受保护API。<br>
3.`oauth2client`由 Spring Security 创建的 OAuth2.0 客户端服务。<br>
4.启动服务后，`oauth2client`由*ArticleJob*定时任务调用资源服务resource/article受保护API。

### 相关文章：

- [Spring Security OAuth2客户端凭据授权](https://relive27.github.io/blog/oauth2-client-model)
