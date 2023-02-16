### 相关信息:

本例`persistence-client`服务中我们通过实现 ClientRegistrationRepository 接口自定义持久化存储库 *JdbcClientRegistrationRepository*
。在我们启动服务测试之前，你需要拥有一个MySQL数据库。 你可以使用 Docker 快速启动一个MySQL数据库：

```
docker run -p 3306:3306 --name mysql \
-v /usr/local/mysql/conf:/etc/mysql/conf.d \
-v /usr/local/mysql/logs:/logs \
-v /usr/local/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=123456 \
-d mysql:5.6
```

在你将`application.yml`中数据库用户名密码修改后，使用浏览器访问 [http://127.0.0.1:8070/client/test](http://127.0.0.1:8070/client/test)
，其中登录所需用户名密码为 admin/password 。

### 相关文章:

- [Spring Security 持久化OAuth2客户端](https://relive27.github.io/blog/persisrence-oauth2-client)
