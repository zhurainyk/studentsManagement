# 学生管理系统

一个基于 Vue2 + Element UI 的简易学生管理系统，用于学习前后端交互。
现已配套 Java 后端（Spring Boot + MyBatis），默认连接真实后端接口；`src/mock` 保留作为对照。

## 技术栈

- 前端：Vue 2.6.x、Element UI 2.x、Axios
- Mock.js（可选，纯前端演示时启用）
- 后端：Spring Boot 2.7 + MyBatis + H2 / MySQL（见 `backend/`）

## 功能

- ✅ 学生列表展示
- ✅ 按姓名搜索
- ✅ 分页功能
- ✅ 添加学生
- ✅ 编辑学生
- ✅ 删除学生

## 项目启动

当前默认连接 Java 后端，需要同时启动两端：

```bash
# 终端 1：启动后端（需 JDK 8+ 与 Maven，详见 backend/README.md）
cd backend
mvn spring-boot:run          # http://localhost:8081

# 终端 2：启动前端
npm install
npm run serve                # http://localhost:8080
```

`vue.config.js` 已把 `/api` 代理到 `http://localhost:8081`，因此前端 `src/api/student.js` 的 `baseURL` 无需修改，也不存在跨域问题。

只想跑纯前端演示时：把 `src/main.js` 中 `import './mock'` 的注释放开，即可脱离后端使用内存 Mock 数据。

## 项目结构

```
students/
├── backend/          # Java 后端（详见 backend/README.md）
│   └── src/main/{java,resources}/
├── src/
│   ├── api/          # API 接口（baseURL: /api）
│   ├── mock/         # Mock 数据（默认关闭，供对照）
│   ├── views/        # 页面组件
│   ├── components/   # 通用组件
│   ├── utils/        # 工具函数
│   ├── App.vue
│   └── main.js
└── public/
    └── index.html
```

## API接口

- GET    /api/students      获取学生列表（支持 page、pageSize、keyword）
- POST   /api/students      添加学生
- PUT    /api/students/:id  更新学生
- DELETE /api/students/:id  删除学生

所有响应统一为 `{ code, message, data }`；业务失败时 HTTP 状态仍为 200，但 `code` 为 `404`（学生不存在）或 `400`（参数校验失败），前端响应拦截器据此抛错。

## 数据模型

```javascript
{
  id: 1,              // 学号
  name: "张三",        // 姓名
  age: 20,            // 年龄
  className: "计科1班", // 班级
  phone: "13800138000" // 电话
}
```

## Java 后端

后端已完成上述四个接口的增删改查实现，包含统一响应、参数校验、分页搜索、事务、全局异常，以及 H2/MySQL 两种数据库配置。

分层结构、启动方式、curl 示例、切换 MySQL 与知识点地图见 [backend/README.md](backend/README.md)。
