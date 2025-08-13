<template>
  <component :is="type" v-bind="linkProps(to)">
    <slot />
  </component>
</template>

<script setup lang="ts">
import { isExternal } from '@/utils/validate'

const props = defineProps({
  to: {
    type: String,
    required: true
  }
})

const isExternalLink = isExternal(props.to)
const type = isExternalLink ? 'a' : 'router-link'

function linkProps(to: string) {
  if (isExternalLink) {
    return {
      href: to,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return { to }
}
</script>