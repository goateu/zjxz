import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: localStorage.getItem('user_id') || null,
    userType: 'PATIENT' // PATIENT, DOCTOR
  }),

  actions: {
    setUser(userId, userType = 'PATIENT') {
      this.userId = userId
      this.userType = userType
      localStorage.setItem('user_id', userId)
    },

    initFromStorage() {
      this.userId = localStorage.getItem('user_id')
    }
  }
})