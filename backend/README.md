# 学生管理系统 · Java 后端

与前端 `src/api/student.js` 的四个接口**严格对齐**的增删改查后端，用于学习 Java 后端分层开发。

技术栈：Spring Boot 2.7 + MyBatis + Bean Validation + H2（默认，零安装）/ MySQL（可选）。
默认使用 H2 内存库，启动时自动建表并导入与 `src/mock/data.js` 完全一致的 10 条数据。

## 分层结构

```
backend/
├── pom.xml
└── src
    └── main
        ├── java/com/example/students/
        │   ├── StudentsApplication.java       启动类（@MapperScan）
        │   ├── common/
        │   │   ├── Result.java                统一响应 {code, message, data}
        │   │   ├── PageResult.java            分页 {list, total, page, pageSize}
        │   │   ├── BusinessException.java     业务异常
        │   │   └── GlobalExceptionHandler.java 全局异常 -> 统一响应
        │   ├── config/WebConfig.java          跨域（直连方案用）
        │   ├── entity/Student.java            实体（对应表 students）
        │   ├── dto/
        │   │   ├── StudentQuery.java          分页/搜索入参
        │   │   └── StudentRequest.java        新增/编辑入参 + 校验规则
        │   ├── controller/StudentController.java  接收 HTTP 请求
        │   ├── service/                       业务逻辑接口 + 实现（事务）
        │   └── mapper/StudentMapper.java      持久层接口
        └── resources/
            ├── application.yml                默认 H2 配置
            ├── application-mysql.yml          可选 MySQL 配置
            ├── schema-h2.sql / schema-mysql.sql 建表脚本
            ├── data.sql                       初始数据
            └── mapper/StudentMapper.xml       手写 SQL（动态条件 / 分页 / CRUD）
```

请求链路：`StudentList.vue` → axios → `StudentController` → `StudentService` → `StudentMapper` → `StudentMapper.xml` → 数据库。

## 环境准备

需要 **JDK 8+（推荐 11/17）** 和 **Maven 3.6+**。当前开发机已安装完成，新开终端即可直接使用：

- Temurin JDK 17.0.21：`~/.local/opt/jdk-17`（`java`、`javac` 已链到 `/opt/homebrew/bin`）
- Maven 3.9.16：`~/.local/opt/maven`（`mvn` 已链到 `/opt/homebrew/bin`）
- `~/.zshrc` 已配置 `JAVA_HOME` 与 `PATH`（标记为 `STUDENTS_JAVA_SETUP`）
- `~/.m2/settings.xml` 使用阿里云镜像加速依赖下载

```bash
java -version && mvn -v
```

其它机器可以用 `brew install openjdk@17 maven`；若 Homebrew 源很慢（`maven` 还会连带安装最新版 openjdk 及一批图形库依赖），更快的做法是从清华镜像下载 Temurin JDK 17 与 Maven 二进制包解压到用户目录，再配置 `JAVA_HOME` 与 `PATH`。也可以直接用 IntelliJ IDEA 打开 `backend/pom.xml`。

## 启动与验证

```bash
cd backend
mvn spring-boot:run
# 另开一个终端，用下面的 curl 示例逐个验证接口
```

服务运行在 `http://localhost:8081`。可直接查看内存库：`http://localhost:8081/h2-console`
（JDBC URL 填 `jdbc:h2:mem:students`，用户 `sa`，密码留空）。

单独验证一个接口：

```bash
curl 'http://localhost:8081/api/students?page=1&pageSize=10&keyword=张'
```

## 前端联调

前端已经改好，无需再改 `src/api/student.js`：

1. `src/main.js` 中已注释掉 `import './mock'`，请求会真正发出（mock 目录保留，方便对照学习）。
2. `vue.config.js` 的 `devServer.proxy` 把 `/api` 转发到 `http://localhost:8081`，因此前端仍是同源请求，不产生跨域问题。

```bash
# 终端 1
cd backend && mvn spring-boot:run
# 终端 2
cd /path/to/students && npm run serve   # http://localhost:8080
```

想切回纯前端 Mock：把 `src/main.js` 里的 `import './mock'` 放开即可（proxy 可以留着，不会被用到）。

另一种联调方式是直连：把 `src/api/student.js` 的 `baseURL` 改为 `http://localhost:8081/api`，
此时由后端的 `WebConfig` 放行 `http://localhost:8080` 的跨域请求。

