import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    open: true,
    cors: true,
    proxy: {
      // 🔥 新增：转发/spine开头的请求到后端8080
      '/spine': {
        target: 'http://localhost:8080',
        changeOrigin: true, // 关键：模拟后端请求源，避免跨域/后端鉴权问题
        secure: false, // 非HTTPS环境关闭
        logLevel: 'debug', // 可选：开启日志，控制台可查看代理匹配情况
        rewrite: (path) => path, // 保留/spine前缀（后端接口路径是/spine/xxx）
      },
      // 原有代理规则保留
      '/role': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
        rewrite: (path) => path,
      },
      '/images': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        secure: false
      }
    }
  }
});