// 【给前端同学】dto 包 = 接口「入参」的数据形状，这里对应新增/编辑表单提交的数据。
package com.example.students.dto;

// Jackson 的注解：控制 JSON → 对象时遇到「不认识的字段」怎么办
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ↓ 下面这些是 Bean Validation（参数校验）注解，≈ 前端 Element UI 表单的 rules
import javax.validation.constraints.Max;        // 最大值
import javax.validation.constraints.Min;        // 最小值
import javax.validation.constraints.NotBlank;   // 不能为 null / 空串 / 纯空格
import javax.validation.constraints.NotNull;    // 不能为 null
import javax.validation.constraints.Pattern;    // 正则匹配
import javax.validation.constraints.Size;       // 字符串长度范围

/**
 * 【这个文件 ≈ StudentForm.vue 里 this.form 的「后端版定义」+ 表单 rules 的后端版】
 *
 * 前端提交：{"name":"张三","age":20,"className":"计科1班","phone":"13800138000"}
 * @RequestBody 把这段 JSON 转成本类对象，类上的每个字段对应一个 key。
 *
 * 为什么前端已经校验了、后端还要再校验一遍？
 *   前端 rules 只是为了「即时提示、体验好」，别人可以用 curl / Postman 绕过页面直接打接口。
 *   后端注解是真正的**安全边界**，规则和 StudentForm.vue 的 rules 一条条对齐。
 *
 * 为什么这个类里没有 id 字段？
 *   编辑时前端会把 { ...form, id } 一起提交，但后端只认 URL 路径 /api/students/{id} 上的 id，
 *   防止请求体里的 id 和路径不一致导致改错数据。多出来的字段靠下面的注解静默忽略。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// ↑ 遇到 JSON 里多出来的、本类没有的字段（比如 id）就忽略，不要报「无法识别的字段」错误。
//   ≈ 前端解构时只取自己要的字段，其余丢掉；没它的话编辑接口会直接 400。
public class StudentRequest {

    // ── 姓名：对应表单「姓名」输入框，规则与 rules.name 一致 ──
    @NotBlank(message = "请输入姓名")                              // 必填，≈ { required: true, message: '请输入姓名' }
    @Size(min = 2, max = 20, message = "姓名长度在2到20个字符")    // 长度 2~20，≈ { min: 2, max: 20, ... }
    private String name;                                           // message 里的文字会原样出现在 code 400 的响应里

    // ── 年龄：对应 el-input-number ──
    @NotNull(message = "请输入年龄")        // Integer 可能为 null，先判空
    @Min(value = 1, message = "年龄最小为1")   // ≈ el-input-number :min="1"
    @Max(value = 150, message = "年龄最大为150") // ≈ el-input-number :max="150"
    private Integer age;                     // 用 Integer 而非 int：能区分「没传」和「传了 0」

    // ── 班级：对应「班级」输入框 ──
    @NotBlank(message = "请输入班级")        // 必填即可，没有额外格式要求
    private String className;                // 注意：这里驼峰 className；数据库列叫 class_name，靠配置自动映射

    // ── 电话：对应「电话」输入框 ──
    @NotBlank(message = "请输入电话")                              // 必填
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号") // 正则 ≈ /^1[3-9]\d{9}$/
                                                                 // Java 字符串里 \d 要写成 \\d（多一个转义）
    private String phone;

    // ↓ 下面这组 getter/setter 是框架读写字段的通道：
    //   · Jackson（@RequestBody）用 setXxx() 把 JSON 的值写进来
    //   · MyBatis / Service 用 getXxx() 把值取出去（如 #{name} 调 getName()）
    //   ≈ 前端直接 this.form.name，Java 只是走了一层方法。别删，删了字段就取不到值。
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

    public String getClassName() {                   // 读班级
        return className;
    }

    public void setClassName(String className) {     // 写班级
        this.className = className;
    }

    public String getPhone() {                       // 读电话
        return phone;
    }

    public void setPhone(String phone) {             // 写电话
        this.phone = phone;
    }
}
