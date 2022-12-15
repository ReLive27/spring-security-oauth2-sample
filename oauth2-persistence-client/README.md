
### 相关信息:
1.`oauth2server`是一个使用[Spring Authorization Server](https://spring.io/projects/spring-authorization-server) 构建的授权服务器。
<br>
2.`oauth2server`使用简单的Form表单认证(*admin*/*password*)。
<br>
3.`persistence-client`作为OAuth2客户端，Spring Security存储OAuth2客户端信息默认实现*InMemoryClientRegistrationRepository*，通过实现ClientRegistrationRepository自定义持久化存储库*JdbcClientRegistrationRepository*。
<br>
4.`persistence-client`所需数据库表创建由[Flyway](https://flywaydb.org/) 管理。
<br>
5.`persistence-client`默认使用表单认证(*admin*/*password*)。
<br>
6.`persistence-client-resource-server`作为资源服务器，提供/*resource*/*article*API接口。
<br>
7.启动服务后，访问接口：http://127.0.0.1:8070/client/test, 最终将返回`persistence-client-resource-server`资源服务器/*resource*/*article*接口响应信息。


### 相关文章:
- [Spring Security 持久化OAuth2客户端](https://relive27.github.io/blog/persisrence-oauth2-client)
