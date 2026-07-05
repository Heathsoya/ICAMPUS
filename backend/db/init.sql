-- ICAMPUS MySQL 8.0 database initialization.
-- Run from repository root:
--   mysql -u root -p < backend/db/init.sql

CREATE DATABASE IF NOT EXISTS icampus
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE icampus;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识条目ID',
    question VARCHAR(500) NOT NULL COMMENT '标准问题',
    answer TEXT NOT NULL COMMENT '标准答案',
    category VARCHAR(100) COMMENT '分类',
    keywords VARCHAR(500) COMMENT '空格分隔关键词',
    source VARCHAR(200) COMMENT '数据来源',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge_question (question),
    INDEX idx_knowledge_category (category),
    FULLTEXT INDEX ft_knowledge_question_answer (question, answer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

CREATE TABLE IF NOT EXISTS question_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '问答日志ID',
    user_id BIGINT NULL COMMENT '提问用户ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT '系统答案',
    matched_question VARCHAR(500) COMMENT '匹配到的知识库问题',
    category VARCHAR(100) COMMENT '问题分类',
    confidence DOUBLE COMMENT '置信度',
    answer_source VARCHAR(50) NOT NULL COMMENT 'knowledge_base / not_found / llm',
    related_questions JSON COMMENT '关联问题JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_question_log_user_id (user_id),
    INDEX idx_question_log_created_at (created_at),
    CONSTRAINT fk_question_log_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    CONSTRAINT chk_question_log_answer_source
        CHECK (answer_source IN ('knowledge_base', 'not_found', 'llm'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答日志表';

CREATE TABLE IF NOT EXISTS answer_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    question_log_id BIGINT NOT NULL COMMENT '问答日志ID',
    user_id BIGINT NULL COMMENT '反馈用户ID',
    helpful TINYINT(1) NULL COMMENT '是否有帮助',
    comment VARCHAR(1000) COMMENT '反馈内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_feedback_question_log_id (question_log_id),
    INDEX idx_feedback_user_id (user_id),
    UNIQUE KEY uk_feedback_log_user (question_log_id, user_id),
    CONSTRAINT fk_feedback_question_log
        FOREIGN KEY (question_log_id) REFERENCES question_log(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案反馈表';

CREATE TABLE IF NOT EXISTS contribution (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '贡献ID',
    user_id BIGINT NULL COMMENT '提交用户ID',
    question VARCHAR(500) NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending / approved / rejected',
    audit_reason VARCHAR(500) COMMENT '审核原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_contribution_user_id (user_id),
    INDEX idx_contribution_status (status),
    CONSTRAINT fk_contribution_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    CONSTRAINT chk_contribution_status
        CHECK (status IN ('pending', 'approved', 'rejected'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户贡献表';

CREATE TABLE IF NOT EXISTS qa_keyword (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
    keyword VARCHAR(100) NOT NULL COMMENT '关键词',
    category VARCHAR(100) COMMENT '分类',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    weight INT NOT NULL DEFAULT 0 COMMENT '权重',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_qa_keyword (keyword),
    INDEX idx_qa_keyword_enabled (enabled),
    INDEX idx_qa_keyword_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='QA关键词表';

CREATE TABLE IF NOT EXISTS qa_stop_word (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '停用词ID',
    word VARCHAR(100) NOT NULL COMMENT '停用词',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_qa_stop_word (word),
    INDEX idx_qa_stop_word_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='QA停用词表';
