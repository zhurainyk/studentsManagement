<template>
  <div class="student-list">
    <el-card class="box-card">
      <div slot="header" class="card-header">
        <span>学生管理系统</span>
        <el-button type="primary" @click="handleAdd">添加学生</el-button>
      </div>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学生姓名"
          clearable
          @input="handleSearch"
          style="width: 300px"
        >
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
      </div>
      
      <!-- 表格 -->
      <el-table
        ref="studentTable"
        :data="tableData"
        v-loading="loading"
        border
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="姓名" width="120"></el-table-column>
        <el-table-column prop="age" label="年龄" width="80"></el-table-column>
        <el-table-column prop="className" label="班级" width="150"></el-table-column>
        <el-table-column prop="phone" label="电话" width="150"></el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        class="pagination"
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
      >
      </el-pagination>
    </el-card>
    
    <!-- 学生表单弹窗 -->
    <StudentForm
      :visible.sync="formVisible"
      :student="currentStudent"
      @submit="handleSubmit"
    />
  </div>
</template>

<script>
import { getStudents, addStudent, updateStudent, deleteStudent } from '@/api/student'
import StudentForm from '@/components/StudentForm.vue'

export default {
  name: 'StudentList',
  components: {
    StudentForm
  },
  data() {
    return {
      tableData: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchKeyword: '',
      searchTimer: null,
      formVisible: false,
      currentStudent: null
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getStudents({
          page: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchKeyword
        })
        this.tableData = res.data.list || []
        this.total = res.data.total || 0
        this.$nextTick(() => {
          if (this.$refs.studentTable) {
            this.$refs.studentTable.doLayout()
          }
        })
      } catch (error) {
        this.$message.error('获取数据失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.currentPage = 1
        this.fetchData()
      }, 300)
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchData()
    },
    handleAdd() {
      this.currentStudent = null
      this.formVisible = true
    },
    handleEdit(row) {
      this.currentStudent = { ...row }
      this.formVisible = true
    },
    async handleSubmit(student) {
      try {
        if (student.id) {
          await updateStudent(student.id, student)
          this.$message.success('更新成功')
        } else {
          await addStudent(student)
          this.$message.success('添加成功')
        }
        this.formVisible = false
        this.fetchData()
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      }
    },
    handleDelete(row) {
      this.$confirm(`确定要删除学生"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteStudent(row.id)
          this.$message.success('删除成功')
          this.fetchData()
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.student-list {
  padding: 20px;
}

.box-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 20px;
  font-weight: bold;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
