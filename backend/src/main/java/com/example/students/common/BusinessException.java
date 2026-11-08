// 【给前端同学】common 包 = 各接口共用的公共类。
package com.example.students.common;

/**
 * 【这个文件 ≈ 一个「带错误码的自定义 Error」】
 *
 * 前端类比：≈ Promise.reject({ code: 404, message: '学生不存在' })
 *           ≈ throw new Error('学生不存在')，但额外带了一个数字 code
 *
 * 为什么需要它：Service（业务层）只负责说「出问题了」，它不该知道 HTTP、JSON 长什么样。
 * 所以业务层「抛异常」，由 GlobalExceptionHandler 统一接住并转成 { code, message, data }。
 * 前端最终看到的就是拦截器里那个 res.code !== 200 的分支。
 *
 * 出现场景：删除/编辑一个不存在的 id 时，StudentServiceImpl 会 throw 这个异常。
 */
// extends RuntimeException = 继承 Java 的「运行时异常」，
// 好处是方法签名上不用声明 throws，异常可以直接往上冒泡，直到被全局处理器接住。
public class BusinessException extends RuntimeException {

    private final int code;  // 业务错误码（404 / 400...），final ≈ 「赋值后不可再改」，类似 const
                             // 它和 message 分开存：message 给用户看，code 给前端拦截器判断

    // 构造器：new BusinessException(404, "学生不存在")
    public BusinessException(int code, String message) {
        super(message);      // super(...) = 调用父类构造器，把提示文字交给 RuntimeException 保存，
                             // 后面 getMessage() 才拿得到（≈ super(props)）
        this.code = code;    // 把错误码存到自己的字段里
    }

    // 供 GlobalExceptionHandler 读取：e.getCode() → Result.fail(code, e.getMessage())
    public int getCode() {
        return code;
    }
}
