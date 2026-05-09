UPDATE dosing_guideline
SET source = LOWER(TRIM(source))
WHERE source IS NOT NULL;

UPDATE dosing_guideline
SET source = 'cpic'
WHERE source IN ('clinical pharmacogenetics implementation consortium');

UPDATE dosing_guideline
SET source = 'cpnds'
WHERE source IN ('canadian pharmacogenomics network for drug safety');

UPDATE dosing_guideline
SET source = 'dpwg'
WHERE source IN ('dutch pharmacogenetics working group');

UPDATE dosing_guideline
SET source = 'fda'
WHERE source IN ('u.s. food and drug administration', 'us food and drug administration', 'food and drug administration');

UPDATE dosing_guideline
SET source = 'pro'
WHERE source IN ('professional society');

UPDATE dosing_guideline
SET source = 'unknown'
WHERE source IS NULL OR TRIM(source) = '';

UPDATE dosing_guideline
SET evidence_level = UPPER(TRIM(evidence_level))
WHERE evidence_level IS NOT NULL;

UPDATE dosing_guideline
SET evidence_level = NULL
WHERE evidence_level IS NOT NULL
  AND evidence_level NOT IN ('1A', '1B', '2A', '2B', '3', '4');

-- Fallback policy: when upstream source does not provide a resolvable evidence token,
-- set to the lowest confidence tier to avoid NULL filtering/display gaps.
UPDATE dosing_guideline
SET evidence_level = '4'
WHERE evidence_level IS NULL OR TRIM(evidence_level) = '';

UPDATE dosing_guideline dg
JOIN drug d
  ON LOWER(REPLACE(REPLACE(REPLACE(REPLACE(d.name, ' ', ''), '-', ''), '/', ''), '_', ''))
     = LOWER(REPLACE(REPLACE(REPLACE(REPLACE(
         SUBSTRING_INDEX(SUBSTRING_INDEX(dg.raw, '\t', 2), '\t', -1),
         ' ', ''), '-', ''), '/', ''), '_', ''))
SET dg.drug_id = d.id
WHERE dg.drug_id IS NULL
  AND dg.source = 'cpic';


