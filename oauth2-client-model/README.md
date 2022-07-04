## Spring Security Persistent OAuth2 Client

---
### Relevant information:
1.`client-model-oauth2-server`是一个使用*spring-security-oauth2-authorization-server*构建的授权服务器。<br>
4.`client-model-resource-server`是一个SpringBoot的资源服务，提供resource/article受保护API。<br>
5.`oauth2client`是一个OAuth2客户端。<br>
6.`oauth2client`数据库表创建使用[Flyway](https://flywaydb.org)数据库版本控制组件，只需更改数据库用户名密码启动程序。<br>
7.启动服务后，`oauth2client`由*ArticleJob*定时任务调用资源服务resource/article受保护API。
