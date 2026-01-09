<template>
  <div class="spine-container">
    <!-- 全屏画布 -->
    <div id="spine-player" class="spine-player-wrap"></div>

    <!-- 按钮区域（绝对定位，不会被画布覆盖） -->
    <div class="ui-controls">
      <div class="btn-group">
        <h4>皮肤选择</h4>
        <button
          v-for="skin in skinList"
          :key="skin"
          @click="switchSkin(skin)"
          :disabled="!isPlayerReady"
          :class="{ active: currentSkin === skin }"
        >
          {{ skin }}
        </button>
      </div>

      <div class="btn-group">
        <h4>动画选择</h4>
        <button
          v-for="anim in playableAnims"
          :key="anim"
          @click="switchAnimation(anim)"
          :disabled="!isPlayerReady"
          :class="{ active: currentAnimation === anim }"
        >
          {{ anim }}
        </button>
      </div>

      <!-- <div class="btn-group">
        <h4>视角控制</h4>
        <button @click="resetCamera" :disabled="!isPlayerReady">重置视角</button>
      </div> -->
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch, defineProps } from 'vue'
import { SpinePlayer, OrthoCamera, CameraController, Vector2, Color } from '@esotericsoftware/spine-player'
import { getRoleSpine } from '@/api/spineAnimation';
import { ElMessage } from 'element-plus'

const props = defineProps({
  selectedRole: {
    type: Object,
    default: null
  },
  initPosition: {
    type: Object,
    default: () => ({ x: 0, y: 0, scale: 1, rotation: 0 })
  },
  canvasSize: {
    type: Object,
    default: () => ({ width: window.innerWidth, height: window.innerHeight })
  },
  characterViewport: {
    type: Object,
    default: () => ({
      x: -1462,
      y: -900,
      width: 3600,
      height: 5500
    })
  }
});

const skinList = ref([]);
const playableAnims = ref([]);
const currentSkin = ref('');
const currentAnimation = ref('');
const isPlayerReady = ref(false);
// 新增：记录上一个播放的动画（用于触摸动画结束后恢复）
const lastPlayedAnimation = ref('');
let player = null;
let animationLoop = null;
let lastTime = 0;

let manualCamera = null;
let cameraController = null;
let defaultCameraPos = new Vector2();
let defaultCameraZoom = 1;
// 新增：基准缩放值（用于限定3倍缩放范围）
let baseZoomValue = 1;

// 新增：用于存储鼠标按下时的状态
let isDragging = false;
let lastMouseX = 0;
let lastMouseY = 0;

// 安全获取 skeleton
const getSafeSkeleton = () => {
  if (!player || player.disposed || !player.skeleton) return null;
  return player.skeleton;
};

// 安全获取 animationState
const getSafeAnimationState = () => {
  if (!player || player.disposed || !player.animationState) return null;
  return player.animationState;
};

const setCharacterViewport = () => {
  if (!player || player.disposed) return;
  const { x, y, width, height } = props.characterViewport;
  player.viewport = { x, y, width, height };
  player.viewportTransitionStart = 0;
};

// 新增：清理相机事件
const cleanCameraResources = () => {
  const canvas = document.querySelector('#spine-player canvas');
  if (!canvas) return;

  // 移除事件监听
  canvas.removeEventListener('mousedown', onMouseDown);
  canvas.removeEventListener('mousemove', onMouseMove);
  canvas.removeEventListener('mouseup', onMouseUp);
  canvas.removeEventListener('mouseleave', onMouseUp);
  canvas.removeEventListener('wheel', onMouseWheel);
  // 移除点击事件
  canvas.removeEventListener('click', onCanvasClick);

  // 重置变量
  isDragging = false;
  manualCamera = null;
  cameraController = null;
  defaultCameraPos = new Vector2();
  defaultCameraZoom = 1;
  baseZoomValue = 1; // 重置基准缩放值
};

