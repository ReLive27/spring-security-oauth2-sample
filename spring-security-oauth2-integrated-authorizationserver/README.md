
## 授权码模式
**获取授权码：**
<br>
GET `http://localhost:8080/oauth2/authorize?response_type=code&client_id=relive-client&scope=profile&state=some-state&redirect_uri=http://127.0.0.1:8070/login/oauth2/code/messaging-client-oidc`

**获取token**
<br />
POST `http://localhost:8080/oauth2/token?grant_type=authorization_code&client_id=relive-client&code=code&redirect_uri=http://127.0.0.1:8070/login/oauth2/code/messaging-client-oidc&client_secret=relive-client&scope=profile`

## JWK
获取jwk，用于资源服务器验证token
```
http://localhost:8080/oauth2/jwks
```

## 提供者配置端点或授权服务器元数据端点
`http://127.0.0.1:8080/.well-known/oauth-authorization-server`

响应
```
{
    "issuer": "http://auth-server:8080",
    "authorization_endpoint": "http://auth-server:8080/oauth2/authorize",
    "token_endpoint": "http://auth-server:8080/oauth2/token",
    "token_endpoint_auth_methods_supported": [
        "client_secret_basic",
        "client_secret_post"
    ],
    "jwks_uri": "http://auth-server:8080/oauth2/jwks",
    "response_types_supported": [
        "code"
    ],
    "grant_types_supported": [
        "authorization_code",
        "client_credentials",
        "refresh_token"
    ],
    "revocation_endpoint": "http://auth-server:8080/oauth2/revoke",
    "revocation_endpoint_auth_methods_supported": [
        "client_secret_basic",
        "client_secret_post"
    ],
    "introspection_endpoint": "http://auth-server:8080/oauth2/introspect",
    "introspection_endpoint_auth_methods_supported": [
        "client_secret_basic",
        "client_secret_post"
    ],
    "code_challenge_methods_supported": [
        "plain",
        "S256"
    ]
}
```