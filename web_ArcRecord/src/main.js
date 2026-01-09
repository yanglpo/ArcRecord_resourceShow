import { createApp } from 'vue';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

const app = createApp(App);

// 全局引入ElMessage
app.config.globalProperties.ElMessage = ElementPlus.ElMessage;

app.use(ElementPlus).mount('#app');