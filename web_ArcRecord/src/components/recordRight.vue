<template>
  <div class="dialog-container" ref="containerRef">
    <!-- 空状态：未选择角色 -->
    <div v-if="!selectedRole" class="empty-tip">
      请从左侧选择英雄查看CG配置
    </div>

    <!-- 加载状态：请求数据时显示 -->
    <div v-else-if="loading" class="loading-tip">
      <el-icon><Loading /></el-icon>
      <span>加载{{ selectedRole.heroId }}的CG数据中...</span>  <!-- 使用对象的ID -->
    </div>

    <!-- 右侧单列布局：可滚动 + 隐藏滚动条 + 垂直居中 + 缩小右侧间距 -->
    <div 
      v-else 
      class="scroll-wrapper"
      ref="scrollWrapperRef"
      @mousedown="handleMouseDown"
      @mouseup="handleMouseUp"
      @mouseleave="handleMouseUp"
      @mousemove="handleMouseMove"
    >
      <!-- 竖列容器：固定在屏幕右侧 + 可滚动 + 居中 -->
      <div class="vertical-list" v-show="dialogList.length > 0">
        <div 
          v-for="(dialog, index) in dialogList" 
          :key="dialog.id || `hero-${selectedRole}-item-${index}`"
          class="dialog-item"
        >
          <!-- 缩略图：等比例放大32% → 182px -->
          <div 
            class="thumbnail-container"
            @click="() => handleThumbnailClick(dialog)"
            :class="{ loading: activeLoadingId === dialog.prefab }"
          >
            <img 
              :src="getImageUrl(dialog.prefabIcon)" 
              :alt="`${dialog.prefab}-缩略图`"
              class="thumbnail-img"
              :class="{ 'no-png-border': !dialog.prefab.includes('.png') }" 
              @error="(e) => handleImageErrorWithRetry(e, getImageUrl(dialog.prefabIcon), 'thumbnail', dialog.prefab)"
              draggable="false"
            />
            <div v-if="activeLoadingId === dialog.prefab" class="loading-mask">
              <el-icon size="16"><Loading /></el-icon>
            </div>
          </div>

          <!-- 路径信息：显示PREFAB名称 -->
          <div class="dialog-content">
            <div class="path-text">{{ dialog.prefab }}</div>
          </div>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div v-show="dialogList.length === 0" class="empty-data-tip">
        {{ selectedRole.id }}暂无CG配置数据
      </div>
    </div>

    <!-- 全屏大图预览层：置顶显示 -->
    <div 
      v-show="isFullScreenShow" 
      class="fullscreen-mask"
      @click="closeFullScreenImage"
    >
      <div class="fullscreen-content" @click.stop>
        <!-- 大图加载中状态 -->
        <div v-if="isFullImgLoading" class="fullscreen-loading">
          <el-icon size="40"><Loading /></el-icon>
          <span>加载大图中...</span>
        </div>
        <!-- 大图显示：修复error事件参数错误 -->
        <img 
          v-else
          :src="fullScreenImageUrl" 
          :alt="`${currentPrefab}-大图`"
          class="fullscreen-img"
          @error="(e) => handleImageErrorWithRetry(e, fullScreenImageUrl, 'full', currentPrefab)"
          draggable="false"
        />
        <el-button  class="close-btn" @click="closeFullScreenImage">
          <template #icon><Close /></template>
        </el-button>
      </div>

    </div>
    
    <!-- CG序列预览层 -->
    <div 
      v-show="isCGPreviewShow" 
      class="fullscreen-mask"
      @click="closeCGPreview"
    >
      <div class="fullscreen-content" @click.stop>
        <div v-if="isFullImgLoading" class="fullscreen-loading">
          <el-icon size="40"><Loading /></el-icon>
          <span>加载序列图片中...</span>
        </div>
        <div v-else-if="cgImages.length">
          <!-- 图片导航信息：仅保留第几张/总张数 -->
          <div class="cg-nav-info">
            <span>{{ currentCGIndex + 1 }} / {{ cgImages.length }}</span>
          </div>
          
          <!-- 图片显示 -->
          <img 
            :src="cgImages[currentCGIndex].url" 
            :alt="cgImages[currentCGIndex].key"
            class="fullscreen-img"
            draggable="false"
          />
          
          <!-- 切换按钮：已修复样式定位 -->
          <el-button 
            class="cg-nav-btn left"
            @click="() => currentCGIndex = (currentCGIndex - 1 + cgImages.length) % cgImages.length"
          >
            <template #icon>
              <ArrowLeft />
            </template>
          </el-button>
          <el-button 
            class="cg-nav-btn right"
            @click="() => currentCGIndex = (currentCGIndex + 1) % cgImages.length"
          >
            <template #icon>
              <ArrowRight />
            </template>
          </el-button>
        </div>
        <!-- 关闭按钮（完整闭合） -->
        <el-button  class="close-btn" @click="closeCGPreview" >
          <template #icon>
            <Close />
          </template>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'; // 新增 onUnmounted 导入
