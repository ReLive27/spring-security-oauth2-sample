## Relevant Information:

This module provides a `JWKSource` that supports key rotation strategies. RotateJwkSource is a JWKSource implementation
that utilizes JWKSetCache to support key rotation.

In addition to the default Redis implementation in the project, it also supports local memory and Caffeine
implementations.

The following code shows an example of how to configure the `RotateJwkSource` of the local in-memory key rotation
strategy to provide support:

```
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return new RotateJwkSource<>(new InMemoryJWKSetCache());
    }
```

`RotateJwkSource` also supports using Caffeine's key rotation strategy, which can be configured as follows:

```
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return new RotateJwkSource<>(new CaffeineJWKSetCache());
    }
```

## Relevant Articles:

- [Spring Security OAuth2 implements simple key rotation and configures resource server JWK cache](https://relive27.github.io/blog/jwk-cache-and-rotate-key)
