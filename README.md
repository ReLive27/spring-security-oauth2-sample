## spring-oauth2-sample
1. 首先配置域名解析，修改hosts文件，添加以下配置
```
127.0.0.1   auth-server
```

2.启动服务

3.访问接口`http://127.0.0.1/client/test`, 服务为认证会跳转到授权服务器认证授权，获取到token后访问资源服务器`/resource/test`接口