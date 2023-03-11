## Relevant Information:

1. `client-model-oauth2-server` is an authorization server built with Spring Authorization Server.<br>
2. `client-model-resource-server` is served by SpringBoot resources protected by Spring Security, providing
   resource/article protected API.<br>
3. `oauth2client` is an OAuth2.0 client service created by Spring Security.<br>
4. After starting the service, `oauth2client` calls the resource service resource/article protected API from *
   ArticleJob* scheduled task.

## Relevant Articles:

- [Spring Security OAuth2 Client Credentials Authorization](https://relive27.github.io/blog/oauth2-client-model)
