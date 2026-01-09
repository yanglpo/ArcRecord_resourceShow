// src/utils/request.js
import axios from 'axios';

// 读取完整的后端地址（带协议头），trim() 去除可能的空格
const baseURL = (import.meta.env.VITE_API_BASE_URL || '').trim();

const request = axios.create({
  baseURL: baseURL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 请求拦截器：验证最终请求地址（确保没有 localhost:5173）
request.interceptors.request.use(
  (config) => {
    console.log('最终请求地址：', config.baseURL + config.url); 
    return config;
  },
  (error) => {
    console.error('请求错误：', error);
    return Promise.reject(error);
  }
);

// 响应拦截器：统一处理返回值
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('响应错误：', error.message);
    return Promise.reject(error);
  }
);

export default request;