// 核心修复：鼠标坐标转换与移动逻辑
const initCamera = () => {
  const skeleton = getSafeSkeleton();
  if (!player || player.disposed || !player.sceneRenderer || !skeleton) return;

  cleanCameraResources(); // 先清理旧事件

  const { width, height } = props.canvasSize;
  
  // 1. 创建相机
  manualCamera = new OrthoCamera(width, height);
  
  // 2. 初始化相机位置（基于视口中心）
  const { x: vpX, y: vpY, width: vpWidth, height: vpHeight } = props.characterViewport;
  manualCamera.position.x = vpX + vpWidth / 2;
  manualCamera.position.y = vpY + vpHeight / 2;
  
  // 3. 初始化缩放（重点修改：直接设置大的初始缩放值，确保人物显示足够大）
  // 【核心修改1】增大初始化缩放值，数值越大人物显示越大（建议从5开始测试，可调整）
  baseZoomValue = 7; // 基准缩放值（初始化大小）
  manualCamera.zoom = baseZoomValue; // 初始化缩放 = 基准值
  
  // 保存默认状态
  defaultCameraPos = new Vector2(manualCamera.position.x, manualCamera.position.y);
  defaultCameraZoom = manualCamera.zoom;

  const canvas = document.querySelector('#spine-player canvas');
  if (!canvas) return;

  // 4. 绑定鼠标事件
  canvas.addEventListener('mousedown', onMouseDown);
  canvas.addEventListener('mousemove', onMouseMove);
  canvas.addEventListener('mouseup', onMouseUp);
  canvas.addEventListener('mouseleave', onMouseUp);
  canvas.addEventListener('wheel', onMouseWheel);
  // 新增：绑定点击事件
  canvas.addEventListener('click', onCanvasClick);

  syncCameraToPlayer();
};

// 新增：Canvas点击事件 - 触发触摸动画
const onCanvasClick = (e) => {
  if (!isPlayerReady.value || isDragging) return; // 拖拽时不触发点击
  
  const animationState = getSafeAnimationState();
  const skeleton = getSafeSkeleton();
  if (!animationState || !skeleton) return;

  // 保存当前播放的动画（用于后续恢复）
  lastPlayedAnimation.value = currentAnimation.value;
  
  try {
        // 关键修复：用let声明，允许后续重新赋值
    let touchAnim = skeleton.data.findAnimation('04_Touch');
    // 04_Touch找不到时，查找01_Touch
    if (!touchAnim) {
      touchAnim = skeleton.data.findAnimation('01_Touch');
    }
    
    // 两种动画都找不到的容错
    if (!touchAnim) {
     
      return;
    }

    // 获取实际找到的动画名称（04_Touch/01_Touch）
    const touchAnimName = touchAnim.name;
    
    // 清除轨道并播放找到的动画
    animationState.clearTracks();
    const trackEntry = animationState.setAnimation(0, touchAnimName, false);

    console.log(`成功播放触摸动画：${touchAnimName}`);
    
    // 监听动画完成事件
    trackEntry.listener = {
      complete: () => {
        // 动画播放完成后，恢复到之前的动画
        if (lastPlayedAnimation.value) {
          switchAnimation(lastPlayedAnimation.value);
        } else {
          // 回退到默认Idle动画
          switchAnimation('00_Idle');
        }
      }
    };
    
    animationState.update(0);
    animationState.apply(skeleton);
    skeleton.updateWorldTransform();
    
  } catch (e) {
    ElMessage.error(`播放触摸动画失败：${e.message}`);
  }
};

// 鼠标按下
const onMouseDown = (e) => {
  if (e.button !== 0) return; // 只监听左键
  isDragging = true;
  // 获取初始鼠标位置（未转换的）
  lastMouseX = e.clientX;
  lastMouseY = e.clientY;
  const canvas = document.querySelector('#spine-player canvas');
  if (canvas) canvas.style.cursor = 'grabbing';
};

