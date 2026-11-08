import Mock from 'mockjs'
import studentData from './data'

// 配置Mock请求延迟
Mock.setup({
  timeout: '200-600'
})

// 获取学生列表
Mock.mock(/\/api\/students(\?.*)?$/, 'get', (options) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page')) || 1
  const pageSize = parseInt(url.searchParams.get('pageSize')) || 10
  const keyword = url.searchParams.get('keyword') || ''
  
  let filtered = studentData.students
  
  // 关键词搜索
  if (keyword) {
    filtered = filtered.filter(s => s.name.includes(keyword))
  }
  
  // 分页
  const start = (page - 1) * pageSize
  const end = start + pageSize
  const list = filtered.slice(start, end)
  
  return {
    code: 200,
    message: 'success',
    data: {
      list,
      total: filtered.length,
      page,
      pageSize
    }
  }
})

// 添加学生
Mock.mock(/\/api\/students$/, 'post', (options) => {
  const student = JSON.parse(options.body)
  const maxId = Math.max(...studentData.students.map(s => s.id))
  student.id = maxId + 1
  studentData.students.push(student)
  
  return {
    code: 200,
    message: '添加成功',
    data: student
  }
})

// 更新学生
Mock.mock(/\/api\/students\/\d+$/, 'put', (options) => {
  const id = parseInt(options.url.match(/\/api\/students\/(\d+)/)[1])
  const student = JSON.parse(options.body)
  const index = studentData.students.findIndex(s => s.id === id)
  
  if (index !== -1) {
    studentData.students[index] = { ...studentData.students[index], ...student }
    return {
      code: 200,
      message: '更新成功',
      data: studentData.students[index]
    }
  } else {
    return {
      code: 404,
      message: '学生不存在',
      data: null
    }
  }
})

// 删除学生
Mock.mock(/\/api\/students\/\d+$/, 'delete', (options) => {
  const id = parseInt(options.url.match(/\/api\/students\/(\d+)/)[1])
  const index = studentData.students.findIndex(s => s.id === id)
  
  if (index !== -1) {
    studentData.students.splice(index, 1)
    return {
      code: 200,
      message: '删除成功',
      data: null
    }
  } else {
    return {
      code: 404,
      message: '学生不存在',
      data: null
    }
  }
})
