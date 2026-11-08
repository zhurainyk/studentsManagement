// 【给前端同学】和 Result.java 一样，属于「接口返回数据的形状定义」。
package com.example.students.common;

// import ≈ 前端的 import；List 相当于 JS 里的数组类型 Array
import java.util.List;

/**
 * 【这个文件 ≈ 分页表格组件需要的数据结构】
 *
 * StudentList.vue 里写的是：
 *   this.tableData = res.data.list      // 表格 :data 绑定的数组
 *   this.total     = res.data.total     // el-pagination :total 绑定的总条数
 * 所以后端返回的 data 必须长成 { list, total, page, pageSize }，字段名一个字母都不能差。
 *
 * 前端类比：≈ TS 的 interface PageData<T> { list: T[]; total: number; page: number; pageSize: number }
 *           ≈ src/mock/index.js 里 return 的 { list, total, page, pageSize }
 *
 * 注意它和 Result 的关系：Result 是「最外层信封」，PageResult 是「信封里装的那份分页数据」，
 * 组合起来就是 Result<PageResult<Student>>，最终 JSON：
 *   { "code":200, "message":"success", "data": { "list":[...], "total":10, "page":1, "pageSize":10 } }
 */
public class PageResult<T> {

    private List<T> list;   // 当前这一页的数据，≈ 前端表格的 tableData（T 这里实际是 Student）
    private long total;     // 符合条件的**总条数**（不是当前页条数！），分页组件靠它算总页数
                            // 用 long 而不是 int：COUNT(*) 结果理论上可能超过 21 亿
    private int page;       // 当前第几页，回传给前端便于核对
    private int pageSize;   // 每页几条，回传给前端便于核对

    // 无参构造器：框架（Jackson / 反射）需要，保留即可
    public PageResult() {
    }

    // 全参构造器：Service 里 new PageResult<>(list, total, page, pageSize) 一次装好 4 个值
    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    // ↓ 又是一组 getter / setter：Jackson 靠 getXxx() 把对象转成 JSON 的 key。
    //   例如 getTotal() → JSON 里的 "total"；StudentList.vue 的 res.data.total 就是这么来的。
    public List<T> getList() {              // 读当前页数组
        return list;
    }

    public void setList(List<T> list) {     // 写当前页数组
        this.list = list;
    }

    public long getTotal() {                // 读总条数
        return total;
    }

    public void setTotal(long total) {      // 写总条数
        this.total = total;
    }

    public int getPage() {                  // 读当前页
        return page;
    }

    public void setPage(int page) {         // 写当前页
        this.page = page;
    }

    public int getPageSize() {              // 读每页条数
        return pageSize;
    }

    public void setPageSize(int pageSize) { // 写每页条数
        this.pageSize = pageSize;
    }
}
