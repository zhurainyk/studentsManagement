import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// 响应拦截器
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    return Promise.reject(error)
  }
)

// 获取学生列表
export const getStudents = (params) => {
  return api.get('/students', { params })
}

// 添加学生
export const addStudent = (data) => {
  return api.post('/students', data)
}

// 更新学生
export const updateStudent = (id, data) => {
  return api.put(`/students/${id}`, data)
}

// 删除学生
export const deleteStudent = (id) => {
  return api.delete(`/students/${id}`)
}
