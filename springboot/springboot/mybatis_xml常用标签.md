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
