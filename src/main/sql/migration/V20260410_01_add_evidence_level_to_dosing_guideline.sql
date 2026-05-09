-- Idempotent migration: safe to rerun on environments where the column/index already exist.
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'dosing_guideline'
      AND column_name = 'evidence_level'
);

SET @add_col_sql = IF(
    @col_exists = 0,
    'ALTER TABLE dosing_guideline ADD COLUMN evidence_level VARCHAR(5) NULL AFTER source',
    'SELECT "evidence_level already exists"'
);

PREPARE stmt_col FROM @add_col_sql;
EXECUTE stmt_col;
DEALLOCATE PREPARE stmt_col;

SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'dosing_guideline'
      AND index_name = 'idx_dosing_guideline_source_evidence'
);

SET @create_idx_sql = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_dosing_guideline_source_evidence ON dosing_guideline (source, evidence_level)',
    'SELECT "idx_dosing_guideline_source_evidence already exists"'
);

PREPARE stmt_idx FROM @create_idx_sql;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;

