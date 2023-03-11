## Relevant Information:

This module demonstrates the authorization code flow of the code exchange proof key (PKCE). It is worth noting that when
we register the client with the authorization service, the `ClientAuthenticationMethod` selects the `none` method. After
we start the service, the browser visits [http://127.0.0.1:8070/client/test](http://127.0.0.1:8070/client/test) to test
the complete process.


> Remember that the username and password used for authentication are admin/password

## Relevant Articles:

- [Authorization Code Flow with Proof Key for Code Exchange (PKCE)](https://relive27.github.io/blog/oauth2-pkce)
