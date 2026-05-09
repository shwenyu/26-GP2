# Feature2 Evidence Source and Level Guide

This note describes the Feature2 implementation for multi-source guideline evidence in `dosing_guideline`.

## What Changed

- Added `evidence_level` to `dosing_guideline` schema.
- Added query methods in `DosingGuidelineDao`:
  - `findBySource(...)`
  - `findByEvidenceLevel(...)`
  - `findBySourceAndEvidence(...)`
  - `findAllOrdered()`
- Added `/kb/guidelines` filtering in `KnowledgeBaseController` for:
  - `source`
  - `evidenceLevel`
- Added evidence color badges in `dosing_guideline.jsp`:
  - `1A/1B` -> high (green)
  - `2A/2B` -> mid (yellow)
  - `3/4` -> low (red)
- Added `CpicImporter` and bundled seed file:
  - `src/main/resources/cpic_guideline_seed.tsv`

## SQL Migration

Run migration:

```sql
ALTER TABLE dosing_guideline
    ADD COLUMN evidence_level VARCHAR(5) NULL AFTER source;

CREATE INDEX idx_dosing_guideline_source_evidence
    ON dosing_guideline (source, evidence_level);
```

Or execute the migration file:

- `src/main/sql/migration/V20260410_01_add_evidence_level_to_dosing_guideline.sql`

## CPIC Seed Import

Seed includes 5 core pairs:

- CYP2D6 - codeine
- CYP2C19 - clopidogrel
- CYP2C9/VKORC1 - warfarin
- SLCO1B1 - simvastatin
- HLA-B - abacavir

Run via `CpicImporter.main(...)` (no args uses bundled seed), or pass a TSV path.

## Verify in UI

Open:

- `/kb/guidelines`

Try filters:

- `source=CPIC`
- `evidenceLevel=1A`

Expected sorting order:

- `1A` -> `1B` -> `2A` -> `2B` -> `3` -> `4`

## Logging

Feature2 adds logs in:

- `KnowledgeBaseController` (request filter + result size)
- `DosingGuidelineDao` (query filter + result size)
- `DosingGuidelineCrawler` (saved guideline source/evidence)
- `CpicImporter` (inserted/skipped count)