// 鼠标移动（核心修复点）
const onMouseMove = (e) => {
  if (!isDragging || !manualCamera) return;

  const canvas = document.querySelector('#spine-player canvas');
  if (!canvas) return;

  // 1. 获取鼠标偏移量
  const deltaX = e.clientX - lastMouseX;
  const deltaY = e.clientY - lastMouseY;

  // 2. 更新上一次鼠标位置
  lastMouseX = e.clientX;
  lastMouseY = e.clientY;

  // 3. 关键：获取 Canvas 的 CSS 尺寸和实际像素尺寸的比例
  const rect = canvas.getBoundingClientRect(); // CSS 显示区域
  const scaleX = canvas.width / rect.width;   // 水平缩放比例
  const scaleY = canvas.height / rect.height; // 垂直缩放比例

  // 4. 应用偏移量
  manualCamera.position.x -= deltaX * scaleX * manualCamera.zoom;
  manualCamera.position.y -= deltaY * scaleY * manualCamera.zoom * -1;

  syncCameraToPlayer();
};

// 鼠标松开
const onMouseUp = () => {
  isDragging = false;
  const canvas = document.querySelector('#spine-player canvas');
  if (canvas) canvas.style.cursor = 'grab';
};

// 鼠标滚轮缩放（核心修改：基于基准值的3倍范围）
const onMouseWheel = (e) => {
  e.preventDefault();
  if (!manualCamera || !baseZoomValue) return;

  const canvas = document.querySelector('#spine-player canvas');
  if (!canvas) return;

  const rect = canvas.getBoundingClientRect();
  // 获取鼠标在 Canvas 内的相对坐标 (0-1)
  const x = (e.clientX - rect.left) / rect.width;
  const y = (e.clientY - rect.top) / rect.height;

  // 缩放因子
  const zoomFactor = e.deltaY > 0 ? 1.1 : 0.9;
  
  // 记录缩放前的鼠标世界坐标
  const worldXBefore = manualCamera.position.x - (x - 0.5) * manualCamera.viewportWidth / manualCamera.zoom;
  const worldYBefore = manualCamera.position.y - (y - 0.5) * manualCamera.viewportHeight / manualCamera.zoom;

  // 应用缩放
  manualCamera.zoom *= zoomFactor;
  
  // 【核心修改2】限定缩放范围：基准值的1/3 ~ 基准值的3倍
  const minZoom = baseZoomValue / 3; // 最小：初始化大小的1/3
  const maxZoom = baseZoomValue * 3; // 最大：初始化大小的3倍
  manualCamera.zoom = Math.max(minZoom, Math.min(manualCamera.zoom, maxZoom));

  // 调整相机位置，使鼠标指向的世界坐标保持不变
  manualCamera.position.x = worldXBefore + (x - 0.5) * manualCamera.viewportWidth / manualCamera.zoom;
  manualCamera.position.y = worldYBefore + (y - 0.5) * manualCamera.viewportHeight / manualCamera.zoom;

  syncCameraToPlayer();
};

const syncCameraToPlayer = () => {
  if (!player || player.disposed || !manualCamera || !player.sceneRenderer) return;
  const renderCamera = player.sceneRenderer.camera;
  renderCamera.position.x = manualCamera.position.x;
  renderCamera.position.y = manualCamera.position.y;
  renderCamera.zoom = manualCamera.zoom;
  renderCamera.update();
};

const resetCamera = () => {
  if (!manualCamera) return;
  manualCamera.position.x = defaultCameraPos.x;
  manualCamera.position.y = defaultCameraPos.y;
  manualCamera.zoom = defaultCameraZoom;
  syncCameraToPlayer();
  ElMessage.success('视角已重置');
};

const resizeCanvas = () => {
  if (!player || player.disposed) return;
  const canvas = document.querySelector('#spine-player canvas');
  if (!canvas) return;

  const { width, height } = props.canvasSize;
  canvas.width = width;
  canvas.height = height;
  canvas.style.width = `${width}px`;
  canvas.style.height = `${height}px`;

  if (player.renderer?.resize) {
    player.renderer.resize(width, height);
  }

  setCharacterViewport();

  if (manualCamera) {
    manualCamera.viewportWidth = width;
    manualCamera.viewportHeight = height;
    manualCamera.update();
    syncCameraToPlayer();
  }
};

