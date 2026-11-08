// 【给前端同学】package 相当于「这个文件放在哪个目录」。
// 它还有一个额外作用：Spring 只会扫描 com.example.students 包及其子包下的代码，
// 所有业务类必须放在这个包里，放出去就「扫描不到」≈ 写了文件但没被 import，永远不会执行。
package com.example.students;

// ↓ 下面 3 行 ≈ 前端的 import xxx from '...'，把框架能力引进来
import org.mybatis.spring.annotation.MapperScan;                        // MyBatis 的「扫描 Mapper 接口」注解
import org.springframework.boot.SpringApplication;                      // Spring Boot 的启动函数
import org.springframework.boot.autoconfigure.SpringBootApplication;     // 最核心的启动注解

/**
 * 【这个文件 ≈ 前端项目里的 src/main.js】
 *
 * 前端 main.js 里写 new Vue({ render: h => h(App) }).$mount('#app')，挂载一个页面；
 * 这里写 SpringApplication.run(...)，启动的是一个常驻的 HTTP 服务器（内嵌 Tomcat，监听 8081 端口）。
 * 它不渲染页面，只负责「接请求 → 查数据库 → 返回 JSON」。
 *
 * 执行 mvn spring-boot:run 后，这个 main 会依次触发：
 *   1) 启动内嵌 Tomcat，监听 8081（前端 8080 的请求由 vue.config.js 代理转发过来）
 *   2) 创建 Spring 容器，实例化所有带 @RestController / @Service / @Configuration 的类（叫 Bean）
 *   3) 让 MyBatis 给 Mapper 接口生成代理实现
 *   4) 执行 schema-h2.sql 建表、data.sql 插入 10 条初始数据
 *   5) 服务就绪，进程一直不退出，等着接请求
 *
 * 【在请求链路中的位置】一切的开始。平时写业务基本不用改这个文件。
 */
@SpringBootApplication
// ↑ 三合一注解，作用 ≈「自动注册路由 + 按依赖自动装插件」：
//   ① 声明自己是配置类  ② 开启自动配置（看 pom.xml 引了什么依赖，就把 Web、MyBatis 等自动配好）
//   ③ 组件扫描：把本包下 @RestController / @Service / @Configuration 的类都 new 成对象放进容器
//   容器 ≈ 一个全局的对象仓库，后面谁需要谁，框架自动递过去（这就是「依赖注入」，见 Controller 的构造器）
@MapperScan("com.example.students.mapper")
// ↑ 告诉 MyBatis 去扫描 mapper 包，给里面的每个接口生成一个「代理实现类」并注册进容器。
//   前端类比：≈ 自动帮你把 src/api/ 里那些只有声明、没有函数体的请求函数补上实现，
//   而「函数体」写在 resources/mapper/StudentMapper.xml 里（靠 namespace + id 对上号）。
//   有了这一行，StudentMapper 接口上就不用再写 @Mapper 了。
public class StudentsApplication {

    // main 方法 = 程序入口，≈ 前端打包后最先执行的那段代码；args ≈ process.argv（命令行参数，这里没用）
    public static void main(String[] args) {
        // run(...) 启动整个应用，并且会「一直运行不退出」，因为 Tomcat 要持续监听端口等请求。
        // 传 StudentsApplication.class 是告诉 Spring「从哪个类开始找配置」。
        SpringApplication.run(StudentsApplication.class, args);
    }
}
