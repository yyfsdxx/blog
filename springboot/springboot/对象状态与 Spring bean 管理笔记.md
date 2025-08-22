# 对象状态与 Spring 管理笔记

---

## 1. 无状态对象（Stateless Object）
**定义**：对象中没有会随调用变化的实例变量，方法结果完全由入参决定。

### 特点
- 不保存任何与请求/会话相关的数据。
- 多线程调用安全，不需要同步。
- 可复用，适合作为 **Spring 单例 Bean**。

### 示例
```java
// 工具类（无状态）
public class MathUtil {
    public static int add(int a, int b) {
        return a + b;
    }
}

@Service
public class UserService {
    @Autowired private UserMapper userMapper;
    public User findById(Long id) {
        return userMapper.selectById(id);
    }
}
```

---

## 2. 有状态对象（Stateful Object）
**定义**：对象内部保存了会随调用变化的数据，每个实例代表不同状态。

### 特点
- 内部有可变实例变量。
- 每个实例的数据不同。
- 如果作为单例 Bean 可能导致线程安全问题。
- 更适合 **方法内 new 出来** 或使用 **原型作用域**。

### 示例
```java
// 有状态：保存计数
public class Counter {
    private int count = 0;
    public void increase() { count++; }
}

// DTO/VO（数据载体，天然有状态）
public class PageResult<T> {
    private final long total;
    private final List<T> records;
    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
```

> 例如 `PageResult<User>` 保存了本次分页查询的结果，是有状态的对象。它通常直接 `new` 出来，不交给 Spring 管理。

---

## 3. Spring Bean 管理实践

### 适合交给 Spring 管理的对象
- Service、Repository、Mapper 等业务组件（无状态）。
- 第三方客户端、工具服务（配置为单例 Bean）。

### 不适合交给 Spring 管理的对象
- DTO/VO/Entity/Result 包装类（有状态，携带一次性数据）。
- 简单工具类（若全是静态方法，可直接调用）。

---

## 4. 有参构造函数的自动装配
- Spring 支持 **构造器注入**：
  ```java
  @Service
  public class OrderService {
      private final PaymentClient client;
      private final OrderRepo repo;

      @Autowired // 单构造器时可省略
      public OrderService(PaymentClient client, OrderRepo repo) {
          this.client = client;
          this.repo = repo;
      }
  }
  ```
- 配合 Lombok：
  ```java
  @Service
  @RequiredArgsConstructor
  public class OrderService {
      private final PaymentClient client;
      private final OrderRepo repo;
  }
  ```

---

## 5. 小结
- **无状态对象**：交给 Spring 管理，复用、安全。
- **有状态对象**：直接 `new`，避免线程问题。
- **PageResult / DTO / VO / Entity** → 有状态，不交给 Spring。
- **Service / Mapper / Config / Client** → 无状态，交给 Spring。