## 接口契约

所有响应均为 HTTP 200 + JSON，失败通过 `code` 表达（与原 Mock.js 一致，前端拦截器按 `code !== 200` 抛错）。

| 方法 | 路径 | 说明 | data |
|---|---|---|---|
| GET | `/api/students?page=1&pageSize=10&keyword=` | 分页 + 姓名模糊搜索 | `{list, total, page, pageSize}`，message `success` |
| POST | `/api/students` | 新增，id 由数据库自增生成 | 新增后的学生对象，message `添加成功` |
| PUT | `/api/students/{id}` | 编辑 | 更新后的学生对象，message `更新成功`；不存在时 code `404` / `学生不存在` |
| DELETE | `/api/students/{id}` | 删除 | `null`，message `删除成功`；不存在时 code `404` |

请求体（POST/PUT）：`{"name":"张三","age":20,"className":"计科1班","phone":"13800138000"}`
后端校验规则与 `StudentForm.vue` 的 rules 一致：姓名 2~20 字、年龄 1~150、班级必填、手机号 `^1[3-9]\d{9}$`；
校验不通过返回 code `400` 和具体错误信息。

```bash
# 查询：第二页 / 搜索
curl 'http://localhost:8081/api/students?page=2&pageSize=5'
curl 'http://localhost:8081/api/students?keyword=李'

# 新增
curl -X POST 'http://localhost:8081/api/students' \
  -H 'Content-Type: application/json' \
  -d '{"name":"新同学","age":20,"className":"计科1班","phone":"13800138000"}'

# 编辑（body 里的 id 会被忽略，以路径 id 为准）
curl -X PUT 'http://localhost:8081/api/students/1' \
  -H 'Content-Type: application/json' \
  -d '{"name":"张三丰","age":21,"className":"计科2班","phone":"13800138001"}'

# 删除
curl -X DELETE 'http://localhost:8081/api/students/1'

# 观察失败场景：不存在的 id、非法参数
curl -X DELETE 'http://localhost:8081/api/students/9999'
curl -X POST 'http://localhost:8081/api/students' \
  -H 'Content-Type: application/json' \
  -d '{"name":"A","age":0,"className":"","phone":"123"}'
```

## 切换到真实 MySQL

```bash
mysql -uroot -p -e 'CREATE DATABASE students DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
mysql -uroot -p students < src/main/resources/schema-mysql.sql
mysql -uroot -p students < src/main/resources/data.sql
# 按需修改 application-mysql.yml 里的账号密码
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

MySQL profile 关闭了自动初始化（`spring.sql.init.mode: never`），避免每次重启重复插入示例数据；
H2 profile 是内存库，每次启动重建，数据不持久化，适合练习。

## 可以顺着代码学习的知识点

1. **分层**：Controller（HTTP）→ Service（业务规则、事务 `@Transactional`）→ Mapper（SQL）。
2. **REST 映射**：`@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`、`@PathVariable`、`@RequestBody`。
3. **参数校验**：`@Valid` + `javax.validation` 注解，异常由 `@RestControllerAdvice` 统一处理。
4. **统一响应**：`Result<T>` 泛型、`code/message/data` 契约，以及"业务失败也返回 HTTP 200"的原因。
5. **MyBatis**：`namespace` 与接口全限定名绑定、`<if>/<where>` 动态 SQL、`#{ }` 预编译占位符防 SQL 注入、`useGeneratedKeys` 回填自增主键、下划线转驼峰。
6. **分页**：`LIMIT ... OFFSET ...` + 独立 `COUNT(*)`，以及 Service 里对 `page/pageSize` 的边界规整。
7. **数据库**：DDL、自增主键、H2 与 MySQL 的方言差异、profile 切换。
8. **联调**：devServer 代理 vs CORS 跨域、Mock 与真实接口的切换。

## 常见坑

- 页面数据没变化时，先确认 `src/main.js` 里是否还开着 Mock（Mock.js 会直接拦截 XHR，请求根本到不了后端）。
- 数据库列是 `class_name`，Java/前端字段是 `className`，靠 `map-underscore-to-camel-case` 映射。
- 业务 404 不是 HTTP 404：前端 `api.interceptors.response.use` 读取的是响应体里的 `code`。
- 改了 `baseURL` 直连后端时，端口必须是 8081，且需 CORS 放行；用代理方案则保持 `/api` 不变。
