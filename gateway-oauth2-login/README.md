## Gateway Combined With Spring Security OAuth2

### 问题
1.如果使用这种方式保护服务，相当于认证服务维护用户会话，gateway也需要维护用户会话<br>
2.认证服务用户退出登录，如何同步取消gateway用户会话<br>
3.集群部署时如何解决会话共享？认证服务和gateway会话是否需要共享？共享后gateway是否还会向认证服务请求授权获取token<br>

//TODO