// 【给前端同学】config 包放「框架配置」，≈ 前端的 vue.config.js / 各种 xxx.config.js。
package com.example.students.config;

// ↓ import ≈ 前端 import，把 Spring MVC 的配置能力引进来
import org.springframework.context.annotation.Configuration;                       // 「这是配置类」注解
import org.springframework.web.servlet.config.annotation.CorsRegistry;              // 跨域规则注册器
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;          // MVC 配置接口

/**
 * 【这个文件 ≈ 后端版的「跨域白名单」，和 vue.config.js 的 devServer.proxy 二选一】
 *
 * 浏览器的同源策略：8080 的页面默认不能请求 8081 的接口，会报 CORS 错误。两条解法：
 *   ① 默认方案（本项目正在用）：vue.config.js 把 /api 代理到 8081，浏览器始终只跟 8080 说话，
 *      属于同源请求，**根本用不到这个文件**。
 *   ② 直连方案：把 src/api/student.js 的 baseURL 改成 http://localhost:8081/api，
 *      这时浏览器直接访问 8081，就必须靠下面这份配置放行 8080。
 *
 * 所以：「页面改了数据但后端没反应」时，先检查前端是不是还开着 Mock；
 * 「报 CORS 错误」时，检查是不是用了直连却没配这份白名单。
 */
@Configuration
// ↑ 标记为配置类，Spring 启动时会加载它（≈ 这个文件会被 webpack「自动引入并执行」）
public class WebConfig implements WebMvcConfigurer {
// ↑ implements ≈ 实现一个接口/契约；这里表示「我要定制 Spring MVC 的行为」，
//   只覆盖自己关心的方法（addCorsMappings），其余保持框架默认

    @Override
    // ↑ 表示「重写父接口的方法」，写错方法名编译会报错，≈ 实现接口时的类型检查
    public void addCorsMappings(CorsRegistry registry) {
        // 链式配置，≈ 前端里一连串 .xxx() 的 options 配置；registry 就是「规则收集器」
        registry.addMapping("/api/**")   // 只对 /api 开头的所有接口生效（** ≈ 任意层级路径）
                .allowedOriginPatterns("http://localhost:8080", "http://127.0.0.1:8080")
                // ↑ 允许的来源（前端页面地址）白名单；其他域名来访会被浏览器拦下
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // ↑ 允许的 HTTP 方法，和 4 个接口一一对应；
                //   OPTIONS 是浏览器在发 PUT/DELETE 前自动发的「预检请求」，必须放行
                .allowedHeaders("*")     // 允许携带任意请求头，比如 Content-Type: application/json
                .allowCredentials(true) // 允许带 cookie / 登录凭证（本项目没用，先按标准配上）
                .maxAge(3600);          // 预检结果缓存 3600 秒，期间不用每次都发 OPTIONS，减少请求
    }
}
