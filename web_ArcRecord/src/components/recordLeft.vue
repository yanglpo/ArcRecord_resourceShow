<template>
  <!-- 核心：通过父组件传递的 collapsed 状态控制显示/隐藏 + 宽度动画 -->
  <div 
    class="image-container"
    :class="{ collapsed: collapsed }"
    :style="{ width: collapsed ? '0' : '305px' }"
  >
    <!-- 滚动条容器：折叠时隐藏 -->
    <el-scrollbar 
      class="custom-scrollbar"
      style="height: 100%; width: 100%;"
      v-show="!collapsed"
    >
      <div class="image-group">
        <div 
          v-for="(role, index) in roles" 
          :key="role.heroId || index"
          :class="['row', { active: selectedHeroId === role.heroId }]"
          @click="handleRoleClick(role)" 
        >
          <img 
            :src="getImageUrl(role.headL)" 
            alt="角色头像" 
            class="square-image" 
            v-if="role.headL"
            @error="handleImageError"
          />
          <div class="rectangle-image-container" v-if="role.headM">
            <div class="role-name">{{ role.realName }}</div>
            <img 
              :src="getImageUrl(role.headM)" 
              alt="角色背景图" 
              class="rectangle-image-content"
              @error="handleImageError"
            />
          </div>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { fetchRoles, getImageUrl, handleImageError } from '@/api/recordLeft';

// 新增：接收父组件传递的折叠状态
const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false // 默认不折叠
  }
});

// 原有响应式变量
const roles = ref([]);
const selectedHeroId = ref('');

// 自定义事件（传递选中的heroId）
const emit = defineEmits(['roleSelect']);

// 点击角色事件
const handleRoleClick = (role) => {
  console.log('点击的英雄数据:', role);
  if (!role || !role.id) {
    console.warn('英雄数据或ID为空，无法触发选中');
    return;
  }
  selectedHeroId.value = role.id; // 仍保持ID的选中状态
  emit('roleSelect', role); // 传递整个角色对象
};

// 挂载时请求数据
onMounted(async () => {
  roles.value = await fetchRoles();
});

// 暴露方法/变量
defineExpose({
  roles,
  getImageUrl,
  handleImageError,
  selectedHeroId,
  handleRoleClick
});
</script>

<style scoped>
/* 左侧容器：新增过渡动画 + 折叠状态样式 */
.image-container {
  height: 100vh;
  background-color: #f5f7fa;
  border-right: 1px solid #e5e7eb;
  padding: 4.68px 0 4.68px 4.68px;
  box-sizing: border-box;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
  /* 核心：折叠/展开过渡动画 */
  transition: width 0.3s ease, padding 0.3s ease, border 0.3s ease;
}

.no-select-drag {
  -webkit-user-select: none;  /* Chrome/Safari/Edge */
  -moz-user-select: none;     /* Firefox */
  -ms-user-select: none;      /* IE/Edge */
  user-select: none;          /* 标准属性：禁止元素选中 */
  -webkit-user-drag: none;    /* Chrome/Safari：禁止拖拽 */
  user-drag: none;            /* 标准属性：禁止拖拽 */
  pointer-events: none;       /* 可选：禁止图片响应鼠标事件（仅图片层，不影响行点击） */
  cursor: default;            /* 鼠标移到图片上显示默认样式，而非拖拽/选中样式 */
}

/* 折叠状态：宽度为0 + 隐藏边框/内边距 */
.image-container.collapsed {
  width: 0 !important; /* 强制宽度为0（修正注释格式错误） */
  padding: 0;          /* 清空内边距，避免折叠后有残留空白 */
  border-right: none;  /* 隐藏右侧边框，消除残留线条 */
  visibility: hidden;  /* 彻底隐藏容器，避免点击穿透/占位 */
  opacity: 0;          /* 透明度过渡，动画更流畅 */
  flex-shrink: 0;      /* 强制禁止收缩，避免宽度计算异常 */
}

/* 原有样式保留 */
.custom-scrollbar {
  width: calc(100% + 6px);
  height: 100%;
}

.image-group {
  display: flex;
  flex-direction: column;
  width: calc(100% - 10px);
  box-sizing: border-box;
}

.row {
  display: flex;
  align-items: center;
  margin-bottom: 1px;
  padding: 0 2px;
  background: #ffffff;
  width: 100%;
  box-sizing: border-box;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.row.active {
  background-color: #e8f4ff;
  border: 1px solid #409eff;
  margin-bottom: 0;
}

.square-image {
  width: 54px;
  height: 60px;
  object-fit: cover;
  margin-right: 6px;
  flex-shrink: 0;
  border-radius: 4px 0 0 4px;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  -webkit-user-drag: none;
  user-drag: none;
  pointer-events: none;
  cursor: default;
}
.row {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
.rectangle-image-container {
  width: 230px;
  height: 60px;
  flex-shrink: 0;
  background-image: url('@/images/tabHead.png');
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0 4px 4px 0;
  position: relative;
}

.role-name {
  position: absolute;
  top: 6px;
  left: 12px;
  font-size: 15px;
  color: #ffffff;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
  z-index: 10;
}

.rectangle-image-content {
  width: 75%;
  height: 100%;
  object-fit: cover;
  border-radius: 0 4px 4px 0;
  margin-left: 25px;
  z-index: 1;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  -webkit-user-drag: none;
  user-drag: none;
  pointer-events: none;
  cursor: default;
}

/* 滚动条样式 */
:deep(.custom-scrollbar .el-scrollbar__wrap) {
  height: 100% !important;
  overflow-x: hidden;
  padding-right: 0 !important;
}

:deep(.custom-scrollbar .el-scrollbar__bar) {
  right: -4px !important;
  width: 6px;
}

:deep(.custom-scrollbar .el-scrollbar__bar.is-vertical) {
  right: -6px !important;
  width: 15px !important;
  top: 2px;
  bottom: 2px;
  background-color: #f0f0f0;
  border-radius: 5px;
}

:deep(.custom-scrollbar .el-scrollbar__thumb) {
  background-color: #999 !important;
  border-radius: 5px !important;
  width: 100% !important;
  min-height: 15px !important;
}

:deep(.custom-scrollbar .el-scrollbar__thumb:hover) {
  background-color: #666 !important;
}

:deep(.el-scrollbar__view) {
  width: 100%;
}
</style>