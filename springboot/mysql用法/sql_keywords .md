# SQL 实战笔记汇总（关键词精要版）

## ✅ 题目 584. 寻找用户推荐人

### 📌 关键词：`NULL` 判断、逻辑运算符、字段别名

```sql
SELECT name
FROM Customer
WHERE referee_id != 2 OR referee_id IS NULL;
```

- `IS NULL` 用于判断字段是否为空，不能用 `= NULL`
- `!=` 表示“不等于”
- **大小写在 MySQL 中默认不敏感**
- 推荐使用大写关键字、统一小写表/字段名

---

## ✅ 题目 1148. 文章浏览 I

### 📌 关键词：`DISTINCT` 去重、字段重命名、条件筛选

```sql
SELECT DISTINCT viewer_id AS id
FROM Views
WHERE author_id = viewer_id
ORDER BY id;
```

- `DISTINCT` 去重
- `AS id` 用于临时重命名列名，不影响原表结构
- 注意题目要求字段名为 `id`，否则系统可能判错

---

## ✅ 题目 1683. 无效的推文

### 📌 关键词：`LENGTH` / `CHAR_LENGTH`、字符串长度判断、数据统计

```sql
SELECT tweet_id
FROM Tweets
WHERE CHAR_LENGTH(content) > 15;
```

- `CHAR_LENGTH` 更语义清晰（返回字符数）
- 当仅包含字母、数字、空格和 `!` 时，`LENGTH` 和 `CHAR_LENGTH` 等价

#### 📊 拓展应用

- **统计无效推文数量：**

```sql
SELECT COUNT(*) AS invalid_tweet_count
FROM Tweets
WHERE CHAR_LENGTH(content) > 15;
```

- **按有效/无效分类统计：**

```sql
SELECT
  CASE WHEN CHAR_LENGTH(content) > 15 THEN 'Invalid' ELSE 'Valid' END AS status,
  COUNT(*) AS count
FROM Tweets
GROUP BY status;
```

- **找出最长内容：**

```sql
SELECT tweet_id, content
FROM Tweets
ORDER BY CHAR_LENGTH(content) DESC
LIMIT 1;
```

---

## ✅ 子查询别名（AS total_query）

### 📌 关键词：子查询、别名、语法要求

```sql
SELECT COUNT(*)
FROM (
    SELECT id, name, age FROM user WHERE age > 20
) AS total_query;
```

- `AS total_query` 是给子查询结果起一个**临时表名**。
- **语法要求**：在 MySQL、PostgreSQL 等数据库中，`FROM (子查询)` 后必须有别名，否则报错。
- **便于引用**：外层查询如果需要用到子查询的列，可以通过别名访问。
- **可读性**：让 SQL 结构更清晰。
- 在分页插件里，这个别名主要是为了满足 SQL 合法性，**即使外层没引用也必须写**。

错误示例（缺少别名）：
```sql
SELECT COUNT(*) FROM (SELECT id, name FROM user);
-- 报错：Every derived table must have its own alias
```

正确示例：
```sql
SELECT COUNT(*) FROM (SELECT id, name FROM user) AS t;
```

---

## ✅ LIMIT 关键字

### 📌 基本语法（MySQL/MariaDB）

```sql
SELECT 列名
FROM 表名
[WHERE 条件]
[ORDER BY 排序]
LIMIT [offset], [row_count];
```

- `row_count`：返回的记录数。
- `offset`：起始偏移量，从 0 开始计数。
- 常见用法：
  - `LIMIT 10` → 返回前 10 条。
  - `LIMIT 5, 10` → 跳过前 5 条，取接下来的 10 条。

### 📌 示例

```sql
-- 取前 3 条
SELECT * FROM t_user LIMIT 3;

-- 跳过前 5 条，取 10 条
SELECT * FROM t_user LIMIT 5, 10;

-- 分页查询，第 pageNum 页，每页 pageSize 条
SET @offset = (pageNum - 1) * pageSize;
SELECT * FROM t_user ORDER BY id LIMIT @offset, pageSize;
```

### 📌 PostgreSQL 语法

```sql
SELECT *
FROM t_user
ORDER BY id
LIMIT pageSize OFFSET offset;
```

### 📌 SQL Server / Oracle 12c+

```sql
SELECT *
FROM t_user
ORDER BY id
OFFSET offset ROWS FETCH NEXT pageSize ROWS ONLY;
```

### ⚠️ 注意
- `offset` 越大，性能越差（需要扫描更多行）。大数据量场景常结合索引或主键游标优化。
- `LIMIT` 语法是 MySQL/MariaDB 独有，跨数据库时需使用对应方言。

---

## ✅ 小结关键词

| 关键词 | 说明 |
|--------|------|
| `IS NULL` | 判断空值 |
| `!=` | 不等于 |
| `DISTINCT` | 去重 |
| `AS` | 字段/表别名 |
| `ORDER BY` | 排序 |
| `LENGTH` / `CHAR_LENGTH` | 字符长度（字节/字符） |
| `CASE WHEN` | 条件分组 |
| `COUNT(*)` | 统计数量 |
| **子查询别名** | 子查询结果必须命名（如 `AS total_query`） |
| **LIMIT** | 限制返回记录数 / 分页 |

