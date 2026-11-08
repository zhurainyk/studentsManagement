// 【给前端同学】这个包 common 放的是「所有接口共用的东西」，≈ 前端 src/utils/ 下的公共工具。
package com.example.students.common;

/**
 * 【这个文件 ≈ axios 响应拦截器认识的那种「统一返回格式」】
 *
 * 前端 src/api/student.js 的响应拦截器写的是：
 *   const res = response.data
 *   if (res.code !== 200) return Promise.reject(new Error(res.message))
 *   return res
 * 也就是说，前端**强制要求**后端每个接口都返回 { code, message, data } 这个形状。
 * 这个类就是那个形状的「Java 版定义」，Jackson 会自动把它序列化成 JSON。
 *
 * 前端类比：≈ TS 里的 interface ApiResponse<T> { code: number; message: string; data: T }
 *           ≈ Mock.js 里 return { code: 200, message: 'success', data: ... } 的那个对象
 *
 * 【重要设计】业务失败（学生不存在 / 参数错误）时，HTTP 状态码仍然是 200，
 * 只在 body.code 里写 404 / 400。这是为了兼容原来 Mock.js 的约定，
 * 这样前端拦截器的 res.code !== 200 分支才能拿到 res.message 弹出错误提示。
 *
 * <T> 是泛型：T 表示「data 里装的东西类型不固定」，和 TypeScript 泛型一个意思。
 * 例如 Result<Student> → data 是一个学生；Result<PageResult<Student>> → data 是分页对象。
 */
public class Result<T> {

    // ↓ 4 个常量：把状态码收拢到一处，避免代码里到处写 200 / 404 这种「魔法数字」
    public static final int SUCCESS = 200;       // 成功，前端拦截器认这个值
    public static final int BAD_REQUEST = 400;   // 参数校验失败（对应前端表单 rules 没通过）
    public static final int NOT_FOUND = 404;     // 业务上的「学生不存在」，注意不是 HTTP 404
    public static final int ERROR = 500;         // 服务器内部错误（兜底用）

    // ↓ 3 个私有字段 = 最终 JSON 的 3 个 key。
    //   private ≈ 只有本类能直接访问，外部要通过下面的 get/set；
    //   Jackson 序列化时靠 getXxx() 读取它们，字段名就是 JSON 的 key（code / message / data）。
    private int code;        // 业务状态码，≈ res.code
    private String message;  // 提示文字，≈ res.message（成功时是 "success" 或 "添加成功"）
    private T data;          // 真正的数据，≈ res.data；泛型所以什么都能装

    // 无参构造器：Jackson 反序列化（JSON → 对象）时需要；本项目主要是「对象 → JSON」，用得少
    public Result() {
    }

    // 全参构造器：下面 3 个静态方法最终都调它
    public Result(int code, String message, T data) {
        this.code = code;        // this.code 指本对象的字段，右边的 code 指传进来的参数
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法：调用方写 Result.success(data) 就行，不用手动 new + set 三次。
    // 前端类比：≈ 封装一个 function ok(data) { return { code: 200, message: 'success', data } }
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS, "success", data);   // <> 是类型推断，Java 会根据参数自动猜出 T
    }

    // 重载版：可以自定义 message，比如「添加成功」「删除成功」，前端 $message.success 会用到
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS, message, data);
    }

    // 失败时 data 一律为 null：对应 JSON 里 "data": null
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    // ↓ 下面这一组 getter / setter ≈ 前端对象属性的「读 / 写」。
    //   Java 习惯把字段设成 private，再暴露 public 的 get/set；
    //   Jackson（对象 → JSON 的那个库）就是靠调用 getCode() / getMessage() / getData() 拿到值，
    //   并按方法名推出 JSON 的 key 分别是 code / message / data。
    //   MyBatis、Spring 等框架也都依赖这套约定，所以别删。
    public int getCode() {                 // 读 code
        return code;
    }

    public void setCode(int code) {        // 写 code
        this.code = code;
    }

    public String getMessage() {           // 读 message
        return message;
    }

    public void setMessage(String message) { // 写 message
        this.message = message;
    }

    public T getData() {                   // 读 data（泛型，可能是一个学生，也可能是分页对象）
        return data;
    }

    public void setData(T data) {          // 写 data
        this.data = data;
    }
}
