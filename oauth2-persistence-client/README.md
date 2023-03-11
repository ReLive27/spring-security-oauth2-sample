## Relevant Information:

In this example `persistence-client` service, we implement the custom persistence repository
JdbcClientRegistrationRepository. Before we start the service test, you need to have a MySQL database. You can quickly
start a MySQL database using Docker:

```
docker run -p 3306:3306 --name mysql \
-v /usr/local/mysql/conf:/etc/mysql/conf.d \
-v /usr/local/mysql/logs:/logs \
-v /usr/local/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=123456 \
-d mysql:5.6
```

After you modify the database username and password in `application.yml`, use a browser to
visit [http://127.0.0.1:8070/client/test](http://127.0.0.1:8070/client/test). The username and password required for
login are admin/password.

## Relevant Articles:

- [Spring Security Persistent OAuth2 Client](https://relive27.github.io/blog/persisrence-oauth2-client)
