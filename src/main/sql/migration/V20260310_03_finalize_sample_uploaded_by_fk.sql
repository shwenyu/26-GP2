-- 方案一（最终收口）：
-- 将 sample 的正式 uploaded_by 切换为 user.id 外键语义，同时保留 uploaded_by_legacy 作为历史备份

USE biomed;

-- 1) 若 uploaded_by_user_id 不存在，说明前置迁移未完成，直接给出提示
SET @has_uploaded_by_user_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by_user_id'
);
SET @has_uploaded_by := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by'
);

-- 2) 只有在过渡列存在时，才删除旧 uploaded_by
SET @sql := IF(@has_uploaded_by_user_id = 1 AND @has_uploaded_by = 1,
               'ALTER TABLE sample DROP COLUMN uploaded_by',
               'SELECT ''skip drop old uploaded_by''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 重新检查 uploaded_by 是否还存在，再决定是否转正
SET @has_uploaded_by := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by'
);
SET @sql := IF(@has_uploaded_by_user_id = 1 AND @has_uploaded_by = 0,
               'ALTER TABLE sample CHANGE COLUMN uploaded_by_user_id uploaded_by BIGINT NULL',
               'SELECT ''skip rename uploaded_by_user_id''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 若 uploaded_by 索引不存在，则创建
SET @has_uploaded_by_index := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND INDEX_NAME = 'idx_sample_uploaded_by'
);
SET @has_uploaded_by := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by'
);
SET @sql := IF(@has_uploaded_by = 1 AND @has_uploaded_by_index = 0,
               'CREATE INDEX idx_sample_uploaded_by ON sample (uploaded_by)',
               'SELECT ''skip create idx_sample_uploaded_by''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 若外键不存在，则创建
SET @has_fk := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND CONSTRAINT_NAME = 'fk_sample_uploaded_by_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@has_uploaded_by = 1 AND @has_fk = 0,
               'ALTER TABLE sample ADD CONSTRAINT fk_sample_uploaded_by_user FOREIGN KEY (uploaded_by) REFERENCES user (id)',
               'SELECT ''skip add fk_sample_uploaded_by_user''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6) 历史文本列 uploaded_by_legacy 保留，不删除，便于后期核对迁移结果
