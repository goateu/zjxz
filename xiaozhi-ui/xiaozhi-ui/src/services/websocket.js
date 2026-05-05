import Stomp from 'stompjs'
import SockJS from 'sockjs-client'

class WebSocketService {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.subscriptions = []
  }

  connect(userId, userType, onConnected, onError) {
    const socket = new SockJS(__WS_URL__)
    this.stompClient = Stomp.over(socket)

    this.stompClient.connect(
      {},
      (frame) => {
        console.log('WebSocket connected:', frame)
        this.connected = true

        // 存储用户信息到session
        this.stompClient.send('/app/heartbeat', {},
          JSON.stringify({ userId, userType }))

        if (onConnected) onConnected()
      },
      (error) => {
        console.error('WebSocket error:', error)
        this.connected = false
        if (onError) onError(error)
      }
    )
  }

  disconnect() {
    if (this.stompClient) {
      this.subscriptions.forEach(sub => sub.unsubscribe())
      this.subscriptions = []
      this.stompClient.disconnect()
      this.stompClient = null
      this.connected = false
    }
  }

  subscribe(destination, callback) {
    if (!this.stompClient || !this.connected) {
      console.warn('WebSocket not connected')
      return null
    }

    const subscription = this.stompClient.subscribe(destination, (message) => {
      if (message.body) {
        const data = JSON.parse(message.body)
        callback(data)
      }
    })

    this.subscriptions.push(subscription)
    return subscription
  }

  send(destination, body) {
    if (!this.stompClient || !this.connected) {
      console.warn('WebSocket not connected')
      return
    }
    this.stompClient.send(destination, {}, JSON.stringify(body))
  }

  // 发送聊天消息
  sendChatMessage(sessionId, senderId, senderType, senderName, content) {
    this.send('/app/chat.send', {
      type: 'TEXT',
      sessionId: sessionId.toString(),
      senderId,
      senderType,
      senderName,
      content,
      timestamp: Date.now()
    })
  }

  // 加入排队
  joinQueue(doctorId, patientId, sessionId) {
    this.send('/app/queue.join', {
      type: 'QUEUE_JOIN',
      doctorId,
      patientId,
      sessionId
    })
  }

  // 取消排队
  cancelQueue(doctorId, patientId) {
    this.send('/app/queue.cancel', {
      type: 'QUEUE_CANCEL',
      doctorId,
      patientId
    })
  }

  // 医生叫号
  callPatient(doctorId, patientId, sessionId) {
    this.send('/app/queue.call', {
      type: 'QUEUE_CALL',
      doctorId,
      patientId,
      sessionId
    })
  }

  // 结束问诊
  endConsult(sessionId) {
    this.send('/app/consult.end', {
      type: 'CONSULT_END',
      sessionId
    })
  }
}

export default new WebSocketService()