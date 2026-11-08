import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App.vue'
// 对接 Java 后端时关闭 Mock；只需纯前端演示时，放开下面这一行即可
// import './mock'

Vue.use(ElementUI)

new Vue({
  render: h => h(App)
}).$mount('#app')
