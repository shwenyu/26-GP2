-- 方案一（数据回填）：
-- 将旧 sample.uploaded_by / sample.uploaded_by_legacy 中的文本上传者，映射为 user 表中的账号
-- 默认密码说明：这里写入的是占位 hash，建议这些历史账号首次登录前由管理员重置密码

USE biomed;

-- 1) 若存在 uploaded_by_legacy，则为历史上传者建账号（去重）
SET @has_uploaded_by_legacy := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by_legacy'
);
SET @sql := IF(@has_uploaded_by_legacy = 1,
'INSERT INTO user (username, password_hash, created_at)
 SELECT legacy_name, ''MIGRATED_ACCOUNT_RESET_REQUIRED'', NOW()
 FROM (
          SELECT DISTINCT TRIM(uploaded_by_legacy) AS legacy_name
          FROM sample
          WHERE uploaded_by_legacy IS NOT NULL
            AND TRIM(uploaded_by_legacy) <> ''''
      ) legacy_users
 WHERE NOT EXISTS (
     SELECT 1 FROM user u WHERE u.username = legacy_users.legacy_name
 )',
'SELECT ''skip backfill users from legacy column''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 若存在 uploaded_by_user_id 与 uploaded_by_legacy，则回填样本归属
SET @has_uploaded_by_user_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sample'
      AND COLUMN_NAME = 'uploaded_by_user_id'
);
SET @sql := IF(@has_uploaded_by_legacy = 1 AND @has_uploaded_by_user_id = 1,
'UPDATE sample s
 JOIN user u ON u.username = TRIM(s.uploaded_by_legacy)
 SET s.uploaded_by_user_id = u.id
 WHERE s.uploaded_by_legacy IS NOT NULL
   AND TRIM(s.uploaded_by_legacy) <> ''''
   AND s.uploaded_by_user_id IS NULL',
'SELECT ''skip backfill sample uploaded_by_user_id''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 保底账号 legacy_import 只创建一次
INSERT INTO user (username, password_hash, created_at)
SELECT 'legacy_import', 'MIGRATED_ACCOUNT_RESET_REQUIRED', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE username = 'legacy_import'
);

-- 4) 若存在 uploaded_by_user_id，则把未映射数据回填给保底账号
SET @sql := IF(@has_uploaded_by_user_id = 1,
'UPDATE sample s
 JOIN user u ON u.username = ''legacy_import''
 SET s.uploaded_by_user_id = u.id
 WHERE s.uploaded_by_user_id IS NULL',
'SELECT ''skip fill legacy_import''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
