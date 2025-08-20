# MyBatis 分页插件：从零到可跑（takeout 项目版）

> 直接照着**文件路径**与**代码**创建即可运行。已按你给出的实现汇总成“一键落地”稿，含测试路径。

---

## 📁 推荐项目结构

```
src
├─ main
│  ├─ java
│  │  └─ com/example
│  │     ├─ config
│  │     │  └─ MybatisConfig.java
│  │     ├─ domain
│  │     │  └─ User.java
│  │     ├─ mapper
│  │     │  └─ UserMapper.java
│  │     ├─ paging
│  │     │  ├─ PageResult.java
│  │     │  ├─ PaginationContext.java
│  │     │  └─ PaginationInterceptor.java
│  │     ├─ service
│  │     │  ├─ PageService.java
│  │     │  └─ UserService.java
│  │     ├─ web
│  │     │  └─ UserController.java
│  │     └─ DemoApplication.java
│  └─ resources
│     ├─ application.yml
│     └─ mapper
│        └─ UserMapper.xml
└─ pom.xml
```

---

## 步骤 0：依赖 & 基础配置

### `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>demo</artifactId>
  <version>1.0.0</version>
  <properties>
    <java.version>17</java.version>
    <spring-boot.version>3.3.2</spring-boot.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Spring Boot Web -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis + Spring Boot -->
    <dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>3.0.3</version>
    </dependency>

    <!-- 数据库驱动（示例：MySQL） -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>8.4.0</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

### `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo
    username: root
    password: your_password

