
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

## ✅ 小结关键词

| 关键词 | 说明 |
|--------|------|
| `IS NULL` | 判断空值 |
| `!=` | 不等于 |
| `DISTINCT` | 去重 |
| `AS` | 字段别名 |
| `ORDER BY` | 排序 |
| `LENGTH` / `CHAR_LENGTH` | 字符长度（字节/字符） |
| `CASE WHEN` | 条件分组 |
| `COUNT(*)` | 统计数量 |
