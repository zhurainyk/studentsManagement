// 【给前端同学】mapper 包 = 持久层，专门跟数据库打交道。
package com.example.students.mapper;

// ↓ import ≈ 前端 import
import com.example.students.dto.StudentQuery;    // 查询参数对象
import com.example.students.entity.Student;      // 学生实体
import org.apache.ibatis.annotations.Param;      // 给方法参数起名，供 XML 里 #{名} 使用

import java.util.List;   // List ≈ JS 的数组 Array

/**
 * 【这个文件 ≈ src/api/student.js 的镜像：一串「只有声明、没有函数体」的请求函数】
 *
 * 前端：export const getStudents = (params) => api.get('/students', { params })  ← 有函数体
 * 这里：List<Student> selectByPage(StudentQuery query);                        ← 没有函数体
 *
 * 那函数体在哪？在 resources/mapper/StudentMapper.xml 里，靠两件事对上号：
 *   ① XML 的 namespace = 本接口的完整包名 com.example.students.mapper.StudentMapper
 *   ② XML 里 <select id="selectByPage"> 的 id = 这里的方法名
 * 启动类上的 @MapperScan 会让 MyBatis 在运行时生成代理实现类并注册成 Bean，
 * 所以 Service 里 studentMapper.selectByPage(...) 能直接调用（接口本来是不能被 new 的）。
 *
 * 前端类比：≈ 你只写了接口的类型声明，框架按「文件名 + 方法名」自动去别处加载实现。
 */
public interface StudentMapper {

    // ① 查当前页数据 → 对应前端表格 tableData；SQL 见 XML 的 <select id="selectByPage">
    //    参数 query 里带着 pageSize / offset / keyword，XML 直接读它的属性
    List<Student> selectByPage(StudentQuery query);

    // ② 查符合条件的**总条数** → 对应分页组件 :total；返回 long 是因为 COUNT(*) 可能很大
    //    和 ① 复用同一个 WHERE 条件，保证「当前页」和「总数」口径一致
    long countByKeyword(StudentQuery query);

    // ③ 按 id 查单个学生；查不到返回 null（编辑时用来判断「学生不存在」）
    //    @Param("id") 给参数起名为 "id"，XML 里才能写 WHERE id = #{id}
    Student selectById(@Param("id") Long id);

    // ④ 插入一行；返回 int 是「受影响行数」，本例插入成功为 1
    //    关键：XML 配了 useGeneratedKeys，插入后自增 id 会回填到传进去的 student 对象里
    int insert(Student student);

    // ⑤ 更新一行；返回受影响行数
    int update(Student student);

    // ⑥ 删除一行；返回受影响行数。Service 用「== 0」判断 id 不存在 → 抛 404
    int deleteById(@Param("id") Long id);
}
