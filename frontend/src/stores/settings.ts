import { defineStore } from 'pinia'
import defaultSettings from '@/settings'

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    showSettings: defaultSettings.showSettings,
    tagsView: defaultSettings.tagsView,
    fixedHeader: defaultSettings.fixedHeader,
    sidebarLogo: defaultSettings.sidebarLogo
  }),
  actions: {
    changeSetting(data: { key: string, value: any }) {
      const { key, value } = data
      if (Object.prototype.hasOwnProperty.call(this, key)) {
        (this as any)[key] = value
      }
    }
  }
})