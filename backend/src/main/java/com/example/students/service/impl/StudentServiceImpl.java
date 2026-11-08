// 【给前端同学】impl = implementation（实现），这里放的是 StudentService 的「函数体」。
package com.example.students.service.impl;

// ↓ import ≈ 前端 import，把依赖的类引进来
import com.example.students.common.BusinessException;   // 业务异常（带 code 的自定义 Error）
import com.example.students.common.PageResult;          // 分页结果
import com.example.students.common.Result;              // 只为用里面的常量 Result.NOT_FOUND
import com.example.students.dto.StudentQuery;           // 查询参数对象
import com.example.students.dto.StudentRequest;         // 表单请求对象
import com.example.students.entity.Student;             // 学生实体
import com.example.students.mapper.StudentMapper;       // 持久层接口（连数据库）
import com.example.students.service.StudentService;     // 上面那份「能力清单」
import org.springframework.stereotype.Service;                        // 「这是业务层 Bean」注解
import org.springframework.transaction.annotation.Transactional;      // 事务注解

import java.util.List;   // List ≈ JS 数组 Array

/**
 * 【这个文件 = 全项目业务规则的核心，≈ 前端 store 里的 actions / composable 里的业务函数】
 *
 * Controller 只负责接请求，Mapper 只负责执行 SQL，「该怎么处理数据」全在这里：
 *   · 分页参数兜底与封顶      · 关键词清洗
 *   · 算 offset（跳过多少行）  · 查两次库拿到「当前页」和「总条数」
 *   · 判断学生存不存在 → 抛异常 · 把表单对象拷贝成数据库实体
 *
 * 与 src/mock/index.js 的对照（前端看这个最容易懂）：
 *   Mock 里 filtered = students.filter(...) + slice(start, end)
 *   这里换成 selectByPage(...) + countByKeyword(...) 两条真 SQL，其余逻辑一一对应。
 */
@Service
// ↑ 把本类注册成 Spring 容器里的 Bean；容器创建它时会自动把需要的 StudentMapper 传进构造器
public class StudentServiceImpl implements StudentService {
// ↑ implements = 实现上面那份「能力清单」，下面 4 个方法就是清单里 4 个声明的函数体

    // 每页最多 100 条：防止有人传 pageSize=9999999 把整张表拉出来拖垮服务
    private static final int MAX_PAGE_SIZE = 100;   // static final ≈ 全局常量（类似 const，全类共用一份）

    private final StudentMapper studentMapper;   // 持久层对象，由 Spring 构造器注入，不用自己 new

    // 构造器注入：Spring 自动把 MyBatis 生成的 Mapper 代理对象递进来（见 StudentsApplication 的 @MapperScan）
    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    // ───────────────────────── ① 列表 / 搜索 / 分页 ─────────────────────────
    @Override   // 表示「实现接口里同名方法」，方法名写错会编译报错
    public PageResult<Student> list(StudentQuery query) {
        // 第 1 步：规整分页参数，不信任前端传来的值（≈ 前端也会给 currentPage 设默认值 1）
        // query.getPage() 可能为 null（URL 没带 page）或 0/负数，两种情况统一回退成 1
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        // 三元表达式 a ? b : c ≈ JS 里的 a ? b : c；pageSize 同理，非法值回退成 10，
        // Math.min(..., 100) 再做封顶，保证不会超过 MAX_PAGE_SIZE
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1
                ? 10 : Math.min(query.getPageSize(), MAX_PAGE_SIZE);

        // 第 2 步：清洗关键词。null 保持 null，非 null 先去掉首尾空格
        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        // 空字符串按「不筛选」处理：转成 null 后，XML 里的 <if> 才不会拼出 LIKE '%%' 这种全匹配条件
        if (keyword != null && keyword.isEmpty()) {
            keyword = null;
        }
        // 注意：这里刻意没有转义 LIKE 的通配符 % 和 _，是教学简化；
        // 真实项目要考虑用户搜「%」时把整表查出来的情况。

        // 第 3 步：把「算好的值」回写进 query 对象（它会被一路传给 XML 当参数袋用）
        query.setPage(page);            // 回写规整后的页码
        query.setPageSize(pageSize);    // 回写规整后的每页条数 → XML 里 LIMIT #{pageSize}
        query.setKeyword(keyword);      // 回写清洗后的关键词 → XML 里 LIKE #{keyword}
        query.setOffset((page - 1) * pageSize);
        // ↑ offset = 要跳过的行数。第 1 页跳 0 行，第 2 页(每页10条)跳 10 行……
        //   ≈ Mock 里的 const start = (page - 1) * pageSize；对应 SQL 的 OFFSET

        // 第 4 步：查两次数据库（分页必须查两次，这是初学者常问的点）
        List<Student> list = studentMapper.selectByPage(query);  // ① 当前这一页的数据 → 表格 tableData
        long total = studentMapper.countByKeyword(query);        // ② 符合搜索条件的**总条数** → 分页组件 total
                                                                 //    两条 SQL 复用同一个 WHERE 条件，保证数据一致
        // 第 5 步：打包成分页对象返回给 Controller（Controller 再套一层 Result）
        return new PageResult<>(list, total, page, pageSize);
    }

