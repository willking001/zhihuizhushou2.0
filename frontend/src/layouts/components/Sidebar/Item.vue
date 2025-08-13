<script lang="ts">
import { defineComponent, h } from 'vue'

export default defineComponent({
  name: 'MenuItem',
  functional: true,
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
  setup(props) {
    const vnodes: any[] = []

    if (props.icon) {
      if (props.icon.includes('el-icon')) {
        vnodes.push(h('i', { class: [props.icon, 'sub-el-icon'] }))
      } else {
        vnodes.push(h('svg-icon', { 'icon-class': props.icon }))
      }
    }

    if (props.title) {
      vnodes.push(h('span', { slot: 'title' }, props.title))
    }

    return () => vnodes
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