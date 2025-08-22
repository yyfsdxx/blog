# ThreadLocal 原理与“不串值”原因（独立笔记）

> 用一句话概括：**ThreadLocal 提供“每个线程一份”的小仓库**。`ThreadLocal` 变量本身是全局唯一的“钥匙（key）”，但**值存放在各线程自己的 `ThreadLocalMap`** 里，因此并发请求不会互相串值。

---

## 1. 一图看懂存储模型
```
             static final ThreadLocal<PageParam> PAGE_HOLDER (key)
                            │
            ┌───────────────┴────────────────┐
            │                                │
         Thread A                          Thread B
     ┌────────────────┐                ┌────────────────┐
     │ ThreadLocalMap │                │ ThreadLocalMap │
     │  (A 私有)      │                │  (B 私有)      │
     │  key→ value    │                │  key→ value    │
     │ PAGE_HOLDER →X │                │ PAGE_HOLDER →Y │
     └────────────────┘                └────────────────┘
```
- **同一个 `ThreadLocal` 实例**（`PAGE_HOLDER`）作为 key，在**不同线程的 Map** 里各自有一份 value。
- 线程 A 的 `X` 与线程 B 的 `Y` 互不可见。

---

## 2. 核心概念
- **ThreadLocal 对象**：只是一个“键（key）”，通常定义为 `static final`，便于全局访问。
- **ThreadLocalMap**：挂在 `Thread` 实例上的私有 Map（每个线程一份）。真正的值存储在这里。
- **键值关系**：`currentThread.threadLocalMap.put(threadLocalKey, value)`。

---

## 3. 工作流程
1. **set**：`tl.set(v)` → 把 `(tl → v)` 放进 **当前线程** 的 `ThreadLocalMap`。
2. **get**：`tl.get()` → 从 **当前线程** 的 `ThreadLocalMap` 取值。
3. **remove**：`tl.remove()` → 从 **当前线程** 的 `ThreadLocalMap` 移除。线程池复用时尤其重要。

---

## 4. 为什么不会串
- Web（Servlet/MVC）是“**每个请求一个工作线程**”的模型；不同客户端请求 → 不同线程。
- ThreadLocal 的值**存放在线程私有的 ThreadLocalMap** 中，**线程之间不共享**。
- 即便 `ThreadLocal` 变量是 `static final`（全局唯一 key），各线程仍拿的是**自己 Map** 里的 value。

> 结论：**并发请求互不影响**。只要**请求结束后及时 `remove()` 清理**，就不会出现串值问题。

---

## 5. 与“全局变量”对比
| 对象类型 | 存储位置 | 可见性 | 并发风险 |
|---|---|---|---|
| 静态字段/单例字段（“全局变量”） | 堆（所有线程共享） | 共享 | 有（需锁/原子类/并发容器） |
| ThreadLocal 值 | 各线程的 `ThreadLocalMap`（线程私有） | 私有 | 无（限同线程），但需及时清理 |

---

## 6. 典型适用场景
- **请求级上下文**：分页参数、TraceId、登录用户信息、数据源路由标签等。
- **与拦截器/切面配合**：进入前 `set`，业务中 `get`，完成后 `remove`。

---

## 7. 最佳实践
- **定义**：`private static final ThreadLocal<T> TL = new ThreadLocal<>();`
- **使用模板**（务必清理）：
```java
TL.set(value);
try {
    // ... 当前线程内使用 TL.get()
} finally {
    TL.remove(); // 必须：线程池复用下防“遗留”
}
```
- **放置位置**：做成**纯工具类 + 静态方法**，不要注入成 Spring Bean。
- **边界**：不要在 **异步/并行/子线程** 中依赖父线程的 ThreadLocal 值；需要就**显式传参**。
- **WebFlux/响应式**：不保证同一请求固定线程，**不适合用 ThreadLocal**，改用上下文传递。

---

## 8. 常见坑与规避
1. **忘记 `remove()`**：线程池复用导致“脏数据”污染下一请求 → **始终在 `finally` 清理**。
2. **跨线程访问**：`@Async`、`CompletableFuture`、`parallelStream()` 等不会带上父线程的值 → **显式传参**。
3. **误当全局状态**：ThreadLocal 只在**当前线程**有效，切莫用于进程级共享。
4. **内存泄漏**：长生命周期线程 + 未清理，或异常路径未走到清理逻辑 → 用 **try/finally** 保证清理。

---

## 9. 与分页插件的结合（示例）
```java
// Service 层内：设置分页参数并清理
PaginationContext.setPageHolder(new PageParam(pageNum, pageSize));
try {
    List<User> rows = userMapper.selectByCond(kw);
    long total = Optional.ofNullable(PaginationContext.getTotalHolder()).orElse(0L);
    return new PageResult<>(total, rows);
} finally {
    PaginationContext.clearAll();
}
```
- `PaginationContext` 使用 `ThreadLocal` 保存分页参数；
- 分页拦截器在**同一线程**里读取参数并回填 `total`；
- `finally` 中统一清理，防止线程复用造成污染。

---

## 10. 核心记忆点（TL;DR）
- **ThreadLocal 是 key，不是仓库；值在各线程自己的 ThreadLocalMap**。
- **static final 只保证 key 单例**，不代表值共享。
- **同线程可见、跨线程不可见**。
- **try/finally 永远清理**；不要在异步/并行里依赖它。

