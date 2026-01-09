// src/api/roleConfig.js（建议重命名文件，语义更清晰）
import request from '@/api/request'; // 复用已封装的axios实例
import { ElMessage } from 'element-plus'; // 补充ElMessage导入（关键修复）

const IMAGE_BASE_PATH = import.meta.env.VITE_API_BASE_URL;

/**
 * 根据英雄ID获取角色配置列表（修正方法名，匹配功能）
 * @param {number|string} heroId 英雄/角色ID
 * @returns {Promise<Array>} 角色配置列表（兜底返回空数组）
 */
export const getRoleCollectionByHeroId = async (heroId) => {
  // 1. 前置校验：空值/无效值直接返回
  if (!heroId || heroId.toString().trim() === '') {
    console.error('【角色配置接口】英雄ID不能为空或空白');
    ElMessage?.warning('请选择有效的角色ID');
    return [];
  }

  try {
    // 2. 接口请求（添加超时兜底，避免无限等待）
    const response = await request.get('/role/getRoleCollectionByHeroId', {
      params: { heroId: heroId.toString().trim() }, // 去空格，避免无效参数
      timeout: 10000 // 10秒超时
    });

    console.log('【角色配置接口】返回数据:', response);

    // 3. 数据格式校验：非数组则兜底
    if (Array.isArray(response)) {
      // 可选：过滤无效数据（如prefabIcon/prefab为空的项）
      return response.filter(item => item && item.id);
    } else {
      console.warn('【角色配置接口】数据格式异常，期望数组:', response);
      ElMessage?.warning('角色配置数据格式异常，请刷新重试');
      return [];
    }
  } catch (error) {
    // 4. 异常分类处理：网络错误/接口错误/其他
    console.error('【角色配置接口】请求失败:', error);
    const errorMsg = error.response?.data?.msg 
      || error.message 
      || '获取角色配置失败，请稍后重试';
    ElMessage?.error(errorMsg);
    return [];
  }
};


/**
 * 拼接图片URL（与左侧recordLeft.js保持一致的逻辑）
 * @param {string} filename 图片文件名或路径
 * @returns {string} 完整图片URL
 */
export const getImageUrl = (filename) => {
  if (!filename) return '';
  // 保持与左侧相同的路径拼接规则
  const fullPath = `${IMAGE_BASE_PATH}/images/${filename}`;
  return encodeURI(fullPath);
};

// 在原有基础上补充以下内容
/**
 * 批量获取CG序列图片
 * @param {string[]} roleMotions -（如 CG_1~CG_2~CG_3）
 * @returns {Promise<{key: string, url: string}[]>} 图片列表
 */
export const getCGImages = async (roleMotions,prefab) => {
  if (!roleMotions || !roleMotions.length) {
    console.error('CG键数组不能为空');
    return [];
  }

  try {
    const response = await request.get('/role/getCGImages', {
      params: {
        roleMotions: roleMotions ,
        prefab: prefab            // 拼接为逗号分隔字符串
      },
      timeout: 15000
    });

    if (Array.isArray(response)) {
      return response.map(item => ({
        ...item,
        url: `${IMAGE_BASE_PATH}/images/${item.url}` // 拼接完整URL
      }));
    }
    return [];
  } catch (error) {
    console.error('获取CG序列图片失败:', error);
    ElMessage.error('加载序列图片失败');
    return [];
  }
};
