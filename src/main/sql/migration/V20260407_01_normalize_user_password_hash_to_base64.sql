-- Normalize user.password_hash to Base64(SHA-256) without salt for test consistency.
-- This migration converts only 64-char hex SHA-256 records.

USE biomed;

-- Before check: count rows that are currently hex format.
SELECT COUNT(*) AS hex_hash_count
FROM user
WHERE password_hash REGEXP '^[0-9A-Fa-f]{64}$';

-- Convert hex(SHA-256) -> Base64(SHA-256).
UPDATE user
SET password_hash = TO_BASE64(UNHEX(password_hash))
WHERE password_hash REGEXP '^[0-9A-Fa-f]{64}$';

-- After check: ensure no hex hashes remain.
SELECT COUNT(*) AS remaining_hex_hash_count
FROM user
WHERE password_hash REGEXP '^[0-9A-Fa-f]{64}$';

