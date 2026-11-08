// 【给前端同学】controller 包 = 「接口层」，≈ 前端的路由表 + src/api/student.js 的服务端对应物。
package com.example.students.controller;

// ↓ 一堆 import ≈ 前端 import { getStudents } from '@/api/student' 之类，把需要的类引进来
import com.example.students.common.PageResult;      // 分页数据结构
import com.example.students.common.Result;          // 统一响应外壳
import com.example.students.dto.StudentQuery;       // GET 查询参数对象
import com.example.students.dto.StudentRequest;     // POST/PUT 请求体对象
import com.example.students.entity.Student;         // 学生实体（表格一行数据）
import com.example.students.service.StudentService; // 业务层接口
import org.springframework.web.bind.annotation.DeleteMapping;    // 匹配 DELETE 请求
import org.springframework.web.bind.annotation.GetMapping;       // 匹配 GET 请求
import org.springframework.web.bind.annotation.PathVariable;     // 取 URL 路径里的 {id}
import org.springframework.web.bind.annotation.PostMapping;      // 匹配 POST 请求
import org.springframework.web.bind.annotation.PutMapping;       // 匹配 PUT 请求
import org.springframework.web.bind.annotation.RequestBody;      // 把请求体 JSON 转成对象
import org.springframework.web.bind.annotation.RequestMapping;   // 类级别的公共 URL 前缀
import org.springframework.web.bind.annotation.RestController;   // 声明「这是接口类，返回值直接转 JSON」

import javax.validation.Valid;   // 触发参数校验的注解

/**
 * 【这个文件 ≈ 后端版的「路由表」+ src/mock/index.js 里的 4 个 Mock.mock 规则】
 *
 * 前端 src/api/student.js 发出的 4 个请求，就是被这里一一接住的：
 *   GET    /api/students?page=&pageSize=&keyword=   → list()
 *   POST   /api/students                            → add()
 *   PUT    /api/students/{id}                       → update()
 *   DELETE /api/students/{id}                       → delete()
 *
 * 这一层的职责很薄，只做 3 件事（≈ 前端路由组件只负责取参 + 调 api + 渲染）：
 *   ① 把 HTTP 请求的参数「绑定」成 Java 对象
 *   ② 触发参数校验（@Valid）
 *   ③ 调 Service 拿结果，用 Result.success(...) 包成前端认识的 JSON 形状
 * 它**不写业务判断、不写 SQL**，那些分别在 Service 和 Mapper 里。
 *
 * 返回类型里的泛型就是最终 JSON 的形状，例如：
 *   Result<PageResult<Student>> → { code, message, data: { list, total, page, pageSize } }
 */
@RestController
// ↑ = @Controller + @ResponseBody：方法返回的对象会被 Jackson 自动转成 JSON，
//   而不是被当成「页面模板名」（≈ 告诉框架「别找 .vue 了，直接返回数据」）
@RequestMapping("/api/students")
// ↑ 类级公共前缀，下面 4 个方法的路径都拼在它后面；和前端 baseURL: '/api' + '/students' 刚好对上
public class StudentController {

    // 依赖的是「接口」StudentService，不是具体实现类。
    // 前端类比：≈ 组件里只 import 一个 api 模块，不关心它内部是用 axios 还是 fetch 实现的。
    private final StudentService studentService;   // final ≈ 赋值后不可变（类似 const，但指引用不可改）

    // 构造器注入：Spring 创建本类时，会自动从容器里找到 StudentService 的实现（StudentServiceImpl）
    // 传进来。全项目没有一行 new StudentController() / new StudentServiceImpl()，就是靠这个机制。
    // 好处：将来要替换实现时，Controller 这一层完全不用改。
    public StudentController(StudentService studentService) {
        this.studentService = studentService;   // this. 指本对象字段，右边是传进来的参数
    }

    // ① 列表 / 搜索 / 分页：对应前端 getStudents({page, pageSize, keyword})
    @GetMapping
    // ↑ 匹配 GET /api/students。没写路径是因为类上已有 /api/students
    public Result<PageResult<Student>> list(StudentQuery query) {
        // StudentQuery 前没加任何注解：Spring 会按「参数名 = 属性名」自动把
        // ?page=1&pageSize=10&keyword=张 填进 query 对象（≈ 前端把 URL query 解析成对象）
        // 一行干两件事：调 Service 拿分页数据 → 用 Result.success 包上 code/message 返回
        return Result.success(studentService.list(query));
    }

    // ② 新增：对应前端 addStudent(data)
    @PostMapping
    public Result<Student> add(@Valid @RequestBody StudentRequest request) {
        // @RequestBody：把请求体 JSON 自动转成 StudentRequest 对象（≈ JSON.parse(body) 后赋给表单对象）
        // @Valid：立刻按 StudentRequest 上的注解做校验，不通过就直接抛异常，
        //         方法体根本不会执行，由 GlobalExceptionHandler 转成 code 400
        // 成功后 message 是「添加成功」，data 是带自增 id 的新学生对象
        return Result.success("添加成功", studentService.add(request));
    }

    // ③ 编辑：对应前端 updateStudent(id, data)
    @PutMapping("/{id}")
    // ↑ {id} 是路径占位符，≈ 前端路由 /user/:id 里的动态段
    public Result<Student> update(@PathVariable Long id,
                                  // ↑ 把 URL 里的 /api/students/1 的 1 取出来转成 Long（≈ JS 里的 number）
                                  //   编辑以**路径上的 id 为准**，请求体里带的 id 会被忽略，防止改错数据
                                  @Valid @RequestBody StudentRequest request) {
        // 同上：先校验请求体，再交给 Service；Service 会检查该 id 是否存在，不存在抛 404
        return Result.success("更新成功", studentService.update(id, request));
    }

    // ④ 删除：对应前端 deleteStudent(id)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // Result<Void> = data 恒为 null；前端只用 res.code === 200 判断成功
        studentService.delete(id);   // 删除没有返回值，Service 里若删不到会抛 BusinessException(404)
        return Result.success("删除成功", null);
    }
}
