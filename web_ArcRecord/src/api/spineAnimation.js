import request from '@/api/request'; // 复用项目统一的axios实例
import { ElMessage } from 'element-plus';


const IMAGE_BASE_PATH = import.meta.env.VITE_API_BASE_URL;

// 获取角色Spine资源的HTTP访问路径
export const getRoleSpine = async (spineObject) => {
  // 前置校验：空值/非对象/非字符串直接返回
  if (!spineObject || (typeof spineObject !== 'object' && typeof spineObject !== 'string')) {
    console.error('【Spine接口】spineObject不能为空，且必须是对象或字符串');
    ElMessage?.warning('缺少有效的Spine配置参数');
    return { atlas: '', png: [], skel: '' };
  }

  try {
    // 处理入参：如果是对象则转为JSON字符串（适配后端要求）
    const requestData = {
      spineObject: typeof spineObject === 'object' ? JSON.stringify(spineObject) : spineObject
    };
    // 调用后端/getRoleSpine接口，设置15秒超时（资源请求可适当延长）
    const response = await request.post('/spine/getRoleSpine', requestData, {
      timeout: 15000
    });

    // 关键修正：axios的响应数据在response.data里，原代码直接取response会报错
    const rawResult = response || {};
    
    const result = {
      atlas: rawResult.atlas,
      png: Array.isArray(rawResult.png) ? rawResult.png : [],
      skel: rawResult.skel
    };
    // 拼接后端/getSingleSpineFile接口，生成可直接访问的HTTP路径
    const baseApi = '/spine/getSingleSpineFile';
    // 处理atlas路径：直接传完整的atlas路径作为参数
    if (result.atlas) {
      result.atlas = `${baseApi}?spinePath=${result.atlas}`;
    }
    // 处理png路径数组：直接传每个完整的png路径作为参数
    result.png = result.png.map(pngPath => {
      if (pngPath) {
        return `${baseApi}?spinePath=${pngPath}`;
      }
      return '';
    }).filter(Boolean); // 过滤空值
    // 处理skel路径：直接传完整的skel路径作为参数
    if (result.skel) {
      result.skel = `${baseApi}?spinePath=${result.skel}`;
    }

    console.log('【Spine接口】拼接后的HTTP路径:', result);
    return result;
    
  } catch (error) {
    console.error('【Spine接口】请求失败:', error);
    // 更友好的错误提示：区分网络错误/接口返回错误
    const errorMsg = error.response?.data?.msg || error.message || '获取Spine资源路径失败';
    ElMessage?.error(errorMsg);
    return { atlas: '', png: [], skel: '' };
  }
};