const setInitialPosition = () => {
  const skeleton = getSafeSkeleton();
  if (!skeleton) return;

  const { x, y, scale, rotation } = props.initPosition;
  skeleton.setToSetupPose();

  const rootBone = skeleton.findBone('root');
  if (rootBone) {
    rootBone.x = x;
    rootBone.y = y;
    rootBone.scaleX = scale;
    rootBone.scaleY = scale;
    rootBone.rotation = rotation;
  } else {
    skeleton.x = x;
    skeleton.y = y;
    skeleton.scaleX = scale;
    skeleton.scaleY = scale;
    skeleton.rotation = rotation;
  }

  skeleton.updateWorldTransform();
  setCharacterViewport();
  if (manualCamera) syncCameraToPlayer();
};

const initPlayer = async (spineObject) => {
  if (!spineObject) return;

  if (animationLoop) cancelAnimationFrame(animationLoop);
  cleanCameraResources();

  if (player && !player.disposed) {
    try { player.dispose(); } catch (e) { console.warn(e); }
    player = null;
  }

  isPlayerReady.value = false;
  skinList.value = [];
  playableAnims.value = [];

  const container = document.getElementById('spine-player');
  if (container) container.innerHTML = '';

  try {
    const spineFiles = await getRoleSpine(spineObject);
    if (!spineFiles || !spineFiles.atlas || !spineFiles.skel) {
      ElMessage.warning('未找到该角色的Spine资源文件');
      return;
    }

    player = new SpinePlayer('spine-player', {
      skelUrl: spineFiles.skel,
      atlasUrl: spineFiles.atlas,
      binary: true,
      loadBinary: true,
      renderer: 'webgl',
      premultipliedAlpha: true,
      alpha: true,
      preferWebGL: true,
      showControls: false,
      autoUpdate: true,
      autoRender: true,
      clearBeforeRender: true,
      cors: 'use-credentials',
      antialias: true,
      resolution: window.devicePixelRatio || 1,
      width: props.canvasSize.width,
      height: props.canvasSize.height,
      viewport: props.characterViewport,
      imageLoader: {
        load: (path, callback) => {
          if (!spineFiles.png) { callback(null); return; }
          const pngUrl = spineFiles.png.find(p => p.includes(path)) || path;
          const img = new Image();
          img.crossOrigin = 'use-credentials';
          img.onload = () => callback(img);
          img.onerror = () => { ElMessage.error(`贴图加载失败：${path}`); callback(null); };
          img.src = pngUrl;
        }
      },
      update: () => {
        if (!player || player.disposed) return;
        if (manualCamera) syncCameraToPlayer();
      },
      success: () => {
        setTimeout(() => {
          const skeleton = getSafeSkeleton();
          const animationState = getSafeAnimationState();
          if (!skeleton || !animationState) {
            ElMessage.error('Skeleton 初始化失败');
            isPlayerReady.value = false;
            return;
          }

          try {
            skinList.value = skeleton.data.skins.map(s => s.name).filter(s => s !== 'default' && !s.includes('_M'));
            playableAnims.value = skeleton.data.animations.filter(a => a && a.duration > 0).map(a => a.name);
          } catch (e) { console.warn('获取列表失败：', e); }

          currentSkin.value = skinList.value[0] || '';
          currentAnimation.value = playableAnims.value[0] || '00_Idle';
          // 初始化时记录默认动画
          lastPlayedAnimation.value = currentAnimation.value;

          setInitialPosition();
          resizeCanvas();
          initCamera(); // 初始化自定义相机

          isPlayerReady.value = true;
          startAnimationLoop();

          setTimeout(() => {
            if (currentSkin.value) switchSkin(currentSkin.value);
            if (currentAnimation.value) switchAnimation(currentAnimation.value);
          }, 200);
        }, 300);
      },
      error: (err) => {
        ElMessage.error(`加载失败：${err.message}`);
        isPlayerReady.value = false;
      }
    });
  } catch (e) {
    ElMessage.error(`初始化失败：${e.message}`);
    isPlayerReady.value = false;
  }
};

