# `@ConfigurationProperties(prefix="sky.jwt")` 使用笔记

`@ConfigurationProperties` 是 Spring Boot 中用于将配置文件中的属性自动绑定到 Java Bean 的注解，适合管理配置项特别多或需要复用的情况。

---

## 🧩 用法示例

**YAML 文件配置**：

```yaml
sky:
  jwt:
    admin-secret-key: itcast
    admin-ttl: 7200000
    admin-token-name: token
```

**Java Bean 类绑定**：

```java
package org.yyf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sky.jwt")
public class JwtProperties {

    private String adminSecretKey;
    private Long adminTtl;
    private String adminTokenName;
}
```

---

## 🧠 注解说明

| 注解/元素                        | 含义 |
|----------------------------------|------|
| `@Component`                     | 注册为 Spring Bean |
| `@ConfigurationProperties`       | 配置绑定，指定前缀 |
| `@Data`                          | 来自 Lombok，自动生成 getter/setter |
| **字段必须使用驼峰命名法**        | **比如配置项 `admin-secret-key`，字段名必须是 `adminSecretKey`** |

---

## 🔁 属性映射规则

| 配置文件中的 key      | Java 类中的字段名 |
|------------------------|-------------------|
| `admin-secret-key`     | `adminSecretKey`  |
| `admin-ttl`            | `adminTtl`        |
| `admin-token-name`     | `adminTokenName`  |

Spring Boot 自动完成类型转换，无需手动处理。

---

## 🧪 支持嵌套结构

```yaml
sky:
  jwt:
    admin:
      secret-key: abc
      ttl: 7200
```

```java
@Data
public class AdminConfig {
    private String secretKey;
    private Integer ttl;
}

@Data
@Component
@ConfigurationProperties(prefix = "sky.jwt")
public class JwtProperties {
    private AdminConfig admin;
}
```

---

## ✅ 优势总结

- 配置分组更清晰，适合大型系统
- 支持类型安全转换
- 支持嵌套对象结构
- 可结合 `@Validated` 实现配置项校验
- 强 IDE 支持，提示更友好（需引入 `spring-boot-configuration-processor` 依赖）

---

## 📦 开启配置提示（如未自动补全）

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-configuration-processor</artifactId>
  <optional>true</optional>
</dependency>
```