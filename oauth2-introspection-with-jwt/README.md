## Relevant Information:

1. `oauth2-server-introspction-with-jwt` is an authorization server built with Spring Authorization Server.
2. `oauth2-server-introspction-with-jwt` registered an OAuth2.0 client:
    - clientId: relive-client
    - clientSecret: relive-client
    - redirectUri: http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code


3. `oauth2-server-introspction-with-jwt` has registered a user, the username and password are: `admin`/`password`.

4. `resourceserver-introspection-with-jwt` is a SpringBoot resource service, you can modify the `application.yml`
   database configuration to connect to your MySQL database.
5. `oauth2-client-introspection-with-jwt` is an OAuth2.0 client service.
6. After starting all SpringBoot services, the browser
   visits [http://127.0.0.1:8070/client/test](http://127.0.0.1:8070/client/test) for testing.

## Relevant Articles:

- [Spring Security OAuth2 Introspection Protocol in conjunction with JWT Guide](https://relive27.github.io/blog/oauth2-introspection-with-jwt)
