<template>
  <div class="page-container">
    <!-- 左侧列表 -->
    <div class="left-panel">
      <recordLeft 
        @roleSelect="handleRoleSelect"
        :collapsed="!isLeftPanelOpen" 
      />
      <div 
        class="toggle-button" 
        @click="toggleLeftPanel"
        :title="isLeftPanelOpen ? '收起左侧列表' : '展开左侧列表'"
      >
        <el-icon :size="18">
          <ArrowLeft v-if="isLeftPanelOpen" />
          <ArrowRight v-else />
        </el-icon>
      </div>
    </div>
    <div class="spine-center">
    <SpineAnimation :selectedRole="selectedRole"/>
  </div>
    <!-- 右侧区域：只传递选中的hero -->
    <div class="right-area">
      <recordRight :selectedRole="selectedRole"
      @roleSelect="handleRoleSelectFromRight"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElIcon } from 'element-plus';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import recordLeft from '@/components/recordLeft.vue';
import recordRight from '@/components/recordRight.vue';
import SpineAnimation from '@/components/SpineAnimation.vue';
// 左侧面板状态
const isLeftPanelOpen = ref(true);
// 仅存储选中的英雄ID
const selectedRole = ref(null);

// 角色选中事件：role给右侧组件
const handleRoleSelect = (role) => {
  selectedRole.value = role; // 仍可通过role.id获取ID
  // 现在可以使用整个role对象的其他属性了
  console.log('选中的英雄完整数据:', role);
};
const handleRoleSelectFromRight = (role) => {
  selectedRole.value = role;
  console.log('右侧更新的英雄数据:', role);
};

// 切换左侧面板
const toggleLeftPanel = () => {
  isLeftPanelOpen.value = !isLeftPanelOpen.value;
};
</script>

<!-- 样式不变，保留原有样式 -->
<style scoped>
.spine-center {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: auto;
  z-index: 1;
  
}

/* 原有样式全部保留 */
:global(body) {
  margin: 0;
  padding: 0;
  overflow: hidden;
  background-color: #f5f7fa;
}

.page-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  box-sizing: border-box;
  background-image: url('@/images/bg.png');
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center center;
  background-attachment: fixed;
}

.left-panel {
  position: relative;
  flex-shrink: 0;
  z-index: 2;
}

.toggle-button {
  position: absolute;
  top: 50%;
  right: -15px;
  transform: translateY(-50%);
  width: 30px;
  height: 60px;
  background-color: #fff;
  border-radius: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  transition: background-color 0.2s;
}

.toggle-button:hover {
  background-color: #e8f4ff;
  color: #409eff;
}

.right-area {
  height: 100vh;
  position: fixed;
  right: 0;
  width: 190px;
  z-index: 2;
}
</style>