## Relevant Information:

This module demonstrates that the OAuth2.0 authorization service implements key rotation through Consul. If you don't
know Consul, please take some time to read the information provided by [Consul](https://www.consul.io/) is official
website. Consul KV Store provides a hierarchical KV store that can store distributed key values. We will use Consul KV
Store to enable the resource server to discover the public key information of the authorization server. After the
authorization server rotates the key, the public key will be updated to KV Store.

Let's test it out by visiting [http://127.0.0.1:8070/client/article](http://127.0.0.1:8070/client/article) in your browser!


## Relevant Articles:

- [Using Spring Security OAuth2 Authorization Service JWK with Consul Configuration Center](https://relive27.github.io/blog/oauth2-jwk-consul-config)
