
# Maven 多模块项目中的模块调用与命名规范说明

本笔记总结了你在多模块 Maven 项目中遇到的几个关键问题，包括模块之间的依赖调用、包名与 groupId 的区别，以及 Maven 中 groupId 的继承规则。

---

## 📁 一、模块之间如何调用 Java 文件

在一个多模块 Maven 项目中，比如你有如下结构：

```
sky-take-out/
├── sky-common/
├── sky-pojo/
└── sky-server/
```

如果 `sky-server` 需要调用 `sky-pojo` 模块中的类，需要做以下两步：

1. **在 `sky-server/pom.xml` 中加入对 `sky-pojo` 的依赖：**

```xml
<dependency>
    <groupId>com.sky</groupId>
    <artifactId>sky-pojo</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

2. **在 Java 文件中使用 `import` 语句引用目标类，例如：**

```java
import org.yyf.dto.EmployeeLoginDTO;
```

---

## 📦 二、包名（package） vs 组名（groupId）

| 名称        | 作用域         | 示例              | 用途                           |
|-------------|----------------|-------------------|--------------------------------|
| 包名        | Java 代码组织   | `org.yyf.dto`     | 决定类的全路径、目录结构        |
| 组名（groupId） | Maven 坐标系统 | `com.sky`         | 用于唯一标识 Maven 项目的发布归属 |

🔹 `import org.yyf.xxx` 是在 **引用 Java 的包名**，与 Maven 的 `groupId` 没有直接关系。

🔹 即使你所有模块的 `groupId` 都是一样的，也不会影响 `import` 行为，只要 **Java 包名不冲突** 即可。

---

## 📄 三、groupId 的继承规则

- 如果子模块的 `pom.xml` 中没有显式写 `<groupId>`，**Maven 会尝试继承其父模块的 groupId。**

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>
```

- 在这种情况下，子模块实际的 Maven 坐标为：

```
groupId: com.sky
artifactId: sky-pojo
version: 1.0-SNAPSHOT
```

⚠️ 注意：**Java 的包名 `org.yyf.xxx` 并不会影响 Maven 坐标。**

---

## ✅ 建议

- 尽量保持 **groupId 与包名风格统一**，便于管理和识别。
- 避免不同模块中出现相同包名下的同名类，容易导致冲突。

---

以上内容适用于你当前的项目结构，可以直接用于博客或文档。
