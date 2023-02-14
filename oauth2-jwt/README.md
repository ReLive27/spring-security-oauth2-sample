### 相关信息:

该模块演示了如何让我们的 Spring Authorization Server 实现使用 JSON Web 令牌。在启动工程前，你需要更改`oauth2-server`中`application.yml`文件 MySQL
的用户名和密码，SQL表的创建使用 [Flyway](https://flywaydb.org) 数据库版本控制组件。

你可以使用 Docker 快速启动一个MySQL数据库：

```
docker run -p 3306:3306 --name mysql \
-v /usr/local/mysql/conf:/etc/mysql/conf.d \
-v /usr/local/mysql/logs:/logs \
-v /usr/local/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=123456 \
-d mysql:5.6
```

启动服务后，浏览器访问 [http://127.0.0.1:8070/client/test](http://127.0.0.1:8070/client/test) ,通过输入 admin/password 认证成功后，最终将返回资源信息。

### 相关文章:

- [将JWT与Spring Security OAuth2结合使用](https://relive27.github.io/blog/spring-security-oauth2-jwt)
