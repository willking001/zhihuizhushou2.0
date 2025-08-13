<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span v-if="item.redirect === 'noRedirect' || index == levelList.length - 1" class="no-redirect">{{ item.meta.title }}</span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter, RouteLocationMatched } from 'vue-router'
import { compile } from 'path-to-regexp'

const route = useRoute()
const router = useRouter()
const levelList = ref<RouteLocationMatched[]>([])

function getBreadcrumb() {
  // only show routes with meta.title
  let matched = route.matched.filter(item => item.meta && item.meta.title)
  const first = matched[0]

  if (!isDashboard(first)) {
    matched = ([{ path: '/dashboard', meta: { title: 'Dashboard' }}] as RouteLocationMatched[]).concat(matched)
  }

  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}

function isDashboard(route: RouteLocationMatched) {
  const name = route && route.name
  if (!name) {
    return false
  }
  return name.toString().trim().toLocaleLowerCase() === 'Dashboard'.toLocaleLowerCase()
}

function pathCompile(path: string) {
  const { params } = route
  const toPath = compile(path)
  return toPath(params)
}

function handleLink(item: RouteLocationMatched) {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect.toString())
    return
  }
  router.push(pathCompile(path))
}

watch(() => route.path, () => {
  getBreadcrumb()
}, { immediate: true })
</script>

<style lang="scss" scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>