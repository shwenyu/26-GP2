-- 方案一扩展：新增用户推荐药物记录表，用于保存 TSV 匹配结果
USE biomed;

CREATE TABLE IF NOT EXISTS recommendation_record
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sample_id INT NOT NULL,
    drug_label_id VARCHAR(100) NOT NULL,
    drug_name VARCHAR(200) NULL,
    source VARCHAR(100) NULL,
    summary_markdown TEXT NULL,
    matched_genes VARCHAR(1000) NULL,
    created_at DATETIME NULL,
    CONSTRAINT fk_recommendation_record_user
        FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_recommendation_record_sample
        FOREIGN KEY (sample_id) REFERENCES sample (id),
    CONSTRAINT fk_recommendation_record_drug_label
        FOREIGN KEY (drug_label_id) REFERENCES drug_label (id)
);

SET @has_user_created_idx := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'recommendation_record'
      AND INDEX_NAME = 'idx_recommendation_user_created_at'
);
SET @sql := IF(@has_user_created_idx = 0,
               'CREATE INDEX idx_recommendation_user_created_at ON recommendation_record (user_id, created_at)',
               'SELECT ''skip idx_recommendation_user_created_at''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_sample_idx := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'recommendation_record'
      AND INDEX_NAME = 'idx_recommendation_sample'
);
SET @sql := IF(@has_sample_idx = 0,
               'CREATE INDEX idx_recommendation_sample ON recommendation_record (sample_id)',
               'SELECT ''skip idx_recommendation_sample''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


