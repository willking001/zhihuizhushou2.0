<script lang="ts">
import { defineComponent, h } from 'vue'

export default defineComponent({
  name: 'MenuItem',
  props: {
    icon: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: ''
    }
  },
  render() {
    const {
      icon,
      title
    } = this
    const vnodes = []

    if (icon) {
      if (icon.includes('el-icon')) {
        vnodes.push(h('i', { class: [icon, 'sub-el-icon'] }))
      } else {
        vnodes.push(h('svg-icon', { 'icon-class': icon }))
      }
    }

    if (title) {
      vnodes.push(h('span', { slot: 'title' }, title))
    }
    return vnodes
  }
})
</script>

<style scoped>
.sub-el-icon {
  color: currentColor;
  width: 1em;
  height: 1em;
}
</style>