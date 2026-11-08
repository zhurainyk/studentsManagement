// 【给前端同学】common 包 = 各接口共用的公共类。
package com.example.students.common;

// ↓ 一堆 import ≈ 前端把要用到的工具/类型先 import 进来
import org.slf4j.Logger;                                                   // 日志工具，≈ console.error
import org.slf4j.LoggerFactory;                                            // 获取日志对象的工厂
import org.springframework.http.converter.HttpMessageNotReadableException;  // 「请求体读不懂」异常
import org.springframework.validation.FieldError;                          // 单个字段的校验错误
import org.springframework.web.bind.MethodArgumentNotValidException;       // @Valid 校验失败异常
import org.springframework.web.bind.annotation.ExceptionHandler;           // 「我处理哪种异常」注解
import org.springframework.web.bind.annotation.RestControllerAdvice;       // 全局异常切面注解
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException; // 参数类型不对异常

import java.util.stream.Collectors;   // Java 的流式工具，≈ 数组 .map().join()

/**
 * 【这个文件 ≈ axios 的「全局错误拦截」+ Vue 的全局 errorHandler，只不过长在后端】
 *
 * 它解决什么问题：如果没有它，Controller 里每个方法都得写 try/catch 把异常转成统一格式，4 个接口重复 4 遍。
 * @RestControllerAdvice 会让这个类「罩住所有 Controller」：任何一层（Controller / Service / Mapper）
 * 抛出的异常，只要冒到这里，就按类型挑一个 @ExceptionHandler 方法，转成 Result.fail(...) 返回给前端。
 *
 * 前端最终表现：
 *   · 表单乱填      → { code: 400, message: '姓名长度在2到20个字符；请输入正确的手机号', data: null }
 *   · 删不存在的 id → { code: 404, message: '学生不存在', data: null }
 *   · 服务器崩了    → { code: 500, message: '服务器内部错误，请查看后端日志', data: null }
 * 三种情况 HTTP 状态都还是 200，前端拦截器统一按 res.code !== 200 弹错误提示。
 *
 * 记忆方式：Service 负责「喊出错」，这里负责「把喊声翻译成前端认识的 JSON」。
 */
@RestControllerAdvice
// ↑ 全局切面：对所有 @RestController 生效。≈ 给所有路由挂了一个统一的 catch。
public class GlobalExceptionHandler {

    // 日志对象：只在「真·服务器错误」时打印堆栈到后端控制台，方便排查；不返回给前端
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** ① 业务异常：Service 主动 throw new BusinessException(404, "学生不存在") 会落到这里 */
    @ExceptionHandler(BusinessException.class)
    // ↑ 括号里写「我负责处理哪种异常」，Spring 按类型最匹配的原则挑选处理器
    public Result<Void> handleBusinessException(BusinessException e) {
        // Result<Void> 表示 data 恒为 null（Void ≈ 没有值），对应 JSON 里的 "data": null
        return Result.fail(e.getCode(), e.getMessage());   // 把异常里的 code 和提示语原样装进统一响应
    }

    /** ② 参数校验失败：@Valid 检查 StudentRequest 的注解没通过时，Spring 抛这个异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // e.getBindingResult().getFieldErrors() ≈ 前端表单 validate() 得到的 errors 数组，
        // 里面每一项是一个 FieldError（哪个字段、什么提示语，提示语来自 @NotBlank(message=...)）
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)   // ≈ errors.map(item => item.message)：只取提示文字
                .collect(Collectors.joining("；")); // ≈ .join('；')：多个错误拼成一句话给前端弹窗；分号结束这条赋值语句
        return Result.fail(Result.BAD_REQUEST, message);  // code 固定 400
    }

    /** ③ 请求体不是合法 JSON，例如 body 传了 'abc' 或字段引号没闭合 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadableException(HttpMessageNotReadableException e) {
        // e 参数保留但不使用：这里不需要细节，给前端一句固定提示即可
        return Result.fail(Result.BAD_REQUEST, "请求体格式不正确，请传入合法的 JSON");
    }

    /** ④ 路径参数类型不对，例如 DELETE /api/students/abc（id 声明的是 Long，传了字母） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return Result.fail(Result.BAD_REQUEST, "路径参数格式不正确");
    }

    /** ⑤ 兜底：上面 4 种都没接住的任何异常，都到这里（最宽泛的 Exception 放最后） */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("服务器内部错误", e);   // 把完整堆栈打在后端控制台，≈ console.error(err)
                                        // 注意：堆栈不返回给前端，避免暴露内部实现（安全）
        return Result.fail(Result.ERROR, "服务器内部错误，请查看后端日志");
    }
}
