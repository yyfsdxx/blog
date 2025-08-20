# Spring Boot 中基于 JWT 的登录认证流程笔记

---

## ✅ 什么是 JWT？

JWT（JSON Web Token）是一种无状态的用户认证方式。它将用户身份信息加密为 Token，前端携带该 Token 进行访问，后端解析校验即可，无需存储会话状态。

---

## 🧠 核心组成结构

JWT 由三部分组成：

```
header.payload.signature
```

- **Header**：声明使用的加密算法（如 HS256）
- **Payload**：用户信息、过期时间等
- **Signature**：Header + Payload 用密钥签名生成，防止被篡改

---

## 🚀 Spring Boot 中 JWT 登录完整流程

### 1. 用户登录

- 用户发送用户名 + 密码到 `/login` 接口
- 后端校验通过后，生成 JWT Token，返回给前端

```java
String token = JwtUtil.createToken(userId);
```

### 2. 前端存储 Token

- 一般保存在浏览器的 `localStorage` 或 `sessionStorage`
- 请求时在 Header 加上：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### 3. 后端拦截请求并校验 Token

- 配置拦截器（或 Spring Security 过滤器）
- 拿到请求头中的 Token，使用 `JwtUtil` 解析校验
- 校验成功 → 放行；失败 → 返回 401 未授权

---

### 4. 使用拦截器统一解析 Token（示例）

```java
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token != null && JwtUtil.verify(token)) {
            return true;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }
}
```

---

## 🔐 JWT 的优点

- 无状态，后端无需存储 Session
- 可扩展性强，可存放多种信息
- 跨平台支持好（适合移动端、Web）

---

## ⚠️ 注意事项

| 项目 | 建议 |
|------|------|
| Token 有效期 | 加入 `exp` 字段控制过期 |
| Token 保密性 | 不应包含敏感信息（如密码） |
| 防盗用 | 配合短期 Access Token + Refresh Token 机制 |
| 安全传输 | 推荐使用 HTTPS + HttpOnly Cookie 存储 |

---

## 🧾 示例：Token 结构

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
.
{
  "userId": 123,
  "role": "admin",
  "exp": 1720000000
}
.
<signature>
```

---

## ✅ 总结

- JWT 是一种基于签名的无状态身份认证方式
- Spring Boot 中通过登录生成 Token，前端携带，拦截器校验
- 防篡改靠签名机制，防盗用需结合有效期、设备绑定或 RefreshToken

