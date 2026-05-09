# Guideline Import Scripts

This folder contains maintenance scripts for Feature2 guideline data refresh.

## Files

- `import_guidelines.sh`: one-stop pipeline for migration, import, and verification.

## What `import_guidelines.sh` does

1. Runs migration SQL files:
   - `src/main/sql/migration/V20260410_01_add_evidence_level_to_dosing_guideline.sql`
   - `src/main/sql/migration/V20260410_02_backfill_guideline_source_and_drug_id.sql`
2. Imports guideline data by mode:
   - `seed`: runs `cn.edu.zju.importer.CpicImporter` with classpath seed file
   - `file`: runs `cn.edu.zju.importer.CpicImporter <your_tsv>`
   - `crawler`: runs `cn.edu.zju.crawler.Main` (full refresh path)
   - `guideline`: runs `cn.edu.zju.crawler.GuidelineRefreshMain` (guideline-only refresh)
   - `kb`: runs `cn.edu.zju.crawler.KnowledgeBaseRefreshMain` (refresh `drug` + `dosing_guideline`)
3. Verifies data quality with SQL checks:
   - distribution by `source`
   - distribution by `evidence_level`
   - null/empty `evidence_level` count
   - unresolved `drug_id` count for `source='cpic'`

## Prerequisites

- `mysql` client in PATH
- `java` in PATH
- compiled classes in `target/classes`
- runtime jars in `target/haining_biomed/WEB-INF/lib/`

> Note: this script does not require Maven at runtime. If build artifacts are missing,
> build from IntelliJ or Maven first.

## Usage

```bash
# Basic run (seed mode)
bash scripts/import_guidelines.sh --db-password 'biomed'

# Import custom TSV
bash scripts/import_guidelines.sh --mode file --tsv /absolute/path/to/cpic.tsv --db-password 'biomed'

# Full crawler refresh
bash scripts/import_guidelines.sh --mode crawler --db-password 'biomed'

# Guideline-only refresh (recommended for evidence/source backfill)
bash scripts/import_guidelines.sh --mode guideline --db-password 'biomed'

# Knowledge base refresh (add/update drug + dosing guideline)
bash scripts/import_guidelines.sh --mode kb --db-password 'biomed'

# Strict check for periodic job
bash scripts/import_guidelines.sh --strict --db-password 'biomed'
```

## Useful options

- `--skip-migration`: only import + verify
- `--skip-import`: only migration + verify
- `--skip-verify`: only migration + import
- `--dry-run`: print planned actions only

## Logging

- Logs are written to `logs/import_guidelines_YYYYMMDD_HHMMSS.log`.
- Keep these logs for class demo traceability and periodic import audit.

## Source naming convention

`source` is normalized to short codes during import/crawl:

- `cpic`
- `cpnds`
- `dpwg`
- `fda`
- `pro`
- `pharmgkb`
- `unknown`

This keeps filtering stable even if upstream payload returns full source names.

## Periodic refresh setup (macOS)

Option A (simple): add a cron entry to run every day at 02:30.

```bash
(crontab -l 2>/dev/null; echo "30 2 * * * cd /Users/shwenyu/IdeaProjects/haining_biomed && /bin/bash scripts/import_guidelines.sh --mode kb --db-password 'biomed' --strict >> logs/cron_kb_refresh.log 2>&1") | crontab -
```

Check cron entries:

```bash
crontab -l
```

Option B (recommended for macOS production): use `launchd` with a plist file.

## Rollback suggestion

If a CPIC import is wrong and you need quick cleanup:

```sql
DELETE FROM dosing_guideline
WHERE source = 'cpic'
  AND id LIKE 'CPIC_%';
```

For safer rollback, take a backup before import and restore by backup table.

