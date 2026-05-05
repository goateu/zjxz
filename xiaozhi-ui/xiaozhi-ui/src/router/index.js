import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/patient'
  },
  {
    path: '/patient/login',
    name: 'PatientLogin',
    component: () => import('@/views/patient/PatientLogin.vue')
  },
  {
    path: '/patient/register',
    name: 'PatientRegister',
    component: () => import('@/views/patient/PatientRegister.vue')
  },
  {
    path: '/patient',
    component: () => import('@/views/common/HomeLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/patient/ai'
      },
      {
        path: 'ai',
        name: 'PatientAI',
        component: () => import('@/views/patient/PatientAI.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'doctors',
        name: 'DoctorList',
        component: () => import('@/views/patient/DoctorList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'queue/:doctorId',
        name: 'QueuePage',
        component: () => import('@/views/patient/QueuePage.vue')
      },
      {
        path: 'chat/:sessionId',
        name: 'PatientChat',
        component: () => import('@/views/patient/PatientChat.vue')
      }
    ]
  },
  {
    path: '/doctor/login',
    name: 'DoctorLogin',
    component: () => import('@/views/doctor/DoctorLogin.vue')
  },
  {
    path: '/doctor/dashboard',
    name: 'DoctorDashboard',
    component: () => import('@/views/doctor/DoctorDashboard.vue'),
    meta: { requiresAuth: true, isDoctor: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    if (to.meta.isDoctor) {
      // 医生端需要医生登录
      const doctorInfo = localStorage.getItem('doctor_info')
      if (!doctorInfo) {
        next('/doctor/login')
        return
      }
    } else {
      // 患者端需要患者登录
      const userId = localStorage.getItem('user_id')
      if (!userId) {
        next('/patient/login')
        return
      }
    }
  }
  next()
})

export default router