mybatis:
  mapper-locations: classpath*:mapper/*.xml
  type-aliases-package: com.example.domain
  configuration:
    map-underscore-to-camel-case: true

logging:
  level:
    org.apache.ibatis: debug      # 方便观察最终SQL
```

> 若你更偏好 `mybatis-config.xml` 注册插件，配置见“步骤 3”。

---

## 步骤 1：分页上下文（ThreadLocal）

### `src/main/java/com/example/paging/PaginationContext.java`

```java
package com.example.paging;

public final class PaginationContext {
    private PaginationContext() {}
    private static final ThreadLocal<PageParam> PAGE_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Long> TOTAL_HOLDER = new ThreadLocal<>();

    public static class PageParam {
        private final int pageNum;
        private final int pageSize;
        public PageParam(Integer pageNum, Integer pageSize) {
            int pn = (pageNum == null || pageNum < 1) ? 1 : pageNum;
            int ps = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 1000);
            this.pageNum = pn; this.pageSize = ps;
        }
        public int getPageNum() { return pageNum; }
        public int getPageSize() { return pageSize; }
        public int offset() { return (pageNum - 1) * pageSize; }
    }

    public static void set(PageParam param) { PAGE_HOLDER.set(param); }
    public static PageParam get() { return PAGE_HOLDER.get(); }
    public static void clearPage() { PAGE_HOLDER.remove(); }

    public static void setTotal(long total) { TOTAL_HOLDER.set(total); }
    public static Long getTotal() { return TOTAL_HOLDER.get(); }
    public static void clearTotal() { TOTAL_HOLDER.remove(); }

    public static void clearAll() { clearPage(); clearTotal(); }
}
```

### `src/main/java/com/example/paging/PageResult.java`

```java
package com.example.paging;

import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {
    private final long total;
    private final List<T> records;
    public PageResult(long total, List<T> records) {
        this.total = total; this.records = records;
    }
    public long getTotal() { return total; }
    public List<T> getRecords() { return records; }
}
```

---

## 步骤 2：分页拦截器

### `src/main/java/com/example/paging/PaginationInterceptor.java`

```java
package com.example.paging;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.*;
import java.util.Locale;
import java.util.Properties;

@Intercepts({
  @Signature(type = Executor.class, method = "query",
      args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
  @Signature(type = Executor.class, method = "query",
      args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class PaginationInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation inv) throws Throwable {
        PaginationContext.PageParam page = PaginationContext.get();
        if (page == null) return inv.proceed();

        Object[] args = inv.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];

        Executor executor = (Executor) inv.getTarget();
        BoundSql boundSql;
        CacheKey cacheKey;

        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameterObject);
            cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
        } else {
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        // 1) 统计总数
        String originalSql = boundSql.getSql().trim();
        String countSql = "SELECT COUNT(*) FROM (" + stripOrderBy(originalSql) + ") AS total_query";
        long total = queryCount(executor, ms, parameterObject, boundSql, countSql);
        PaginationContext.setTotal(total);

        // 2) 生成分页SQL（MySQL）
        String pageSql = originalSql + " LIMIT " + page.offset() + ", " + page.getPageSize();

        // 3) 构建新的 BoundSql/MappedStatement
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), pageSql,
                boundSql.getParameterMappings(), parameterObject);
        // 复制额外参数（foreach等）
        for (ParameterMapping pm : boundSql.getParameterMappings()) {
            String prop = pm.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        MappedStatement newMs = copyMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));

        // 4) 执行分页查询
        return executor.query(newMs, parameterObject, RowBounds.DEFAULT, resultHandler, cacheKey, newBoundSql);
    }

    @Override public Object plugin(Object target) { return Plugin.wrap(target, this); }
    @Override public void setProperties(Properties properties) {}

    private long queryCount(Executor executor, MappedStatement ms, Object param, BoundSql boundSql, String countSql) throws SQLException {
        Configuration cfg = ms.getConfiguration();
        try (Connection conn = executor.getTransaction().getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            ParameterHandler ph = cfg.newParameterHandler(ms, param, boundSql);
            ph.setParameters(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    // 简易去掉最外层 ORDER BY（不处理子查询里的）
    private String stripOrderBy(String sql) {
        String lower = sql.toLowerCase(Locale.ROOT);
        int idx = lower.lastIndexOf(" order by ");
        return (idx > -1) ? sql.substring(0, idx) : sql;
    }

    private MappedStatement copyMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder b = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        b.resource(ms.getResource());
        b.fetchSize(ms.getFetchSize());
        b.statementType(ms.getStatementType());
        b.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0)
            b.keyProperty(String.join(",", ms.getKeyProperties()));
        b.timeout(ms.getTimeout());
        b.parameterMap(ms.getParameterMap());
        b.resultMaps(ms.getResultMaps());
        b.resultSetType(ms.getResultSetType());
        b.cache(ms.getCache());
        b.flushCacheRequired(ms.isFlushCacheRequired());
        b.useCache(ms.isUseCache());
        return b.build();
    }

    private static class BoundSqlSqlSource implements SqlSource {
        private final BoundSql boundSql;
        BoundSqlSqlSource(BoundSql boundSql) { this.boundSql = boundSql; }
        @Override public BoundSql getBoundSql(Object parameterObject) { return boundSql; }
    }
}
```

---

## 步骤 3：注册插件到 MyBatis

**方式 A（推荐，Spring Boot Java 配置）**

### `src/main/java/com/example/config/MybatisConfig.java`

```java
package com.example.config;

import com.example.paging.PaginationInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {
    @Bean
    public Interceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
```

**方式 B（mybatis-config.xml）**

### `src/main/resources/mybatis-config.xml`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <plugins>
    <plugin interceptor="com.example.paging.PaginationInterceptor"/>
  </plugins>
</configuration>
```

并在 `application.yml` 指定：

```yaml
mybatis:
  config-location: classpath:mybatis-config.xml
```

> 两种方式二选一即可。

---

## 步骤 4：示例实体 & Mapper 与 XML

### `src/main/java/com/example/domain/User.java`

```java
package com.example.domain;

public class User {
    private Long id;
    private String name;
    private Integer age;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
```

### `src/main/java/com/example/mapper/UserMapper.java`

```java
package com.example.mapper;
import com.example.domain.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper {
    List<User> selectByCond(@Param("kw") String kw);
}
```

### `src/main/resources/mapper/UserMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.UserMapper">
  <resultMap id="UserMap" type="com.example.domain.User">
    <id property="id" column="id"/>
    <result property="name" column="name"/>
    <result property="age" column="age"/>
  </resultMap>

  <select id="selectByCond" resultMap="UserMap">
    SELECT id, name, age
    FROM t_user
    <where>
      <if test="kw != null and kw != ''">
        AND name LIKE CONCAT('%', #{kw}, '%')
      </if>
    </where>
    ORDER BY id DESC
  </select>
</mapper>
```

---

## 步骤 5：Service 层统一组装 `PageResult`

### `src/main/java/com/example/service/PageService.java`

```java
package com.example.service;

import com.example.paging.PageResult;
import com.example.paging.PaginationContext;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class PageService {
    public <T> PageResult<T> page(Supplier<List<T>> supplier) {
        try {
            List<T> list = supplier.get();
            long total = Optional.ofNullable(PaginationContext.getTotal()).orElse(0L);
            return new PageResult<>(total, list);
        } finally {
            PaginationContext.clearAll();
        }
    }
}
```

### 业务 Service 示例（供 Controller 调用）

#### `src/main/java/com/example/service/UserService.java`

```java
package com.example.service;

import com.example.domain.User;
import com.example.paging.PageResult;

public interface UserService {
    PageResult<User> searchUsers(String kw, Integer pageNum, Integer pageSize);
}
```

#### `src/main/java/com/example/service/impl/UserServiceImpl.java`

```java
package com.example.service.impl;

import com.example.domain.User;
import com.example.mapper.UserMapper;
import com.example.paging.PageResult;
import com.example.paging.PaginationContext;
import com.example.service.PageService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Resource private UserMapper userMapper;
    @Resource private PageService pageService;

    @Override
    public PageResult<User> searchUsers(String kw, Integer pageNum, Integer pageSize) {
        PaginationContext.set(new PaginationContext.PageParam(pageNum, pageSize));
        return pageService.page(() -> userMapper.selectByCond(kw));
    }
}
```

---

## 步骤 6：Controller（测试用）

### `src/main/java/com/example/web/UserController.java`

```java
package com.example.web;

import com.example.domain.User;
import com.example.paging.PageResult;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Resource private UserService userService;

    @GetMapping
    public PageResult<User> list(@RequestParam(required=false) String kw,
                                 @RequestParam Integer pageNum,
                                 @RequestParam Integer pageSize) {
        return userService.searchUsers(kw, pageNum, pageSize);
    }
}
```

---

## 启动类（若尚未创建）

### `src/main/java/com/example/DemoApplication.java`

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

## （可选）初始化 SQL

```sql
CREATE TABLE IF NOT EXISTS t_user (
  id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  age  INT
);

INSERT INTO t_user(name, age) VALUES
 ('Alice',23),('Bob',31),('Cindy',19),('David',27),('Eve',22),
 ('Frank',28),('Grace',26),('Heidi',35),('Ivan',29),('Judy',24);
```

---

## 运行与测试

**启动**

```bash
mvn spring-boot:run
```

**测试 URL**

```
GET http://localhost:8080/users?pageNum=1&pageSize=5&kw=a
```

**示例返回**

```json
{
  "total": 23,
  "records": [
    {"id":3,"name":"Alice","age":23}
  ]
}
```

---

## 常见坑 & 排查

- **忘记注册插件** → 没有分页，只返回全部；日志看不到 `LIMIT`。
- **多次查询** → 每次分页前都 `PaginationContext.set(...)`，用完交由 `PageService.page()` 清理。
- **动态 SQL（foreach）** → 必须复制 `additionalParameters`（拦截器里已处理）。
- **count 慢** → 去掉最外层 `ORDER BY`（已做）；复杂大表可自定义更高效的 count。
- **数据库方言**：PostgreSQL 用 `LIMIT size OFFSET offset`；如需多方言，抽一个 `Dialect` 接口在生成分页 SQL 时分支即可。

---

> 以上内容已完整同步你给出的实现，并补齐缺失的实体、启动类与 `mybatis-config.xml` 可选方案。把文件照路径放入项目、改好数据库连接即可直跑。



---

## 附录：MyBatis 四大核心组件速览

> 本节补充解释 **Executor、StatementHandler、ParameterHandler、ResultSetHandler** 在执行链中的职责与关系，便于理解分页拦截器的切入点。

### 1) Executor（执行器）
- **位置**：最外层协调者，负责整体 SQL 调用流程。
- **职责**：
  - 统一入口：`query()` / `update()`。
  - **缓存**管理：一级/二级缓存的命中与回填。
  - 调度 `StatementHandler` 完成具体 SQL 执行。
- **类比**：项目经理（先看缓存、后派单给“施工工人”）。

### 2) StatementHandler（语句处理器）
- **位置**：JDBC 层的封装者。
- **职责**：
  - 基于 `MappedStatement` 和 `BoundSql` **创建 JDBC Statement**（`PreparedStatement`/`CallableStatement`）。
  - 调用 `ParameterHandler` **填充参数**。
  - **执行 SQL** 并得到 `ResultSet`。
- **类比**：施工工人（真正去数据库跑 SQL）。

### 3) ParameterHandler（参数处理器）
- **位置**：由 `StatementHandler` 使用。
- **职责**：
  - 将 Mapper 入参按 `BoundSql` 的参数映射，**绑定到 `?` 占位符**。
  - 代替手写 `ps.setXxx(i, value)`，复用 MyBatis 的类型处理与映射规则。
- **类比**：搬运/填坑工（把 Java 参数放进 SQL 的坑位里）。

### 4) ResultSetHandler（结果集处理器）
- **位置**：SQL 执行完成后，处理 `ResultSet`。
- **职责**：
  - **映射**：将 `ResultSet` 转为实体类/Map/List（支持自动映射与自定义 `resultMap`）。
- **类比**：翻译/质检员（把原始表格数据翻译成 Java 对象）。

### 🔄 执行链路（简图）
1. **Executor**：查缓存 → 未命中 → 调 `StatementHandler`。
2. **StatementHandler**：创建 `PreparedStatement` → 调 `ParameterHandler` 赋值 → 执行 SQL。
3. **ResultSetHandler**：将 `ResultSet` 映射为目标对象。
4. **Executor**：写回缓存（如开启）→ 返回结果。

### 📝 速查表

| 组件 | 核心作用 | 关键点 | 类比 |
|---|---|---|---|
| **Executor** | 流程调度与缓存 | `query`/`update` 入口、一级/二级缓存 | 项目经理 |
| **StatementHandler** | 封装并执行 JDBC Statement | 创建/执行 `PreparedStatement` | 施工工人 |
| **ParameterHandler** | 绑定 SQL 参数 | 将入参绑定到 `?` | 搬运/填坑工 |
| **ResultSetHandler** | 结果映射 | `ResultSet` → Java 对象 | 翻译/质检员 |

> 在本分页插件中：拦截点在 `Executor#query(...)` 上；统计总数时通过 `executor.getTransaction().getConnection()` 拿连接，借助 `ParameterHandler` 绑定参数，避免手写 `ps.setXxx(...)`。

---
在 MyBatis 中，`MappedStatement` 和 `BoundSql` 是执行 SQL 的两个核心对象，它们分别承担不同的职责：

---

## 1. MappedStatement

MyBatis 会把 **Mapper XML 或注解 SQL 配置** 解析为 `MappedStatement` 对象。它是 **SQL 执行的元信息描述**。

**主要内容：**
- `id`：SQL 的唯一标识（namespace + 方法名）
- `sqlCommandType`：命令类型（SELECT / INSERT / UPDATE / DELETE）
- `SqlSource`：SQL 源对象（原始 SQL + 动态 SQL 解析器）
- `parameterMap`：参数映射（Mapper 方法参数 → SQL 占位符）
- `resultMaps`：结果映射（SQL 查询结果 → Java 对象字段）
- `statementType`：语句类型（STATEMENT / PREPARED / CALLABLE）
- `keyGenerator` / `keyProperties`：主键生成策略
- 缓存相关：`cache`、`flushCacheRequired`、`useCache`
- 其他：超时时间、fetchSize、resource 来源路径等

👉 在分页插件中，我们拷贝原始的 `MappedStatement`，只替换其中的 `SqlSource`，让它绑定新的分页 SQL。注意这里面不是直接通过`MappedStatement`创建`boundsql`，虽然`MappedStatement`中确实可以调用getBoudsql（）获得boundsql，但是实际这个方法中是调用了`sqlsource.getBoundSql()`注意这里`sqlSource`是一个接口，如果你重新复制一个`MappedStetement`，记得自己实现。

---

## 2. BoundSql

`BoundSql` 是从 `MappedStatement` 中解析出来的，代表**最终可执行的 SQL 语句 + 参数信息**。

**主要内容：**
- `sql`：最终拼接后的 SQL 字符串（含 `?` 占位符）
- `parameterMappings`：参数映射列表，对应 SQL 中的每个 `?`
- `parameterObject`：传入的参数对象（Mapper 方法参数）
- `additionalParameters`：额外参数（动态 SQL 用，比如 foreach 循环生成的参数）

👉 在分页插件中：
1. 通过 `ms.getBoundSql(parameterObject)` 获取原始 SQL。
2. 构造分页 SQL，生成新的 `BoundSql`。
3. 复制 `additionalParameters`，避免 foreach 等动态 SQL 报错。

---

## 3. 关系与区别
- `MappedStatement`：宏观定义，描述 SQL 的完整配置信息（类似**食谱**）。
- `BoundSql`：运行时产物，结合参数生成的最终 SQL（类似**实际这次做菜的食材与步骤**）。

---
 MyBatis 四大核心组件与相关对象关系图（两层结合版）

MyBatis 的执行流程分为两个层次：
- **核心组件层**（四大组件：Executor、StatementHandler、ParameterHandler、ResultSetHandler）
- **支撑对象层**（运行时对象：MappedStatement、BoundSql、PreparedStatement、ResultSet）

---

## 🔑 四大核心组件

### 1. Executor —— 调度总管
- 接收 Mapper 调用，管理缓存。
- 调用 StatementHandler 执行 SQL。
- 常见实现：SimpleExecutor、ReuseExecutor、BatchExecutor（外层常包 CachingExecutor）。

### 2. StatementHandler —— SQL 执行管家
- 创建并管理 JDBC Statement 的生命周期。
- 调用 ParameterHandler 绑定参数。
- 执行 SQL，调用 ResultSetHandler 处理结果。
- 常见实现：PreparedStatementHandler、SimpleStatementHandler、CallableStatementHandler。

### 3. ParameterHandler —— 参数绑定器
- 从 BoundSql.parameterObject 中取出参数。
- 根据 parameterMappings 调用 TypeHandler。
- 把参数值绑定到 PreparedStatement 的 `?` 占位符。
- 默认实现：DefaultParameterHandler。

### 4. ResultSetHandler —— 结果映射器
- 遍历 JDBC ResultSet。
- 按照 ResultMap 映射规则把结果封装为 Java 对象。
- 默认实现：DefaultResultSetHandler。

---

## 🧩 支撑对象层
- **MappedStatement**：SQL 蓝图，配置级别的描述（存在于 Configuration 中）。
- **BoundSql**：运行时生成的最终 SQL + 参数信息。
- **PreparedStatement**：JDBC 执行对象，真正下锅执行 SQL。
- **ResultSet**：数据库返回的原始结果集。

---

## 🪜 调用关系（两层结合）

```
Mapper 方法调用
   ↓
Executor (调度)
   ↓ 依赖配置
MappedStatement (SQL 蓝图)
   ↓ 结合参数生成
BoundSql (最终 SQL + 参数信息)
   ↓
StatementHandler
   1) prepare() 创建 PreparedStatement
   2) parameterize() → 调用 ParameterHandler 设置参数
   3) query()/update() 执行 SQL
   ↓
ParameterHandler (参数绑定)
   ↓
PreparedStatement (JDBC 对象)
   ↓ 执行 SQL
ResultSetHandler (结果映射)
   ↓
ResultSet (JDBC 原始结果集 → Java 对象)
```

---

## 📌 类比总结
- **Executor**：项目经理（调度全局）。
- **MappedStatement**：设计图纸。
- **BoundSql**：实际施工方案（这次的 SQL 和参数）。
- **StatementHandler**：施工队长（管理 SQL 执行）。
- **ParameterHandler**：材料工（把参数填到 ? 占位符）。
- **PreparedStatement**：施工工具（锅）。
- **ResultSetHandler**：验收员（把结果转成需要的模型）。
- **ResultSet**：原始成品，需再加工。

---


# MyBatis 6 参数 query 专题笔记

---

## 📎 为什么 6 参 `Executor.query(ms, param, rowBounds, rh, cacheKey, boundSql)` 需要同时传 `MappedStatement` 和 `BoundSql`

> 结论：**6 参是“高级直通口”**。`BoundSql` 负责“这次要执行什么 SQL”，`MappedStatement` 提供“如何执行和如何映射”的元信息；两者缺一不可，且必须保持一致。

### 1) 两者分工不同但互补
- **BoundSql（运行时数据）**：最终 SQL 字符串、`parameterMappings`、`parameterObject`、`additionalParameters`。
- **MappedStatement（静态元信息）**：`resultMaps`、`statementType`、`fetchSize`、`timeout`、`keyGenerator`、`parameterMap`、`cache/useCache` 等。

### 2) 下游组件依赖 `MappedStatement`
- `Configuration.newStatementHandler(...)` 需要 `MappedStatement` 决定用 **Prepared/Simple/Callable** 哪种执行方式。
- `ResultSetHandler` 依赖 `ms.getResultMaps()` 把 `ResultSet` 映射成目标 Java 对象。

### 3) 缓存体系需要 `MappedStatement`
- 二级缓存（`CachingExecutor`）要看 `ms.isUseCache()`、`ms.getId()` 等配置。
- `CacheKey` 的组成通常含 `ms.getId()` + SQL + 参数；**改了 SQL（BoundSql）时，`CacheKey` 也要基于新 `BoundSql` 重算**。

### 4) 不一致会导致的问题（风险清单）
- **SQL 不一致**：`BoundSql.sql` 与 `ms.SqlSource` 的产出不同 → 可能缓存命中错乱。
- **参数映射不一致**：`BoundSql.parameterMappings` 与 `ms.parameterMap` 不匹配 → 绑定报错（如 `There is no getter for property`）。
- **结果映射不一致**：查询列与 `ms.resultMaps` 不匹配 → 字段为 `null` 或类型转换异常。

### 5) 插件/改写 SQL 的最佳实践
1. 基于原 `ms` **构造新 `BoundSql`**（如追加 `LIMIT`）。
2. 用新 `BoundSql` 包装成 **新的 `SqlSource`**，**copy 出新的 `MappedStatement`**（继承原元信息，仅替换 `SqlSource`）。
3. 基于 **新 `MappedStatement + 新 BoundSql`** 调用 **6 参 query**，并 **重算 `CacheKey`**：
   ```java
   CacheKey cacheKey = executor.createCacheKey(newMs, parameterObject, RowBounds.DEFAULT, newBoundSql);
   return executor.query(newMs, parameterObject, RowBounds.DEFAULT, resultHandler, cacheKey, newBoundSql);
   ```

### 6) 6 参与 4 参的定位
- **4 参**：常规入口，Executor 自行 `ms.getBoundSql(param)`，天生与 `MappedStatement` 一致。
- **6 参**：高级入口，**信任调用方**已经让 `MappedStatement / BoundSql / CacheKey` 三者一致（用于缓存层、分页/审计等插件）。

### 7) 迷你流程图（6 参直通）
```
newMs(替换SqlSource) + newBoundSql + newCacheKey
   ↓  6参直通
Executor.query(ms, p, rb, rh, cacheKey, boundSql)
   ↓
StatementHandler.prepare/parameterize/query
   ↓
ResultSetHandler.handleResultSets → 映射为对象
```

---

## ✅ 使用注意事项清单（6 参数 query）
- 改 SQL 时务必：
  1. 用新的 SqlSource + BoundSql 构造新的 MappedStatement
  2. 同时传递新 BoundSql
  3. 重算 CacheKey
- 确保三者一致：**MappedStatement 元信息 + BoundSql 执行数据 + CacheKey 缓存键**。
- 避免出现参数映射、结果映射和缓存错乱。
- 为了便于参数一致性，我们这里的例子选择使用4参数query使用`mappedstatement`自动生成`boundsql`与`cachekey`。下面是4参数与6参数分别的运用场景与注意事项。
  
  # Executor.query 四参 vs 六参的区别与适用场景

---

## 1. 两种方法签名

### 四参数版
```java
<E> List<E> query(
    MappedStatement ms,
    Object parameter,
    RowBounds rowBounds,
    ResultHandler resultHandler)
```
- 传入 **MappedStatement**、参数对象、分页信息、结果处理器。
- 内部会 **自动生成 BoundSql 和 CacheKey**。

### 六参数版
```java
<E> List<E> query(
    MappedStatement ms,
    Object parameter,
    RowBounds rowBounds,
    ResultHandler resultHandler,
    CacheKey cacheKey,
    BoundSql boundSql)
```
- 需要调用方显式传入 **CacheKey** 和 **BoundSql**。
- MyBatis 完全信任调用方保证三者一致：`MappedStatement + BoundSql + CacheKey`。

---

## 2. 内部逻辑对比

### 四参调用流程
1. 自动生成 BoundSql：
   ```java
   BoundSql boundSql = ms.getBoundSql(parameter);
   ```
2. 自动生成 CacheKey：
   ```java
   CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
   ```
3. 执行 query。

👉 **适合大多数场景**，自动保证一致性。

### 六参调用流程
- 直接使用调用方传入的 `BoundSql` 与 `CacheKey`。  
- 不再调用 `ms.getBoundSql()` 或 `createCacheKey()`。

👉 **适合高级场景**（插件、二级缓存、自定义 SQL 改写）。

---

## 3. 适用场景

### 四参数版（常规入口）
- 常见的 Mapper 方法调用。
- 分页插件若只替换了 `MappedStatement` 的 `SqlSource`，也可以使用四参，MyBatis 会帮你生成新的 BoundSql + CacheKey。
- **优点**：简单、安全。

### 六参数版（高级入口）
- 分页插件、审计插件：在外部预先生成新 SQL 和新 CacheKey，再调用 Executor。
- 二级缓存：需要精确控制 CacheKey，以保证缓存命中逻辑一致。
- **优点**：灵活、可控。
- **缺点**：需要调用方保证一致性，否则可能导致缓存错乱或映射失败。

---

## 4. 对比总结表

| 方法 | BoundSql 生成 | CacheKey 生成 | 使用难度 | 典型场景 |
|------|---------------|---------------|----------|----------|
| **四参数 query** | MyBatis 自动 `ms.getBoundSql(parameter)` | MyBatis 自动 `createCacheKey(...)` | 简单，安全 | 普通查询、Mapper 默认调用 |
| **六参数 query** | 调用方自己传入 | 调用方自己传入 | 复杂，需要谨慎 | 插件改写 SQL、分页、缓存控制 |

---

## ✅ 总结
- **四参数版**：常规、安全入口，MyBatis 自动生成 BoundSql 和 CacheKey。适合 95% 的场景。  
- **六参数版**：高级入口，给插件/缓存控制使用，完全依赖调用方保证 **MappedStatement + BoundSql + CacheKey 一致性**。



---


# 插曲 Builder模式创建类
## 1. Builder 模式的优势（以 MyBatis MappedStatement 为例）

### 为什么不用 setter？
- **不可变性**：避免运行时随意修改，保证线程安全。
- **属性多**：MappedStatement 有二十多个字段，用构造函数或 setter 都不够优雅。
- **完整性**：Builder 的 `build()` 方法能做默认值填充和校验，避免出现“不完整对象”。
- **插件友好**：分页插件等需要复制 MappedStatement，如果能直接 set 就可能污染原对象，用 Builder 可以“复制 + 替换部分属性 + build 新对象”。

### 总结：
`MappedStatement` 采用 Builder 模式是为了 **线程安全 + 对象不可变 + 构建灵活性**。

---

## 2. 链式调用的实现方式

### 普通 setter
```java
public class Person {
    private String name;
    private int age;

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
}

Person p = new Person();
p.setName("Tom");
p.setAge(18);
```
👉 每次调用单独一行。

### 链式调用 setter
```java
public class Person {
    private String name;
    private int age;

    public Person setName(String name) {
        this.name = name;
        return this;   // 返回当前对象
    }
    public Person setAge(int age) {
        this.age = age;
        return this;   // 返回当前对象
    }
}

Person p = new Person()
              .setName("Tom")
              .setAge(18);
```
👉 通过 `return this` 实现方法链式调用。

### MyBatis 的 Builder 链式调用
```java
MappedStatement ms = new MappedStatement.Builder(cfg, id, sqlSource, sqlCommandType)
        .resource("xxx.xml")
        .fetchSize(100)
        .timeout(30)
        .build();
```
👉 每个方法返回 `Builder` 本身，最后 `build()` 返回完整对象。

---
## ✅ 总结
- **Builder 模式**：解决对象复杂构建问题，保证不可变性和完整性。
- **链式调用**：setter 返回 `this`，让对象初始化更简洁、直观。

---
# 小方法
