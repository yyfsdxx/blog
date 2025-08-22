# 📝 MyBatis XML 常用标签速查表

## 1. 基本结构
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.mapper.YourMapper">

  <!-- 结果映射 -->
  <resultMap id="UserMap" type="com.example.domain.User">
    <id property="id" column="id"/>
    <result property="name" column="name"/>
    <result property="age" column="age"/>
  </resultMap>

</mapper>
```

---

## 2. 查询（`<select>`）
```xml
<!-- 简单查询 -->
<select id="findById" resultMap="UserMap">
  SELECT id, name, age FROM t_user WHERE id = #{id}
</select>

<!-- 模糊查询 -->
<select id="findByNameLike" resultMap="UserMap">
  SELECT id, name, age
  FROM t_user
  WHERE name LIKE CONCAT('%', #{kw}, '%')
</select>

<!-- 直接返回基本类型 -->
<select id="countUsers" resultType="long">
  SELECT COUNT(*) FROM t_user
</select>
```

---

## 3. 插入（`<insert>`）
```xml
<!-- 单条插入并返回主键 -->
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
  INSERT INTO t_user(name, age) VALUES(#{name}, #{age})
</insert>

<!-- 批量插入 -->
<insert id="batchInsert">
  INSERT INTO t_user(name, age)
  VALUES
  <foreach collection="list" item="u" separator=",">
    (#{u.name}, #{u.age})
  </foreach>
</insert>
```

---

## 4. 更新（`<update>`）
```xml
<!-- 全字段更新 -->
<update id="updateUser">
  UPDATE t_user SET name=#{name}, age=#{age} WHERE id=#{id}
</update>

<!-- 动态更新 -->
<update id="updateUserSelective">
  UPDATE t_user
  <set>
    <if test="name != null">name=#{name},</if>
    <if test="age != null">age=#{age},</if>
  </set>
  WHERE id=#{id}
</update>
```

---

## 5. 删除（`<delete>`）
```xml
<delete id="deleteById">
  DELETE FROM t_user WHERE id=#{id}
</delete>

<!-- 批量删除 -->
<delete id="deleteByIds">
  DELETE FROM t_user WHERE id IN
  <foreach collection="ids" item="id" open="(" separator="," close=")">
    #{id}
  </foreach>
</delete>
```

---

## 6. 动态 SQL 标签

### `<if>`：条件判断
```xml
<if test="name != null">AND name=#{name}</if>
```

### `<where>`：自动处理 AND/OR
```xml
<where>
  <if test="name != null">AND name=#{name}</if>
  <if test="age != null">AND age=#{age}</if>
</where>
```

### `<choose>`：类似 switch-case
```xml
<choose>
  <when test="name != null">WHERE name=#{name}</when>
  <when test="age != null">WHERE age=#{age}</when>
  <otherwise>WHERE 1=1</otherwise>
</choose>
```

### `<set>`：更新时自动去掉多余的逗号
```xml
<set>
  <if test="name != null">name=#{name},</if>
  <if test="age != null">age=#{age},</if>
</set>
```

### `<foreach>`：循环拼接（常用于批量）
```xml
<foreach collection="list" item="u" separator=",">
  (#{u.name}, #{u.age})
</foreach>
```
```xml
<foreach collection="ids" item="id" open="(" separator="," close=")">
  #{id}
</foreach>
```

---

## 7. 占位符的区别
- `#{}`：安全的预编译绑定参数（推荐）
- `${}`：直接字符串拼接（有 SQL 注入风险，**仅用于列名、表名等受控场景**）

---

## 8. 常用小技巧
- 模糊匹配：
  ```xml
  WHERE name LIKE CONCAT('%', #{kw}, '%')
  ```
- 别名映射：
  ```xml
  SELECT user_name AS userName FROM t_user
  ```
- 分页（MySQL）：
  ```xml
  LIMIT #{offset}, #{pageSize}
  ```

---

**✅ 总结**  
- **固定标签**：`<select>`、`<insert>`、`<update>`、`<delete>`  
- **动态标签**：`<if>`、`<where>`、`<choose>`、`<set>`、`<foreach>`  
- **绑定规则**：`#{}` 安全绑定，`${}` 直接拼接。  
- **映射控制**：`resultType` 简单，`resultMap` 适合复杂映射。
----
# MyBatis `<resultMap>` 标签简要说明

`<resultMap>` 用于定义 **数据库字段 → Java 对象属性** 的映射关系，比 `resultType` 更灵活，适用于字段名不一致、复杂对象、一对一/一对多映射等场景。

---

## 基本用法

```xml
<resultMap id="UserMap" type="com.example.domain.User">
    <id property="id" column="id"/>         <!-- 主键字段映射 -->
    <result property="name" column="user_name"/>  <!-- 普通字段映射 -->
    <result property="age" column="user_age"/>
</resultMap>
```

- `<id>`：主键映射（缓存唯一性判断）
- `<result>`：普通字段映射
- `property`：Java 属性名
- `column`：数据库字段名

在 `<select>` 中使用：
```xml
<select id="selectById" parameterType="long" resultMap="UserMap">
    SELECT id, user_name, user_age FROM t_user WHERE id = #{id}
</select>
```

---

## 一对一（`<association>`）

适用于返回对象中包含另一个对象属性。

```xml
<resultMap id="OrderMap" type="com.example.domain.Order">
    <id property="id" column="id"/>
    <result property="orderNo" column="order_no"/>
    <association property="user" javaType="com.example.domain.User">
        <id property="id" column="user_id"/>
        <result property="name" column="user_name"/>
    </association>
</resultMap>
```

> 一个订单（Order）里有一个用户（User）。

---

## 一对多（`<collection>`）

适用于返回对象中包含一个集合（List/Set）。

```xml
<resultMap id="DeptMap" type="com.example.domain.Dept">
    <id property="id" column="id"/>
    <result property="deptName" column="dept_name"/>
    <collection property="employees" ofType="com.example.domain.Employee">
        <id property="id" column="emp_id"/>
        <result property="name" column="emp_name"/>
    </collection>
</resultMap>
```

> 一个部门（Dept）里有多个员工（List<Employee> employees）。

---

## 小结
- **字段一致** → `resultType`
- **字段不一致/复杂映射** → `resultMap`
- **一对一** → `<association>`
- **一对多** → `<collection>`



# MyBatis 参数与 @Param 使用笔记

## 1. `<if test="...">` 中变量来源
- **主要来源**：Mapper 接口方法的参数。
- **单参数**：变量名可直接用方法参数名。
- **多参数**：若未使用 `@Param`，MyBatis 默认使用 `param1`、`param2` … 访问。
- **复杂对象**：若传入对象，可以直接使用对象属性名（MyBatis 会自动展开属性）。
- **其他内置变量**：
  - `_parameter`：指整个参数对象。
  - `_databaseId`：区分不同数据库厂商。

## 2. 单参数场景
### 基础类型
```java
@Select("select * from employee where username = #{username}")
Employee getByUsername(String username);
```
- 不加 `@Param` 也能用，依赖编译器是否保留参数名。
- 加上 `@Param` 更稳妥：
  ```java
  Employee getByUsername(@Param("username") String username);
  ```

### 复杂对象（JavaBean/DTO）
```java
Employee getByCond(Employee emp);
```
XML：
```xml
<select id="getByCond" resultMap="UserMap">
  select * from employee
  where name = #{name} and age = #{age}
</select>
```
- 可以直接写属性名，如 `#{name}`，映射到 `emp.getName()`。
- 若开启 `type-aliases`，可以直接使用属性名，不必写成 `emp.name`。

## 3. 多参数场景
- **必须加 `@Param`**，否则只能用 `#{param1}`、`#{param2}`：
```java
Employee find(@Param("username") String username, @Param("age") Integer age);
```
SQL：
```sql
select * from employee where username = #{username} and age = #{age}
```

## 4. 总结规则
| 场景                 | 是否需要 `@Param` | 使用方式                         |
|----------------------|------------------|----------------------------------|
| 单参数（基础类型）    | 可加可不加        | `#{参数名}` 或 `#{value}`         |
| 单参数（对象类型）    | 可加可不加        | `#{属性名}`                      |
| 多参数               | 必须加            | `#{@Param指定的名字}`             |

👉 **推荐习惯**：
- 单参数：可以不加，但加上 `@Param` 更安全。
- 多参数：必须加 `@Param`。
- 使用对象参数时，结合 `type-aliases`，直接用属性名最简洁。

