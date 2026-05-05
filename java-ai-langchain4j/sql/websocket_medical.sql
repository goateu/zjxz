-- 医疗问诊系统 WebSocket 相关表
-- 数据库: guiguxiaozhi

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `id_card` VARCHAR(18) COMMENT '身份证号',
  `phone` VARCHAR(20) COMMENT '手机号',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 医生表
CREATE TABLE IF NOT EXISTS `doctor` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '关联系统用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '医生姓名',
  `title` VARCHAR(20) COMMENT '职称：主任医师、副主任医师、主治医师',
  `department` VARCHAR(50) COMMENT '科室',
  `expertise` VARCHAR(200) COMMENT '擅长领域',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `max_consult_count` INT DEFAULT 3 COMMENT '最大同时接诊数',
  `status` VARCHAR(20) DEFAULT 'OFFLINE' COMMENT 'ONLINE, OFFLINE, BUSY',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 问诊会话表
CREATE TABLE IF NOT EXISTS `consult_session` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT COMMENT '医生ID（AI问诊时为null）',
  `type` VARCHAR(20) NOT NULL COMMENT 'AI, CONSULT',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, WAITING, CLOSED',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `end_time` DATETIME COMMENT '结束时间',
  INDEX `idx_patient` (`patient_id`),
  INDEX `idx_doctor` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 问诊消息表
CREATE TABLE IF NOT EXISTS `consult_message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_type` VARCHAR(20) NOT NULL COMMENT 'PATIENT, DOCTOR, AI',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 排队表
CREATE TABLE IF NOT EXISTS `queue_entry` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `session_id` BIGINT COMMENT '关联的会话ID',
  `position` INT NOT NULL COMMENT '排队位置',
  `estimated_wait_minutes` INT COMMENT '预计等待分钟数',
  `status` VARCHAR(20) DEFAULT 'WAITING' COMMENT 'WAITING, CALLED, CANCEL, TIMEOUT',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_doctor` (`doctor_id`),
  INDEX `idx_patient` (`patient_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试医生数据
INSERT INTO `doctor` (`user_id`, `name`, `title`, `department`, `expertise`, `avatar`) VALUES
(1, '张医生', '主任医师', '心内科', '高血压、冠心病、心律失常', '/avatar/zhang.jpg'),
(2, '李医生', '副主任医师', '神经内科', '头痛、脑血管疾病、帕金森', '/avatar/li.jpg'),
(3, '王医生', '主治医师', '呼吸内科', '感冒、肺炎、支气管炎', '/avatar/wang.jpg');

-- 插入测试用户数据
INSERT INTO `user` (`username`, `password`, `real_name`, `id_card`, `phone`) VALUES
('patient1', '123456', '张三', '110101199001011234', '13800138001'),
('patient2', '123456', '李四', '110101199002021234', '13800138002');