    // ───────────────────────── ② 新增 ─────────────────────────
    @Override
    @Transactional
    // ↑ 事务：方法内所有数据库操作「要么全成功，要么全回滚」。
    //   本方法只有一条 INSERT，看不出效果；是示范「写操作的事务边界应划在 Service 层」这个习惯。
    public Student add(StudentRequest request) {
        Student student = new Student();   // 新建一个空的实体对象，≈ const row = {}
        copyFields(request, student);      // 把表单 4 个字段拷进实体（此处不设 id，交给数据库自增）
        // 执行 INSERT。关键：XML 里配了 useGeneratedKeys="true" keyProperty="id"，
        // 插入后数据库生成的主键会被**回填**到 student.id，所以下面能直接返回带 id 的对象
        // （≈ Mock 里 student.id = maxId + 1 那一步，只不过这里由数据库算）
        studentMapper.insert(student);
        return student;                    // 返回带 id 的新学生 → 前端拿到后 message「添加成功」
    }

    // ───────────────────────── ③ 编辑 ─────────────────────────
    @Override
    @Transactional   // 同样：写操作放在事务里
    public Student update(Long id, StudentRequest request) {
        // 先查一次：id 不存在就抛业务异常，异常会被 GlobalExceptionHandler 转成 code 404
        // ≈ Mock 里 if (index === -1) return { code: 404, message: '学生不存在' }
        if (studentMapper.selectById(id) == null) {
            throw new BusinessException(Result.NOT_FOUND, "学生不存在");
        }
        Student student = new Student();   // 组装要写进库的实体
        student.setId(id);                 // id 只用 URL 路径上的，忽略请求体里可能带的 id
        copyFields(request, student);      // 拷贝表单的 4 个字段
        studentMapper.update(student);     // 执行 UPDATE ... WHERE id = #{id}
        // 再查一次返回「数据库里的最新对象」，
        // 与 Mock 里 return studentData.students[index] 的行为保持一致
        return studentMapper.selectById(id);
    }

    // ───────────────────────── ④ 删除 ─────────────────────────
    @Override
    @Transactional
    public void delete(Long id) {
        // 不先查、直接删：deleteById 返回「受影响行数」（删了几行）。
        // 为 0 说明库里没这个 id → 抛 404。比「先 select 再 delete」少一次数据库往返。
        if (studentMapper.deleteById(id) == 0) {
            throw new BusinessException(Result.NOT_FOUND, "学生不存在");
        }
        // 没抛异常就是删成功了；void 方法不返回数据，Controller 统一回「删除成功」
    }

    // ───────────────────────── 私有小工具 ─────────────────────────
    // private = 只在类内部用（≈ 模块里没 export 的函数）
    // 作用：add / update 都要做这 4 行赋值，抽出来避免重复两遍
    // （≈ 前端写 const pick = (o) => ({ name: o.name, age: o.age, ... })）
    private void copyFields(StudentRequest request, Student student) {
        student.setName(request.getName());          // name，对应数据库列 name
        student.setAge(request.getAge());            // age，对应列 age
        student.setClassName(request.getClassName());// className，对应列 class_name（靠驼峰映射自动转换）
        student.setPhone(request.getPhone());        // phone，对应列 phone
        // 注意：这里不拷贝 id，新增时 id 由数据库生成，编辑时 id 由路径参数单独 set
    }
}
