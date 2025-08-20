# Java 静态类/方法/属性与非静态的区别

---

## 1. Java 中 `static` 的适用范围
- **属性**：静态字段，所有对象共享一份。
- **方法**：静态方法，不依赖实例，可以通过类名调用。
- **代码块**：静态代码块，在类加载时执行一次。
- **内部类**：静态内部类（static nested class），不依赖外部类实例。

⚠️ 注意：**顶级类不能用 `static` 修饰**。Java 没有“普通静态类”。

---

## 2. 静态内部类 vs 非静态内部类

### 静态内部类（static nested class）
```java
public class Outer {
    static class Inner {
        void say() {
            System.out.println("Hello from static inner class");
        }
    }
}

Outer.Inner inner = new Outer.Inner();
```
- 不依赖外部类实例。
- 只能访问外部类的 **静态成员**。
- 常用于 **Builder 模式**（如 MyBatis 的 `MappedStatement.Builder`）。

### 非静态内部类（inner class）
```java
public class Outer {
    private String msg = "Outer msg";

    class Inner {
        void say() {
            System.out.println("Hello, " + msg); // 可以直接用外部属性
        }
    }
}

Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();
```
- 必须依赖外部类实例（`outer.new Inner()`）。
- 可以访问外部类的所有成员（包括 private）。

---

## 3. 静态属性（static field）
```java
public class Example {
    static int count = 0;
    int id;

    public Example() {
        count++;
        id = count;
    }
}
```
- 所有对象共享一份 `count`。
- 属于类，不属于对象。
- 使用场景：全局计数器、缓存、常量。

---

## 4. 静态方法（static method）
```java
public class MathUtil {
    public static int add(int a, int b) {
        return a + b;
    }
}

int result = MathUtil.add(2, 3); // ✅ 直接类名调用
```
- 不依赖对象实例。
- 只能访问静态属性/方法，不能访问实例属性/方法。
- 使用场景：工具类方法（如 `Collections.sort()`、`Math.max()`）。

---

## 5. 顶级类为什么不能是 static？
- Java 中 `static` 的语义是：属于类，而不是对象。
- 顶级类本来就是由 **ClassLoader** 加载并在 JVM 中唯一存在，不依赖对象。
- 所以 `static` 修饰顶级类没有意义，Java 语法也不允许。
- 对比 C#：C# 允许“静态类”，只能包含静态成员，用于组织工具方法；Java 推荐用 **工具类 + private 构造器** 实现。

---

## 6. 对比总结表

| 类型              | 是否依赖外部对象 | 能访问外部实例成员？ | 典型场景 |
|-------------------|----------------|----------------------|----------|
| **静态内部类**    | ❌ 不依赖 | 只能访问外部类静态成员 | Builder 模式、工具封装 |
| **非静态内部类**  | ✅ 依赖 | 可以访问外部类所有成员 | 强耦合逻辑、封装子模块 |
| **静态属性**      | ❌ 属于类 | 全类共享 | 全局常量、计数器 |
| **静态方法**      | ❌ 属于类 | 不能访问实例成员 | 工具方法、工厂方法 |

---

## ✅ 总结
- Java 中“静态类”指的是 **静态内部类**，顶级类不能加 static。
- **静态属性/方法**：属于类本身，全局唯一。
- **非静态内部类**：依赖外部实例，适合和外部状态绑定。
- **静态内部类**：更独立，常用于 Builder、工具类的组织。