import { ElMessage, ElIcon, ElButton } from 'element-plus';

import { getRoleCollectionByHeroId, getImageUrl, getCGImages} from '@/api/recordRight';
import { ArrowLeft, ArrowRight, Loading, Close } from '@element-plus/icons-vue';
import { getRoleSpine } from '@/api/spineAnimation';

const containerRef = ref(null);
const scrollWrapperRef = ref(null);

// 鼠标拖动滚动相关状态
const isDragging = ref(false);
const startY = ref(0);
const scrollTop = ref(0);

// 大图预览相关状态
const isFullScreenShow = ref(false);
const fullScreenImageUrl = ref('');
const isFullImgLoading = ref(false);
const activeLoadingId = ref('');
const currentPrefab = ref('');

// 新增多图预览状态
const cgImages = ref([]);          // CG序列图片列表
const currentCGIndex = ref(0);     // 当前显示的CG索引
const isCGPreviewShow = ref(false); // 是否显示CG预览

// 数据相关状态
const dialogList = ref([]);
const loading = ref(false);

// Props 接收父组件传递的选中英雄ID
const props = defineProps({
  selectedRole: {  // 修改为接收完整对象
    type: Object,
    default: null
  }
});

//spine资源存储
const spineResources = ref({}); // 存储 atlas, png, skel

// 监听英雄ID变化，自动请求数据
watch(
  () => props.selectedRole,  // 监听对象变化
  async (newRole) => {
    // 重置状态
    dialogList.value = [];
    isFullScreenShow.value = false;
    
    if (!newRole || !newRole.id) return;  // 校验对象及ID

    try {
      loading.value = true;
      // 调用接口获取数据（使用对象的heroId）
      const data = await getRoleCollectionByHeroId(newRole.id);
      dialogList.value = data;

      // 2. 批量获取每个配置项的Spine资源（如果有spineObject字段）
      const spineData = await getRoleSpine(newRole.spineObject);
      spineResources.value = spineData;
      console.log(spineData);
    } catch (error) {
      console.error('获取CG数据失败:', error);
      dialogList.value = [];
    } finally {
      loading.value = false;
    }
  },
  { immediate: true }
);


// 鼠标拖动滚动逻辑
const handleMouseDown = (e) => {
  if (e.button !== 0) return;
  isDragging.value = true;
  startY.value = e.clientY;
  scrollTop.value = scrollWrapperRef.value.scrollTop;
  scrollWrapperRef.value.style.cursor = 'grabbing';
  document.body.style.userSelect = 'none';
};

const handleMouseUp = () => {
  if (!isDragging.value) return;
  isDragging.value = false;
  scrollWrapperRef.value.style.cursor = 'default';
  document.body.style.userSelect = 'auto';
};

