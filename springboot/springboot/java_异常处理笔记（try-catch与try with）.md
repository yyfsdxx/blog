# Java 异常处理笔记

---

## 一、普通 try-catch-finally

### 语法
```java
try {
    // 可能抛出异常的代码
} catch (ExceptionType1 e1) {
    // 处理异常1
} catch (ExceptionType2 e2) {
    // 处理异常2
} finally {
    // 一定会执行的代码块（通常用于释放资源）
}
```

### 特点
- **try**：放置可能抛出异常的代码。  
- **catch**：捕获并处理异常，可以有多个。  
- **finally**：无论是否发生异常都会执行，常用于关闭资源。  

### 示例
```java
FileInputStream fis = null;
try {
    fis = new FileInputStream("data.txt");
    int data = fis.read();
} catch (IOException e) {
    System.out.println("IO 异常: " + e.getMessage());
} finally {
    if (fis != null) {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

---

## 二、try-with-resources（JDK7+）

### 语法
```java
try (资源对象1; 资源对象2; ...) {
    // 使用资源
} catch (异常类型 e) {
    // 处理异常
}
// 资源会被自动关闭
```

### 要求
- 资源必须实现 `AutoCloseable` 或 `Closeable` 接口。  
- try 代码块结束时会自动调用 `close()` 方法，**不需要写 finally**。  

### 示例
```java
try (FileInputStream fis = new FileInputStream("data.txt")) {
    int data = fis.read();
} catch (IOException e) {
    e.printStackTrace();
}
// fis 会自动关闭
```

### 多资源示例
```java
try (
    FileInputStream fis = new FileInputStream("data.txt");
    FileOutputStream fos = new FileOutputStream("copy.txt")
) {
    int data;
    while ((data = fis.read()) != -1) {
        fos.write(data);
    }
} catch (IOException e) {
    e.printStackTrace();
}
// fos 和 fis 都会自动关闭（关闭顺序：fos -> fis）
```

---

## 三、对比

| 特性 | 普通 try-catch-finally | try-with-resources |
|------|-------------------------|---------------------|
| 代码量 | 较多，需要手动关闭资源 | 简洁，自动关闭资源 |
| 关闭顺序 | 手动控制 | 按声明顺序逆序关闭 |
| 适用范围 | 所有异常场景 | 主要用于需要关闭的资源 |

---

## 四、最佳实践
- 涉及 **文件、数据库连接、网络连接** 等资源 → 优先用 `try-with-resources`。  
- 需要在 `finally` 中做额外逻辑（例如打印日志、释放锁） → 继续用 `try-finally`。