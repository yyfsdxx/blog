# Java 受检异常笔记

在 Java 中，异常分为两类：

## ✅ 受检异常（Checked Exceptions）

- 继承自 `Exception`（但不是其子类 `RuntimeException`）
- 编译器会检查，**必须显示声明或捕获**，否则编译不通过
- 常见受检异常：
  - `IOException`
  - `SQLException`
  - `ClassNotFoundException`
  - `ParseException`
  - `FileNotFoundException`
  - `InterruptedException`

### 使用方式：

```java
public void readFile(String path) throws IOException {
    FileReader reader = new FileReader(path);
}
```

或使用 try-catch 处理：

```java
try {
    readFile("test.txt");
} catch (IOException e) {
    e.printStackTrace();
}
```

---

## ❌ 非受检异常（Unchecked Exceptions）

- 继承自 `RuntimeException`
- 编译器**不强制要求处理**
- 常见非受检异常：
  - `NullPointerException`
  - `IndexOutOfBoundsException`
  - `IllegalArgumentException`
  - `ArithmeticException`
  - `ClassCastException`

---

## ⚠️ 总结

| 类型       | 是否强制处理 | 典型代表           |
|------------|----------------|--------------------|
| 受检异常   | ✅ 是           | IOException, SQLException |
| 非受检异常 | ❌ 否           | NullPointerException, RuntimeException |

编写代码时，IDE 会提示你是否需要抛出或捕获受检异常，确保程序的**健壮性与安全性**。
