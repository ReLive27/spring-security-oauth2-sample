![video_spider](https://socialify.git.ci/ReLive27/spring-security-oauth2-sample/image?forks=1&issues=1&language=1&name=1&owner=1&stargazers=1&theme=Light)

# <font size="6p">spring-oauth2-sample</font> <font size="5p">  | [中文文档](README_CN.md)</font>

<p align="left">
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/stargazers"><img src="https://img.shields.io/github/stars/ReLive27/spring-security-oauth2-sample?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/network/members"><img src="https://img.shields.io/github/forks/ReLive27/spring-security-oauth2-sample?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/watchers"><img src="https://img.shields.io/github/watchers/ReLive27/spring-security-oauth2-sample?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/issues"><img src="https://img.shields.io/github/issues/ReLive27/spring-security-oauth2-sample.svg?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/blob/main/LICENSE"><img src="https://img.shields.io/github/license/ReLive27/spring-security-oauth2-sample.svg?style=flat-square"></a>
</p>

This project is a collection of [Spring Authorization Server](https://spring.io/projects/spring-authorization-server)
example tutorials. It builds on top of Spring Security 6, the modules here cover many aspects of Spring Authorization
Server.

> 💡 Note: Don’t forget to give a star🌟 to the project if you like it!

## Relevant Articles:

- [Using JWT with Spring Security OAuth2](https://relive27.github.io/blog/spring-security-oauth2-jwt)
- [Custom OAuth2 Authorization Consent Page](https://relive27.github.io/blog/custom-oauth2-consent-page)
- [Spring Security Persistent OAuth2 Client](https://relive27.github.io/blog/persisrence-oauth2-client)
- [Spring Security OAuth2 Client Credentials Authorization](https://relive27.github.io/blog/oauth2-client-model)
- [Authorization Code Flow with Proof Key for Code Exchange (PKCE)](https://relive27.github.io/blog/oauth2-pkce)
- [Spring Security OAuth2 Login](https://relive27.github.io/blog/oauth2-login)
- [Spring Security and OpenID Connect](https://relive27.github.io/blog/springn-security-oidc)
- [Spring Cloud Gateway Combined with the Security Practice of OAuth2.0 Protocol](https://relive27.github.io/blog/spring-gateway-oauth2)
- [Spring Security OAuth2 implements Simple Key Rotation and Configures Resource Server JWK Cache](https://relive27.github.io/blog/jwk-cache-and-rotate-key)
- [Using Spring Security OAuth2 Authorization Service JWK with Consul Configuration Center](https://relive27.github.io/blog/oauth2-jwk-consul-config)
- [A Simple Guide to Using Spring Security OAuth2 Opaque Tokens](https://relive27.github.io/blog/oauth2-opaque-token)
- [Spring Security OAuth2 Introspection Protocol in conjunction with JWT Guide](https://relive27.github.io/blog/oauth2-introspection-with-jwt)
- [Spring Security OAuth 2.0 Authorization Server Combined with Redis implements Access to AccessToken Rate Limits](https://relive27.github.io/blog/oauth2-token-access-restrictions)
- [Building an OAuth2 Authorization Consent Page with Vue.js](https://relive27.github.io/blog/oauth2-custom-consent-page-with-vue)
- [(Stay Tuned) OAuth2.0 Device Code Authorization Process]()
- [(Stay Tuned) Register a client dynamically]()
- [(Stay Tuned) The resource server retrieves JWK via HTTP, File, Minio, or Vault]()
- ...

## Description of applicable version

| Project Branch  | Spring Security  | Spring Authorization Server  |  
| -----  |----------------- |  -------  |
| main  |  6.1.5  |  1.2.1    |
| 1.0.1  |  6.0.2  |  1.0.1    |
| 0.4.1  |  5.7.7  |    0.4.1  |
| 0.3.1  | 5.6.3   |  0.3.1    |

## Building from Source

spring-oauth2-sample uses a [Maven](https://maven.apache.org/) based build system.

### Prerequisites

[Git](https://help.github.com/set-up-git-redirect) and
the [JDK17 build](https://www.oracle.com/technetwork/java/javase/downloads).

Be sure that your `JAVA_HOME` environment variable points to the `jdk-17.0.5` folder extracted from the JDK download.

### Check out sources

```
git clone git@github.com:ReLive27/spring-security-oauth2-sample.git
```

### Build and Install the Project:

```
./mvn clean install -Dmaven.test.skip=true
```

## Contributing

[Pull requests](https://help.github.com/articles/creating-a-pull-request) are welcome.

## License

spring-oauth2-sample is Open Source software released under the
[Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
