-- 方案一：在现有 biomed 库上原地升级，不新建数据库
-- 目标：为认证系统建立 user 表，并为 sample 表准备从“文本上传者”迁移到“用户 ID”的过渡结构

USE biomed;

-- 1) 创建 user 表（若不存在）
CREATE TABLE IF NOT EXISTS user
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NULL,
    CONSTRAINT user_username_uindex UNIQUE (username)
);

-- 2) 若不存在 uploaded_by_legacy，则新增
SET @has_uploaded_by_legacy := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by_legacy'
);
SET @sql := IF(@has_uploaded_by_legacy = 0,
               'ALTER TABLE sample ADD COLUMN uploaded_by_legacy TEXT NULL',
               'SELECT ''skip add uploaded_by_legacy''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 若存在旧 uploaded_by 且 legacy 为空，则复制历史文本
SET @has_uploaded_by := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by'
);
SET @sql := IF(@has_uploaded_by = 1,
               'UPDATE sample SET uploaded_by_legacy = CAST(uploaded_by AS CHAR) WHERE uploaded_by_legacy IS NULL',
               'SELECT ''skip copy legacy data''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 若不存在 uploaded_by_user_id，则新增过渡列
SET @has_uploaded_by_user_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by_user_id'
);
SET @sql := IF(@has_uploaded_by_user_id = 0,
               'ALTER TABLE sample ADD COLUMN uploaded_by_user_id BIGINT NULL',
               'SELECT ''skip add uploaded_by_user_id''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 若不存在索引，则建立过渡列索引
SET @has_index := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND INDEX_NAME = 'idx_sample_uploaded_by_user_id'
);
SET @sql := IF(@has_index = 0,
               'CREATE INDEX idx_sample_uploaded_by_user_id ON sample (uploaded_by_user_id)',
               'SELECT ''skip create idx_sample_uploaded_by_user_id''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
