import { defineStore } from 'pinia'
import axios from 'axios'

export const useConsultStore = defineStore('consult', {
  state: () => ({
    currentSession: null,
    messages: [],
    isInQueue: false,
    queuePosition: null,
    estimatedWait: 0
  }),

  actions: {
    async createSession(patientId, doctorId, type) {
      try {
        const res = await axios.post('http://localhost:8080/consult/create', null, {
          params: { patientId, doctorId, type }
        })
        if (res.data.code === '0') {
          const sessionId = res.data.data
          this.currentSession = { id: sessionId }
          return sessionId
        }
      } catch (error) {
        console.error('Create session error:', error)
      }
      return null
    },

    setQueueInfo(position, waitMinutes) {
      this.isInQueue = true
      this.queuePosition = position
      this.estimatedWait = waitMinutes
    },

    clearQueueInfo() {
      this.isInQueue = false
      this.queuePosition = null
      this.estimatedWait = 0
    },

    addMessage(message) {
      this.messages.push(message)
    },

    clearMessages() {
      this.messages = []
    },

    setCurrentSession(session) {
      this.currentSession = session
    }
  }
})