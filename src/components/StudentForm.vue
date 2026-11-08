<template>
  <el-dialog
    :title="isEdit ? '编辑学生' : '添加学生'"
    :visible.sync="dialogVisible"
    width="500px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <el-form
      ref="studentForm"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
      </el-form-item>
      
      <el-form-item label="年龄" prop="age">
        <el-input-number
          v-model="form.age"
          :min="1"
          :max="150"
          style="width: 100%"
        ></el-input-number>
      </el-form-item>
      
      <el-form-item label="班级" prop="className">
        <el-input v-model="form.className" placeholder="请输入班级"></el-input>
      </el-form-item>
      
      <el-form-item label="电话" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入电话"></el-input>
      </el-form-item>
    </el-form>
    
    <div slot="footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'StudentForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    student: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      form: {
        name: '',
        age: 20,
        className: '',
        phone: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
        ],
        age: [
          { required: true, message: '请输入年龄', trigger: 'blur' }
        ],
        className: [
          { required: true, message: '请输入班级', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    isEdit() {
      return this.student && this.student.id
    }
  },
  watch: {
    visible(val) {
      if (val) {
        // 弹窗打开时，填充数据
        if (this.student) {
          this.form = {
            name: this.student.name || '',
            age: this.student.age || 20,
            className: this.student.className || '',
            phone: this.student.phone || ''
          }
        } else {
          this.resetFormData()
        }
      }
    }
  },
  methods: {
    handleOpened() {
      // 弹窗打开动画完成后，清除验证状态
      this.$nextTick(() => {
        if (this.$refs.studentForm) {
          this.$refs.studentForm.clearValidate()
        }
      })
    },
    handleClosed() {
      // 弹窗关闭后，重置表单
      this.resetFormData()
    },
    resetFormData() {
      this.form = {
        name: '',
        age: 20,
        className: '',
        phone: ''
      }
    },
    handleSubmit() {
      if (!this.$refs.studentForm) return
      
      this.$refs.studentForm.validate(valid => {
        if (valid) {
          const data = { ...this.form }
          if (this.student && this.student.id) {
            data.id = this.student.id
          }
          this.$emit('submit', data)
        }
      })
    },
    handleCancel() {
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped>
</style>
