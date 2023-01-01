### 相关信息:

该模块演示了 OAuth2.0 授权服务通过 Consul 实现密钥轮换，如果您并不了解 Consul，请利用一些时间阅读 [Consul](https://www.consul.io/) 官网提供的信息。 Consul KV Store
提供了一个分层的KV存储， 能够存储分布式键值，我们将利用 Consul KV Store 使资源服务器发现授权服务器的公钥信息， 授权服务器轮换密钥后将公钥通过HTTP API更新到 KV Store。

### 相关文章:

- [将Spring Security OAuth2授权服务JWK与Consul 配置中心结合使用](https://relive27.github.io/blog/oauth2-jwk-consul-config)
