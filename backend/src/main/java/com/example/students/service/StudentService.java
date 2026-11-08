// 【给前端同学】service 包 = 业务逻辑层。
package com.example.students.service;

// ↓ import ≈ 前端 import，把本层会用到的「数据形状」引进来
import com.example.students.common.PageResult;      // 分页返回结构
import com.example.students.dto.StudentQuery;       // 查询入参
import com.example.students.dto.StudentRequest;     // 新增/编辑入参
import com.example.students.entity.Student;         // 学生实体

/**
 * 【这个文件 ≈ 一份「能力清单」/ TS 的 interface，只有声明，没有实现】
 *
 * 它列出业务层「能做哪 4 件事」，具体实现在同目录 impl/StudentServiceImpl.java。
 * 前端类比：≈ 先定义 interface StudentApi { list(q): PageData; add(r): Student; ... }，
 *          再写一个 class 去 implements 它。
 *
 * 为什么要多这一层文件：Controller 只依赖这个「抽象」，不依赖具体实现类。
 * 将来想换实现（比如加缓存、换数据源），Controller 一行都不用改，
 * 上层完全不用感知下面的实现细节。
 */
public interface StudentService {

    // 分页 + 搜索：收查询参数，返回「当前页数据 + 总条数」
    PageResult<Student> list(StudentQuery query);

    // 新增：收表单数据，返回带自增 id 的完整学生对象
    Student add(StudentRequest request);

    // 编辑：路径 id + 表单数据，返回更新后的学生对象
    Student update(Long id, StudentRequest request);

    // 删除：void ≈ TS 的 void / Promise<void>，不返回数据，失败靠抛异常表达
    void delete(Long id);
}
