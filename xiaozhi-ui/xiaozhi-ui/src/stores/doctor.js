import { defineStore } from 'pinia'
import axios from 'axios'

export const useDoctorStore = defineStore('doctor', {
  state: () => ({
    doctorInfo: null,
    onlineDoctors: [],
    isOnline: false
  }),

  actions: {
    async login(username, password) {
      try {
        const res = await axios.post('http://localhost:8080/doctor/login', null, {
          params: { username, password }
        })
        if (res.data.code === '0') {
          this.doctorInfo = res.data.data
          this.isOnline = true
          localStorage.setItem('doctor_info', JSON.stringify(this.doctorInfo))
          return true
        }
        return false
      } catch (error) {
        console.error('Login error:', error)
        return false
      }
    },

    logout() {
      if (this.doctorInfo?.doctor?.id) {
        axios.post('http://localhost:8080/doctor/logout', null, {
          params: { doctorId: this.doctorInfo.doctor.id }
        })
      }
      this.doctorInfo = null
      this.isOnline = false
      localStorage.removeItem('doctor_info')
    },

    async getOnlineDoctors() {
      try {
        const res = await axios.get('http://localhost:8080/doctor/list')
        if (res.data.code === '0') {
          this.onlineDoctors = res.data.data
        }
      } catch (error) {
        console.error('Get doctors error:', error)
      }
    },

    initFromStorage() {
      const stored = localStorage.getItem('doctor_info')
      if (stored) {
        this.doctorInfo = JSON.parse(stored)
        this.isOnline = true
      }
    },

    heartbeat() {
      if (this.doctorInfo?.doctor?.id) {
        axios.post('http://localhost:8080/doctor/heartbeat', null, {
          params: { doctorId: this.doctorInfo.doctor.id }
        })
      }
    }
  }
})