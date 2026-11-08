// 【给前端同学】entity 包 = 「实体类」，和数据库表一一对应。
package com.example.students.entity;

/**
 * 【这个文件 ≈ 表格里的一行数据，也就是 src/mock/data.js 里的一条 student 对象】
 *
 * 前端数据模型：{ id, name, age, className, phone }
 * 数据库表：students(id, name, age, class_name, phone)
 * 这个类就是「数据库的一行」在 Java 里的样子，同时也是返回给前端的 JSON 的样子。
 *
 * 它同时干两份活：
 *   ① MyBatis 查完数据库，把每一「行」转成这个对象（结果映射）
 *   ② Jackson 把这个对象序列化成 JSON 返回给前端（响应体）
 *
 * 最容易踩的坑：数据库列叫 class_name（下划线），这里叫 className（驼峰），
 * 靠 application.yml 的 map-underscore-to-camel-case: true 自动对上；
 * 前端 el-table-column 的 prop="className" 也是跟着这个名字来的。
 *
 * 与 DTO 的区别：StudentRequest（入参）故意没有 id，这个类（出参/库表映射）必须有 id。
 */
public class Student {

    // Long 是「可为 null 的整数」：新增前还没插入数据库，id 是 null，
    // 插入后由 useGeneratedKeys 回填。若写成 long 基本类型就表达不了「还没有 id」。
    private Long id;            // 学号，数据库自增主键 ≈ 前端 row.id，编辑/删除时拼在 URL 上
    private String name;        // 姓名 → 列 name
    private Integer age;        // 年龄 → 列 age
    private String className;   // 班级 → 列 class_name（驼峰映射）
    private String phone;       // 电话 → 列 phone

    // ↓ 又是 getter / setter（Java POJO 的固定写法）。
    //   MyBatis 给对象赋值时调 setXxx()（如 setClassName(...)）；
    //   Jackson 把对象转 JSON 时调 getXxx()，并按方法名生成 key：
    //     getId() → "id"，getClassName() → "className"。
    //   所以前端 res.data.list[0].className 能取到值，全靠 getClassName() 这个名字。
    public Long getId() {                            // 读 id
        return id;
    }

    public void setId(Long id) {                     // 写 id（数据库回填主键 / 编辑时手动 set）
        this.id = id;
    }

    public String getName() {                        // 读姓名
        return name;
    }

    public void setName(String name) {               // 写姓名
        this.name = name;
    }

    public Integer getAge() {                        // 读年龄
        return age;
    }

    public void setAge(Integer age) {                // 写年龄
        this.age = age;
    }

    public String getClassName() {                   // 读班级 → JSON key "className"
        return className;
    }

    public void setClassName(String className) {     // 写班级（MyBatis 把 class_name 列的值塞进来）
        this.className = className;
    }

    public String getPhone() {                       // 读电话
        return phone;
    }

    public void setPhone(String phone) {             // 写电话
        this.phone = phone;
    }
}