const handleMouseMove = (e) => {
  if (!isDragging.value) return;
  const dy = e.clientY - startY.value;
  scrollWrapperRef.value.scrollTop = scrollTop.value - dy;
};
// 自定义事件（传递选中的heroId）
const emit = defineEmits(['roleSelect']);
// 点击缩略图预览大图：增加prefab含png的判断
const handleThumbnailClick = async (item) => {
  console.log('点击缩略图，数据：', item);
  
  if (!item || !item.prefab) {
    ElMessage.warning('图片信息缺失');
    return;
  }

  const { prefab } = item;
  currentPrefab.value = prefab;
  
  // 记录当前缩略图元素，避免关闭后状态异常
  const thumbnailImg = document.querySelector(`img[alt="${prefab}-缩略图"]`);
  if (thumbnailImg) {
    thumbnailImg.dataset.active = 'true'; // 标记当前活跃的缩略图
  }

  // 情况1: prefab含png - 单张大图预览（使用你声明的变量名）
  if (prefab.includes('.png')) {
    isFullImgLoading.value = true; // 开启加载状态
    activeLoadingId.value = prefab; // 标记当前加载的图片ID

    try {
      // 调用getImageUrl获取大图URL
      const imgUrl = await getImageUrl(prefab);
      // 校验URL有效性，且确保是当前点击的图片（避免异步请求错乱）
      if (!imgUrl || activeLoadingId.value !== prefab) {
        ElMessage.error('大图URL获取失败或已切换图片');
        isFullImgLoading.value = false;
        return;
      }

      // 赋值到你声明的fullScreenImageUrl变量
      fullScreenImageUrl.value = imgUrl;
      isFullImgLoading.value = false;
      // 打开单图预览层（使用你声明的isFullScreenShow变量）
      isFullScreenShow.value = true;
      console.log('单图预览触发，URL：', fullScreenImageUrl.value);
    } catch (error) {
      console.error('获取大图失败：', error);
      ElMessage.error('加载大图失败，请重试');
      isFullImgLoading.value = false;
    }
    return;
  }

  // 情况2: roleMotions含CG - 多图预览（逻辑不变，变量名已匹配）
  if (item.roleMotions.includes('CG')) {
    if (!item.roleMotions.length) {
      ElMessage.info('未解析到有效CG序列');
      return;
    }

    isFullImgLoading.value = true;
    const images = await getCGImages(item.roleMotions,item.prefab);
    isFullImgLoading.value = false;

    if (images.length) {
      cgImages.value = images;
      currentCGIndex.value = 0;
      isCGPreviewShow.value = true; // 打开CG多图预览层
      // 监听键盘事件（方向键切换）
      document.addEventListener('keydown', handleKeydown);
    } else {
      ElMessage.warning('未获取到CG序列图片');
    }
    return;
  } else {
      
     let role = { spineObject: null };
     role.spineObject = item.prefab;
     emit('roleSelect', role); // 触发事件，传给父组件
     return;
  }

  // 情况3: 其他情况 - 留空处理
  ElMessage.info('该资源暂不支持预览');
};

// 补充关闭单图预览的函数（匹配你的变量名）
const closeFullScreen = () => {
  isFullScreenShow.value = false;
  fullScreenImageUrl.value = '';
  currentPrefab.value = '';
};

// 补充键盘事件处理函数（方向键切换CG图片）
const handleKeydown = (e) => {
  if (!isCGPreviewShow.value) return;

  // 阻止默认行为，避免页面滚动
  e.preventDefault();

  // 左键：上一张
  if (e.key === 'ArrowLeft') {
    currentCGIndex.value = (currentCGIndex.value - 1 + cgImages.value.length) % cgImages.value.length;
  }
  // 右键：下一张
  if (e.key === 'ArrowRight') {
    currentCGIndex.value = (currentCGIndex.value + 1) % cgImages.value.length;
  }
  // ESC键：关闭预览
  if (e.key === 'Escape') {
    closeCGPreview();
  }
};

// 组件卸载时清理事件监听（避免内存泄漏）
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});

// 图片加载失败重试：只对含png的prefab生效
// 修改图片加载失败重试逻辑
const handleImageErrorWithRetry = (e, originalUrl, type, prefab) => {
  const imgElement = e.target;
  
  // 避免重复处理
  if (imgElement.dataset.errorHandled) return;
  imgElement.dataset.errorHandled = 'true';

  // 非png格式不重试大图
  if (type === 'full' && !prefab.includes('.png')) {
    // 显示默认错误图（可选）
    imgElement.src = '/error-placeholder.png'; // 需准备一张占位图
    return;
  }

  // 构建重试路径（修复路径拼接错误）
  let retryUrl = '';
  // 检查原始URL是否已包含基础路径，避免重复拼接
  if (originalUrl.startsWith(IMAGE_BASE_PATH)) {
    const relativePath = originalUrl.replace(IMAGE_BASE_PATH, '');
    const lastSlashIndex = relativePath.lastIndexOf('/');
    if (lastSlashIndex !== -1) {
      const dir = relativePath.substring(0, lastSlashIndex + 1);
      const fileName = relativePath.substring(lastSlashIndex + 1);
      retryUrl = IMAGE_BASE_PATH + dir + 'Img/' + fileName;
    } else {
      retryUrl = IMAGE_BASE_PATH + 'Img/' + relativePath;
    }
  } else {
    // 处理相对路径
    const lastSlashIndex = originalUrl.lastIndexOf('/');
    if (lastSlashIndex !== -1) {
      const dir = originalUrl.substring(0, lastSlashIndex + 1);
      const fileName = originalUrl.substring(lastSlashIndex + 1);
      retryUrl = dir + 'Img/' + fileName;
    } else {
      retryUrl = `Img/${originalUrl}`;
    }
  }

  // 重试失败后显示占位图
  const handleRetryError = () => {
    imgElement.src = '/thumbnail-placeholder.png'; // 缩略图占位图
    imgElement.removeEventListener('error', handleRetryError);
  };

  imgElement.addEventListener('error', handleRetryError);
  imgElement.src = retryUrl;
  console.log(`[图片加载重试] ${type} | 原始: ${originalUrl} → 重试: ${retryUrl}`);
};

