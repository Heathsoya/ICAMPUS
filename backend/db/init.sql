-- ============================================
-- ICAMPUS 校园智能问答系统 — 数据库初始化脚本
-- 使用方法：mysql -u root -p < init.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS icampus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE icampus;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`   VARCHAR(50)  NOT NULL                 COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL                 COMMENT '密码（BCrypt加密）',
    `role`       VARCHAR(20)  NOT NULL DEFAULT 'USER'  COMMENT '角色：USER/ADMIN',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 知识库表
-- ============================================
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '条目ID',
    `question`   VARCHAR(500) NOT NULL                 COMMENT '问题',
    `answer`     TEXT         NOT NULL                 COMMENT '答案',
    `category`   VARCHAR(100)                          COMMENT '分类',
    `keywords`   VARCHAR(500)                          COMMENT '关键词（空格分隔）',
    `source`     VARCHAR(200)                          COMMENT '来源',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    FULLTEXT INDEX `ft_question_answer` (`question`, `answer`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- ============================================
-- 问答日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `question_log` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    `user_id`           BIGINT                                COMMENT '提问用户ID（匿名则为NULL）',
    `question`          TEXT         NOT NULL                 COMMENT '用户问题',
    `answer`            TEXT                                  COMMENT '返回答案',
    `matched_question`  VARCHAR(500)                          COMMENT '匹配到的知识库问题',
    `category`          VARCHAR(100)                          COMMENT '问题分类',
    `confidence`        DOUBLE                                COMMENT '置信度 0.0-1.0',
    `answer_source`     VARCHAR(50)  NOT NULL                 COMMENT '答案来源：KNOWLEDGE_BASE/NOT_FOUND/LLM',
    `related_questions` JSON                                  COMMENT '关联问题列表',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    CONSTRAINT `fk_question_log_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答日志表';

-- ============================================
-- 答案反馈表
-- ============================================
CREATE TABLE IF NOT EXISTS `answer_feedback` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '反馈ID',
    `question_log_id`  BIGINT       NOT NULL                 COMMENT '关联问答日志ID',
    `user_id`          BIGINT                                COMMENT '反馈用户ID',
    `helpful`          TINYINT(1)   NOT NULL                 COMMENT '是否有帮助：0否/1是',
    `comment`          VARCHAR(1000)                         COMMENT '反馈备注',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_question_log_id` (`question_log_id`),
    CONSTRAINT `fk_feedback_question_log` FOREIGN KEY (`question_log_id`) REFERENCES `question_log`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案反馈表';

-- ============================================
-- 用户贡献表（知识众筹审核）
-- ============================================
CREATE TABLE IF NOT EXISTS `contribution` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '贡献ID',
    `user_id`      BIGINT                                COMMENT '贡献用户ID',
    `question`     VARCHAR(500) NOT NULL                 COMMENT '问题',
    `answer`       TEXT         NOT NULL                 COMMENT '答案',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
    `audit_reason` VARCHAR(500)                          COMMENT '审核意见',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_contribution_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户贡献表';
