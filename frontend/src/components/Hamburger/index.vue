<template>
  <div style="padding: 0 15px;" @click="toggleClick">
    <el-icon :size="20" class="icon">
      <component :is="isActive ? 'fold' : 'expand'" />
    </el-icon>
  </div>
</template>

<script setup lang="ts">
import { Fold, Expand } from '@element-plus/icons-vue'

defineProps({
  isActive: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggleClick'])

function toggleClick() {
  emit('toggleClick')
}
</script>

<style scoped>
.icon {
  vertical-align: middle;
}
</style>