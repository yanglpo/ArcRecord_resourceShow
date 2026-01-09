// src/utils/recordLeft.js
// 🌟 确保这行在文件最顶部，路径指向正确的request.js
import request from './request'; // 相对路径：和record.js同目录的request.js

const IMAGE_BASE_PATH = import.meta.env.VITE_API_BASE_URL;

export const fetchRoles = async () => {
  try {
    const data = await request.get('/role/getRole'); // 现在request已定义


    // 新增：判断是否是HTML内容
    if (typeof data === 'string' && data.startsWith('<!DOCTYPE html>')) {
      console.error('请求被前端Vite拦截，未转发到后端！请检查Vite代理配置');
      return [];
    }

    let roleList = [];
    if (Array.isArray(data)) {
      roleList = data;
    } else if (data && Array.isArray(data.data)) {
      roleList = data.data;
    } else {
      console.warn('后端返回数据不是数组，格式:', data);
      roleList = [];
    }
    return roleList.map(role => role);
  } catch (error) {
    console.error('获取角色数据失败:', error);
    return [];
  }
};

/**
 * 拼接图片URL（相对路径，自动适配环境）
 * @param {string} filename 图片文件名
 * @returns {string} 完整图片URL
 */
export const getImageUrl = (filename) => {
  if (!filename) return '';
  // 保持与左侧相同的路径拼接规则
  const fullPath = `${IMAGE_BASE_PATH}/images/${filename}`;
  return encodeURI(fullPath);
};

/**
 * 图片加载失败处理
 * @param {Event} e 错误事件
 */
export const handleImageError = (e) => {
  if (e.target.dataset.errorHandled) return;
  e.target.dataset.errorHandled = true;
  console.warn('图片加载失败:', e.target.src);
};
