## Relevant Information:

This module demonstrates how to get our Spring Authorization Server implementation to use JSON Web Token. Before
starting the project, you need to change the username and password of MySQL in the `application.yml` file
in `oauth2-server`. SQL tables are created using the [Flyway](https://flywaydb.org) database versioning component.

You can use Docker to quickly start a MySQL database:

```
docker run -p 3306:3306 --name mysql \
-v /usr/local/mysql/conf:/etc/mysql/conf.d \
-v /usr/local/mysql/logs:/logs \
-v /usr/local/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=123456 \
-d mysql:5.6
```

After starting the service, the browser visits [http://127.0.0.1:8070/client/test](http://127.0.0.1:8070/client/test).
After successful authentication by entering admin/password, the resource information will eventually be returned.

## Relevant Articles:

- [Using JWT with Spring Security OAuth2](https://relive27.github.io/blog/spring-security-oauth2-jwt)