// 监听英雄ID变化重置预览状态
watch(
  () => props.selectedHeroId,
  () => {
    isFullScreenShow.value = false;
    fullScreenImageUrl.value = '';
    isFullImgLoading.value = false;
    activeLoadingId.value = '';
    currentPrefab.value = '';
  }
);
// 修改 src/components/recordRight.vue 中的关闭方法
const closeFullScreenImage = () => {
  // 关键：重置加载状态
  isFullImgLoading.value = false;
  activeLoadingId.value = '';
  
  // 清理图片元素
  const fullImg = document.querySelector('.fullscreen-img');
  if (fullImg) {
    delete fullImg.dataset.retry;
    fullImg.src = ''; // 清空大图资源
  }
  
  // 重置预览状态
  isFullScreenShow.value = false;
  fullScreenImageUrl.value = '';
  currentPrefab.value = '';
};

// 同步修复 CG 预览关闭方法
const closeCGPreview = () => {
  isFullImgLoading.value = false; // 重置加载状态
  const cgImgs = document.querySelectorAll('.fullscreen-img');
  cgImgs.forEach(img => {
    delete img.dataset.retry;
    img.src = '';
  });
  
  isCGPreviewShow.value = false;
  cgImages.value = [];
  currentCGIndex.value = 0;
  document.removeEventListener('keydown', handleKeydown);
};
</script>
  
  <style scoped>

  
  .empty-tip, .loading-tip, .empty-data-tip {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #666;
    font-size: 16px;
    gap: 12px;
  }
  
  .loading-tip { color: #409eff; }
  
  .scroll-wrapper {
    position: absolute;
    top: 50%;
    right: 5px;
    transform: translateY(-50%);
    width: 182px;
    max-height: calc(100vh - 40px);
    padding: 10px 0;
    box-sizing: border-box;
    overflow-y: auto;
    overflow-x: hidden;
    -ms-overflow-style: none;
    scrollbar-width: none;
    z-index: 10;
    cursor: default;
  }
  
  .scroll-wrapper::-webkit-scrollbar { display: none; }
  
  .vertical-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
    width: 182px;
  }
  
  .dialog-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    transition: all 0.3s ease;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }
  
  .thumbnail-container {
    position: relative;
    cursor: pointer;
    width: 100%;
    -webkit-user-drag: none;
    user-drag: none;
  }
  
  .thumbnail-container.loading .thumbnail-img {
    opacity: 0.7;
    filter: blur(2px);
  }
  
  /* 保留原有.thumbnail-img样式，仅新增上面的类 */
  .thumbnail-img {
    width: 100%;
    height: auto;
    object-fit: contain;
    background-color: #f8f8f8;
    border: 2px solid #ffffff; /* 原有默认白色边框 */
    border-radius: 10px;
    box-shadow: 0 4px 13px rgba(0, 0, 0, 0.15);
    transition: box-shadow 0.3s ease;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    -webkit-user-drag: none;
    user-drag: none;
    pointer-events: auto;
  }
  .thumbnail-img.no-png-border {
  border-color: #409eff; /* Element Plus 主题蓝色，可根据需求调整 */
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.5); /* 可选：增加蓝色外发光效果，增强视觉 */
  }
  /* 新增：图片加载失败时的样式优化 */
  .thumbnail-img[src=""] {
    background-color: #f0f0f0;
    border: 2px dashed #ccc;
  }
  
  .thumbnail-img:hover { box-shadow: 0 6.5px 20px rgba(0, 0, 0, 0.2); }
  
  .loading-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 2;
    border-radius: 10px;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }
  
  .dialog-content {
    width: 100%;
    -webkit-user-drag: none;
    -moz-user-drag: none;
    -ms-user-drag: none;
    user-drag: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }
  
  .path-text {
    font-size: 11px;
    color: #555;
    word-break: break-all;
    line-height: 1.2;
    text-align: center;
  }
  
  .fullscreen-mask {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.9);
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }
  
  .fullscreen-content {
    position: relative;
    max-width: 90%;
    max-height: 90%;
    cursor: default;
  }
  
  .fullscreen-loading {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    gap: 16px;
    height: 200px;
  }
  
  /* 新增：全屏大图加载失败样式 */
  .fullscreen-img[src=""] {
    width: 400px;
    height: 300px;
    background-color: #333;
    border: 2px dashed #666;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
    font-size: 14px;
    content: "图片加载失败";
  }
  
