## Relevant Information:

1. `idp` uses the identity provider service built
   with [Spring Authorization Server](https://spring.io/projects/spring-authorization-server).

2. `idp` registers a client by default:
    - **clientId**: relive-client
    - **clientSecret**: relive-client
    - **redirectUri**: http://127.0.0.1:8070/login/oauth2/code/messaging-client-oidc
    - **scope**: openid profile email

3. `idp` uses Form authentication, username and password are admin/password.

4. `rp` Relying Party Services built
   with [Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html). It includes
   Form authentication and OIDC authentication supported by itself.

5. `rp`uses the *Form Authentication* in to the user (admin/password) and assigns the ROLE_SYSTEM role. Users with this role
   log in successfully and jump to the home page to see the following:

   **Article List**

    - Java
    - Python
    - C++

6. `rp` will also support OIDC login. Use OidcRoleMappingUserService role mapping to map the `idp` service
   **ROLE_ADMIN** role to the rp service **ROLE_OPERATION** role. Users with **ROLE_OPERATION** will see the following
   content on the homepage after successful login:

   **Article List**

    - Java

7. The `rp` service database table is created using the [Flyway](https://flywaydb.org/) database version control
   component, just change the database username and password to start the program.

8. After the test starts the service, visit [http://127.0.0.1:8070/login](http://127.0.0.1:8070/login).

## Involving database table structure

The following is the database table structure related to the `rp` service:

![](./images/oauth2_sql_model.png)

## Relevant Articles:

- [Spring Security and OpenID Connect](https://relive27.github.io/blog/springn-security-oidc)
