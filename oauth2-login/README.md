## Relevant Information:

1. `authorizationserver` acts as an authorization server and provides a custom /userInfo user information endpoint.

2. The `authorizationserver` registers a client:
    - clientId: relive-client
    - clientSecret: relive-client
    - redirectUri: http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code
    - scope: profile

3. `authorizationserver` uses *Form authentication*, username and password: admin/password.

4. `oauth2-login-client` is an OAuth2 client, which includes *Form login* and OAuth2 login.
 
5. The `oauth2-login-client` *Form* *login* user (admin/password) is assigned the ROLE_SYSTEM role, and users with this
   role will see the following content on the page after successful login:

   **Article List**
   
   - Java
   - Python
   - C++

6. `oauth2-login-client` will also support OAuth2 login. Make GrantedAuthoritiesMapper map the `authorizationserver`
   authorization service role **ROLE_ADMIN** to the client service **ROLE_OPERATION** role. Users with the **
   ROLE_OPERATION** role will see the following content on the page after successful login:

   **Article List**
   
   - Java


7. Start the service, visit [http://127.0.0.1:8070/home](http://127.0.0.1:8070/home). First use the username to log in,
   then log out, and use OAuth2 to log in, you will see different display information.

## Relevant Articles:

- [Spring Security OAuth2 Login](https://relive27.github.io/blog/oauth2-login)