const startAnimationLoop = () => {
  if (animationLoop) cancelAnimationFrame(animationLoop);

  const animate = (timestamp) => {
    if (!player || player.disposed) return;

    const animationState = getSafeAnimationState();
    const skeleton = getSafeSkeleton();
    if (!animationState || !skeleton) return;

    if (!lastTime) lastTime = timestamp;
    const deltaTime = (timestamp - lastTime) / 1000;
    lastTime = timestamp;

    try {
      animationState.update(deltaTime);
      animationState.apply(skeleton);
      skeleton.updateWorldTransform();
    } catch (e) { console.warn('动画更新失败：', e); }

    animationLoop = requestAnimationFrame(animate);
  };

  animationLoop = requestAnimationFrame(animate);
};

const switchSkin = (skinName) => {
  if (!isPlayerReady.value) return;
  const skeleton = getSafeSkeleton();
  if (!skeleton) return;

  try {
    const skin = skeleton.data.findSkin(skinName);
    if (!skin) { ElMessage.warning(`未找到皮肤：${skinName}`); return; }
    skeleton.setSkin(skin);
    skeleton.setSlotsToSetupPose();
    skeleton.updateWorldTransform();
    setInitialPosition();
    currentSkin.value = skinName;
    if (currentAnimation.value) switchAnimation(currentAnimation.value);
  } catch (e) { ElMessage.error(`切换皮肤失败：${e.message}`); }
};

const switchAnimation = (animName) => {
  if (!isPlayerReady.value) return;
  const animationState = getSafeAnimationState();
  const skeleton = getSafeSkeleton();
  if (!animationState || !skeleton) return;

  try {
    animationState.clearTracks();
    const animation = skeleton.data.findAnimation(animName);
    if (!animation) { ElMessage.warning(`未找到动画：${animName}`); return; }
    
    animationState.data.defaultMix = 0.2;
    const trackEntry = animationState.setAnimation(0, animName, true);
    if (trackEntry) { trackEntry.time = 0; trackEntry.loop = true; }
    
    animationState.update(0);
    animationState.apply(skeleton);
    skeleton.updateWorldTransform();
    currentAnimation.value = animName;
    // 更新最后播放的动画记录
    lastPlayedAnimation.value = animName;
  } catch (e) { ElMessage.error(`播放动画失败：${e.message}`); }
};

watch(() => props.selectedRole, async (newRole) => {
  if (animationLoop) cancelAnimationFrame(animationLoop);
  if (newRole && newRole.spineObject) {
    await initPlayer(newRole.spineObject);
  } else if (!newRole) {
    cleanCameraResources();
    if (player && !player.disposed) player.dispose();
    player = null;
    isPlayerReady.value = false;
  }
}, { immediate: true, deep: true });

watch(() => props.canvasSize, () => {
  if (isPlayerReady.value && !player?.disposed) resizeCanvas();
}, { deep: true });

watch(() => props.characterViewport, () => {
  if (isPlayerReady.value && !player?.disposed) {
    setCharacterViewport();
    if (manualCamera) initCamera();
  }
}, { deep: true });

onMounted(() => {
  setTimeout(() => {
    if (props.selectedRole?.spineObject) initPlayer(props.selectedRole.spineObject);
  }, 100);
});

onUnmounted(() => {
  if (animationLoop) cancelAnimationFrame(animationLoop);
  cleanCameraResources();
  if (player && !player.disposed) player.dispose();
  player = null;
});
</script>

<style scoped>
.spine-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: transparent;
}

.spine-player-wrap {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: auto;
  z-index: 1;
}

.spine-player-wrap :deep(canvas) {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain;
  pointer-events: auto;
  cursor: grab; /* 鼠标样式 */
}

.ui-controls {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  gap: 30px;
  pointer-events: none;
}

.btn-group {
  pointer-events: auto;
  padding: 15px;
  border-radius: 10px;
  color: white;
}

.btn-group h4 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #fff;
}

.btn-group button {
  padding: 8px 12px;
  margin: 0 5px 8px;
  border: none;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  pointer-events: auto;
}

.btn-group button.active {
  background: rgba(74, 144, 226, 0.8);
  color: white;
}

.btn-group button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-group button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.25);
}

/* 重置按钮样式统一 */
.btn-group button:last-child {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.btn-group button:last-child:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.25);
}
</style>