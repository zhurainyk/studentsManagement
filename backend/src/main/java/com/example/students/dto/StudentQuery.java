// 【给前端同学】dto 包 = Data Transfer Object，「层与层之间传数据用的对象」，
// ≈ 前端调用 api 时传的 params 对象的类型定义：{ page, pageSize, keyword }。
package com.example.students.dto;

/**
 * 【这个文件 ≈ getStudents(params) 里那个 params 的「后端版形状」】
 *
 * 对应请求：GET /api/students?page=1&pageSize=10&keyword=张三
 * Spring 会按名字把 URL 上的 3 个参数自动填进下面 3 个同名字段（叫「参数绑定」）。
 *
 * 前端类比：≈ TS 的 interface Query { page?: number; pageSize?: number; keyword?: string }
 *           ≈ src/mock/index.js 里 url.searchParams.get('page') 取出来的那 3 个值
 *
 * 特别提醒：offset 字段**不是**前端传的，是 StudentServiceImpl 算好后回写进来的，
 * 再交给 StudentMapper.xml 的 LIMIT ... OFFSET ... 使用。
 * 所以这个对象会被当成「数据袋」在 Controller → Service → Mapper 之间一路传下去。
 */
public class StudentQuery {

    // Integer 是「可为 null 的 int」：这样后端能区分「前端没传 page」和「传了 page=0」，
    // 便于 Service 里做兜底。前端类比：JS 里 undefined 和 0 是两回事。
    private Integer page = 1;        // 当前页码，默认第 1 页（≈ data() 里 currentPage: 1）
    private Integer pageSize = 10;   // 每页条数，默认 10（≈ data() 里 pageSize: 10）
    private String keyword;          // 搜索关键词（姓名模糊匹配），不传就是 null = 不筛选
    private int offset;              // 派生字段：(page-1)*pageSize，由 Service 计算，XML 里当 OFFSET 用
                                     // 用 int 是因为它一定会被算出来，不会是 null

    // ↓ 下面全是 getter / setter。
    //   Spring 参数绑定时调用 setPage(...) 把 URL 参数写进来；
    //   Service 调用 getPage() 读、setOffset(...) 写；
    //   MyBatis 的 #{pageSize} / #{offset} / #{keyword} 通过 getXxx() 取值。
    //   ≈ 前端直接读写对象属性，Java 只是多了一层方法而已。
    public Integer getPage() {                    // 读 page
        return page;
    }

    public void setPage(Integer page) {           // 写 page（Spring 绑定 URL 参数时调它）
        this.page = page;
    }

    public Integer getPageSize() {                // 读 pageSize
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {   // 写 pageSize
        this.pageSize = pageSize;
    }

    public String getKeyword() {                  // 读 keyword
        return keyword;
    }

    public void setKeyword(String keyword) {      // 写 keyword
        this.keyword = keyword;
    }

    public int getOffset() {                      // 读 offset（MyBatis 的 #{offset} 从这里取）
        return offset;
    }

    public void setOffset(int offset) {           // 写 offset（Service 算完后回写）
        this.offset = offset;
    }
}
