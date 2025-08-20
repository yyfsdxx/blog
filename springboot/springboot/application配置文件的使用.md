# 📘 Spring Boot application.yml 配置文件笔记

> 本笔记整理了 application.yml 中配置的各项参数及其作用，适用于 Spring Boot 项目中配置文件的理解与使用。

---

## 🗂 配置文件层级结构

```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev
  main:
    allow-circular-references: true
  datasource:
    druid:
      driver-class-name: ${sky.datasource.driver-class-name}
      url: jdbc:mysql://${sky.datasource.host}:${sky.datasource.port}/${sky.datasource.database}?...
      username: ${sky.datasource.username}
      password: ${sky.datasource.password}

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.sky.entity
  configuration:
    map-underscore-to-camel-case: true

logging:
  level:
    com:
      sky:
        mapper: debug
        service: info
        controller: info

sky:
  jwt:
    admin-secret-key: itcast
    admin-ttl: 7200000
    admin-token-name: token
```

---

## 📌 配置说明

### ✅ server.port
设置服务端口为 8080。

### ✅ spring.profiles.active
指定激活的配置文件 profile（例如 application-dev.yml）。
application.yml 会自动加载对应的 application-dev.yml。

### ✅ spring.main.allow-circular-references
是否允许循环依赖，默认 false。
设为 true 可解决部分循环依赖报错。

### ✅ spring.datasource.druid
使用阿里巴巴 Druid 数据源，支持高性能连接池管理。
driver-class-name、url、username、password 从 sky 自定义配置中读取。

### ✅ mybatis
配置 MyBatis 的 Mapper XML 文件路径。
设置别名包路径。
驼峰映射开启（如 user_name -> userName）。

### ✅ logging.level
配置日志级别，方便调试：
- mapper 层：debug
- service 层和 controller 层：info

### ✅ sky.jwt
自定义配置，供 @ConfigurationProperties(prefix = "sky.jwt") 使用。
包含 JWT 密钥、有效时间和 Token 参数名。

---

## 💡 补充：application-dev.yml 是如何被读取的？

通过 spring.profiles.active=dev 激活。
Spring Boot 会自动合并加载 application.yml 和 application-dev.yml，后者中的配置会覆盖前者中相同项。

---

## ⚠️ 注意：属性命名规范

- application.yml 中的属性名采用 **短横线命名法（kebab-case）**
- Java 中若使用 @ConfigurationProperties 自动注入，字段需使用 **驼峰命名法（camelCase）**

例如：

```yaml
sky:
  jwt:
    admin-secret-key: itcast
```

Java 类字段应为：

```java
private String adminSecretKey;
```

---

📅 整理日期：2025-08-06
