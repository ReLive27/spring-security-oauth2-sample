### 相关信息:

该模块提供支持密钥轮换策略的`JWKSource`。 RotateJwkSource 是一种JWKSource利用 JWKSetCache 来支持密钥轮换实现。

除了工程中默认 Redis 实现方式，还支持本地内存和Caffeine实现方式。

以下代码显示了如何配置本地内存密钥轮换策略的`RotateJwkSource`提供支持的示例：

```
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return new RotateJwkSource<>(new InMemoryJWKSetCache());
    }
```

`RotateJwkSource`还支持使用Caffeine的密钥轮换策略，可以按如下方式配置：

```
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return new RotateJwkSource<>(new CaffeineJWKSetCache());
    }
```

### 相关文章:

- [Spring Security OAuth2实现简单的密钥轮换及配置资源服务器JWK缓存](https://relive27.github.io/blog/jwk-cache-and-rotate-key)
