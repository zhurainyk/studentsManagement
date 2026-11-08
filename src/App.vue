<template>
  <div id="app">
    <StudentList />
  </div>
</template>

<script>
import StudentList from './views/StudentList.vue'

export default {
  name: 'App',
  components: {
    StudentList
  }
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f5f7fa;
  min-height: 100vh;
}
</style>