.close-btn {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center;     /* 垂直居中 */
  transition: all 0.3s ease;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  font-size: 16px;        /* 字体大小 */
  top: 20px !important;
  right: 20px !important;
  width: 48px !important;
  height: 48px !important;
  padding: 0 !important;
  margin: 0 !important;  /* 清除默认内边距 */
  text-align: center;     /* 文本本身水平居中 */
  line-height: 1;         /* 重置行高 */
  /* 新增以下样式强制清除内部元素间距 */
}
.close-btn:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(1.1);
}

  /* CG预览相关样式 */
.cg-nav-info {
  position: absolute;
  top: -30px;
  left: 0;
  color: #fff;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.key-info {
  font-family: monospace;
}

.cg-nav-btn {
  position: absolute;
  top: 50%;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border: none;
  transform: translateY(-50%);
  z-index: 10;
}

.cg-nav-btn.left {
  left: -20px;
}

.cg-nav-btn.right {
  right: -20px;
}

.cg-nav-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

/* 导航信息样式：保持原有基础样式 */
.cg-nav-info {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  padding: 5px 10px;
  border-radius: 4px;
  text-align: center;
  width: auto;
  margin: 0;
  /* 提高层级，避免被按钮遮挡 */
  z-index: 10;
}

/* 【关键1】强制显示按钮和图标，解决图标隐藏问题 */
:deep(.cg-nav-btn) {
  position: absolute !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
  background: rgba(0, 0, 0, 0.5) !important;
  color: #fff !important;
  border: none !important;
  width: 40px !important;
  height: 40px !important;
  border-radius: 50% !important;
  z-index: 9999 !important; /* 提升层级，确保不被遮挡 */
  /* 强制显示图标，避免Element Plus默认样式隐藏 */
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

/* 左箭头按钮位置 */
:deep(.cg-nav-btn.left) {
  left: 20px !important;
  width: 60px !important;  /* 原40px → 放大50%至60px */
  height: 60px !important; /* 原40px → 放大50%至60px */
}

/* 右箭头按钮位置 */
:deep(.cg-nav-btn.right) {
  right: 20px !important;
  width: 60px !important;  /* 原40px → 放大50%至60px */
  height: 60px !important; /* 原40px → 放大50%至60px */
}

/* 【关键2】强制显示箭头图标，修复图标大小/颜色 */
:deep(.cg-nav-btn .el-icon) {
  font-size: 20px !important; /* 图标大小 */
  color: #fff !important;     /* 图标颜色 */
  display: block !important;  /* 强制显示 */
}

/* 按钮悬浮效果 */
:deep(.cg-nav-btn:hover) {
  background: rgba(0, 0, 0, 0.7) !important;
  color: #fff !important;
}

/* 确保容器相对定位，按钮绝对定位生效 */
.fullscreen-content {
  position: relative !important;
  max-width: 90%;
  max-height: 90%;
}
/* 全屏容器片容器 */
.fullscreen-content {
  position: relative !important;
  width: 100vw !important;  /* 占用全屏宽度 */
  height: 100vh !important; /* 占满全屏高度 */
  max-width: none !important; /* 移除最大宽度限制 */
  max-height: none !important; /* 移除最大高度限制 */
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}

/* 全屏图片样式 - 关键修改 */
.fullscreen-img {
  width: 100% !important;   /* 宽度适应屏幕 */
  height: 100% !important;  /* 高度适应屏幕 */
  object-fit: contain;      /* 保持比例完整显示 */
  max-width: none !important;
  max-height: none !important;
  border: none !important;  /* 可选：移除边框 */
  border-radius: 0 !important; /* 可选：移除圆角 */
}

/* 调整轮播导航按钮位置 */
:deep(.cg-nav-btn.left) {
  left: 20px !important;
}

:deep(.cg-nav-btn.right) {
  right: 20px !important;
}

/* 调整导航信息位置 */
.cg-nav-info {
  top: 20px !important;
  left: 50% !important;
  transform: translateX(-50%) !important;
}

  </style>