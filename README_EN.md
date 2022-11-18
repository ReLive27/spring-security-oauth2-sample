# <font size="6p">spring-oauth2-sample</font> <font size="5p">  | [中文文档](README.md)</font>

<p align="left">
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/stargazers"><img src="https://img.shields.io/github/stars/ReLive27/spring-security-oauth2-sample?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/network/members"><img src="https://img.shields.io/github/forks/ReLive27/spring-security-oauth2-sample?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/watchers"><img src="https://img.shields.io/github/watchers/ReLive27/spring-security-oauth2-sample?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/issues"><img src="https://img.shields.io/github/issues/ReLive27/spring-security-oauth2-sample.svg?style=flat-square&logo=GitHub"></a>
	<a href="https://github.com/ReLive27/spring-security-oauth2-sample/blob/main/LICENSE"><img src="https://img.shields.io/github/license/ReLive27/spring-security-oauth2-sample.svg?style=flat-square"></a>
</p>
This module contains information on using Spring Security OAuth2

## Relevant Articles:

- [Using JWT with Spring Security OAuth2](https://relive27.github.io/blog/spring-security-oauth2-jwt)
- [Custom OAuth2 Authorization Consent Page](https://relive27.github.io/blog/custom-oauth2-consent-page)
- [Spring Security persistent OAuth2 client](https://relive27.github.io/blog/persisrence-oauth2-client)
- [Spring Security OAuth2 Client Credentials Authorization](https://relive27.github.io/blog/oauth2-client-model)
- [Spring Security OAuth2 Authorization Code Flow with Proof Key for Code Exchange (PKCE)](https://relive27.github.io/blog/oauth2-pkce)
- [Spring Security OAuth2 Login](https://relive27.github.io/blog/oauth2-login)
- [Spring Security and OpenID Connect](https://relive27.github.io/blog/springn-security-oidc)
- [Using Spring Cloud Gateway with OAuth2 Pattern](https://relive27.github.io/blog/spring-gateway-oauth2)
- [Spring Security OAuth2 implements simple key rotation and configures resource server JWK cache](https://relive27.github.io/blog/jwk-cache-and-rotate-key)
- [Using Spring Security OAuth2 Authorization Service JWK with Consul Configuration Center](https://relive27.github.io/blog/oauth2-jwk-consul-config)
- [（Coming soon）Spring Security OAuth2 and dynamic client registration]()
- [A Simple Guide to Using Spring Security OAuth2 Opaque Tokens](https://relive27.github.io/blog/oauth2-opaque-token)
- [（Coming soon）Spring Security OAuth2 Introspection Protocol in conjunction with JWT Guide]()
- [Spring Security OAuth 2.0 authorization server combined with Redis implements access to accessToken rate limits](https://relive27.github.io/blog/oauth2-token-access-restrictions)
- [（Coming soon）Spring Security implements OAuth2.0 protocol core interface guide]()
- ...

## Version

| spring boot   | spring cloud  | Spring Authorization Server  |
| ---------------- | ----------------- |----------------- |
| 2.6.7            | 2021.0.2          | 0.3.1            |

## Building from Source
spring-oauth2-sample uses a [Maven](https://maven.apache.org/) based build system.

### Prerequisites
[Git](https://help.github.com/set-up-git-redirect) and the [JDK8 build](https://www.oracle.com/technetwork/java/javase/downloads).

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.8.0` folder extracted from the JDK download.

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
