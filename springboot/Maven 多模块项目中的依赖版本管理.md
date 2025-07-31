
# Maven 多模块项目中的依赖版本管理：Spring Boot 的最佳实践

在 Maven 构建系统中，合理管理依赖版本对于大型项目尤为重要，特别是在多模块项目中。本文将介绍如何使用 **Spring Boot 的 BOM（Bill of Materials）** 和 **`dependencyManagement`** 来集中管理版本，以及在父模块和子模块中如何配置依赖版本。

## 1. 使用 Spring Boot 的 BOM 来管理版本

当你使用 **Spring Boot** 作为父项目时，可以通过引入 Spring Boot 的 BOM 来自动管理版本号。这意味着你在子模块中不需要手动指定 Spring Boot 相关依赖的版本号，Maven 会自动根据父 POM 中的版本来选择。

### 如何使用 Spring Boot 的 BOM：

- **在根 `pom.xml` 中引入 Spring Boot 的 BOM**：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>2.5.4</version> <!-- 请根据项目需求选择合适的版本 -->
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

- **子模块中引入依赖时，只需指定 `groupId` 和 `artifactId`，不需要版本号**：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

通过这种方式，Maven 会自动根据 BOM 中的配置选择版本，避免在每个子模块中重复指定版本号。

## 2. 使用 `dependencyManagement` 管理版本号

`dependencyManagement` 是 Maven 的一个功能，允许你在父模块中集中管理版本号。子模块通过继承父模块的配置，可以自动继承依赖版本号，避免每个模块重复配置。

### 示例：

在 **根 `pom.xml`** 中：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.26</version> <!-- 集中管理版本号 -->
        </dependency>
    </dependencies>
</dependencyManagement>
```

在子模块的 `pom.xml` 中，只需声明 `groupId` 和 `artifactId`，无需指定版本号：

```xml
<dependencies>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <!-- 版本号自动继承自父模块的 dependencyManagement -->
    </dependency>
</dependencies>
```

## 3. 父模块 `pom.xml` 引入 `spring-boot-starter-parent` 的影响

当你在父模块的 `pom.xml` 中引入 **`spring-boot-starter-parent`** 时，Spring Boot 的相关版本（包括插件和依赖）会由父 POM 管理。父模块的所有配置都会被继承到子模块。因此，子模块**不需要再手动指定 Spring Boot 相关依赖的版本号**。

### 示例：

父模块 `pom.xml`：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.3</version> <!-- 使用 Spring Boot 的版本 -->
</parent>
```

在子模块中引入 Spring Boot 相关的依赖时，不需要指定版本号：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

## 4. 子模块不需要再次引入 `spring-boot-starter-parent`

子模块继承了父模块的所有配置，因此**子模块不需要在自己的 `pom.xml` 中再次引入 `spring-boot-starter-parent`**。Spring Boot 的版本管理会由父模块负责。

子模块 `pom.xml` 示例：

```xml
<parent>
    <groupId>com.sky</groupId> <!-- 父模块的 groupId -->
    <artifactId>sky-take-out</artifactId> <!-- 父模块的 artifactId -->
    <version>1.0-SNAPSHOT</version> <!-- 父模块的版本 -->
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- 不需要版本号，自动从父模块继承 -->
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <!-- 不需要版本号，自动从父模块继承 -->
    </dependency>
</dependencies>
```

## 5. `dependencyManagement` 和 `dependencies` 的区别

- **`dependencyManagement`**：用于集中管理版本号。你在父模块中声明依赖和版本，子模块只需要声明 `groupId` 和 `artifactId`，版本号会自动继承。
- **`dependencies`**：如果你直接在父模块的 `dependencies` 中声明了依赖并指定版本号，那么子模块仍然需要显式指定版本号。为了避免这种情况，建议使用 `dependencyManagement`。

## 6. Lombok 的 `scope` 设置

Lombok 是一个编译时依赖，因此应该为其指定 `provided` 范围，这样它不会被打包到最终的 JAR 文件中。

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

## 总结：

- **使用 Spring Boot 的 BOM**：让 Spring Boot 相关依赖的版本自动管理，避免手动指定版本号。
- **`dependencyManagement`**：集中管理版本号，子模块不需要显式指定版本号，只需引用 `groupId` 和 `artifactId`。
- **父 POM 使用 `spring-boot-starter-parent`**：子模块不需要为 Spring Boot 相关的依赖显式指定版本号。
- **`dependencies` 中声明版本号**：如果没有使用 `dependencyManagement`，子模块仍需要显式指定版本号。
- **Lombok 依赖的 `scope`**：将 Lombok 的 `scope` 设置为 `provided`，避免它被打包到最终 JAR。

通过这种方式，你可以有效地管理 Maven 多模块项目中的依赖版本，并保持项目的整洁和一致性。
