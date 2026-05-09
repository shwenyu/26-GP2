#!/usr/bin/env bash
set -euo pipefail

# import_guidelines.sh
# -----------------------------------------------------------------------------
# One-stop script for Feature2 guideline data refresh:
#   1) Run migration SQL files for source/evidence_level normalization
#   2) Import guideline data (CPIC seed/TSV or full crawler)
#   3) Verify source/evidence/drug_id data quality with SQL checks
#
# Design goals:
# - Safe by default: fail-fast, clear logs, explicit parameters
# - Repeatable: same command can be rerun for periodic import
# - Maintainable: each stage is a separate function with comments
#
# Requirements:
# - mysql client available in PATH
# - java available in PATH
# - compiled classes available in target/classes
# - dependency jars available in target/haining_biomed/WEB-INF/lib/*.jar
#
# Note:
# We do not assume Maven exists on this machine. This script can run against
# already-built classes. If classes are missing, build from IntelliJ or Maven.
# -----------------------------------------------------------------------------

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_PROPERTIES="$ROOT_DIR/src/main/resources/app.properties"
MIGRATION_DIR="$ROOT_DIR/src/main/sql/migration"
LOG_DIR="$ROOT_DIR/logs"
TIMESTAMP="$(date +"%Y%m%d_%H%M%S")"
LOG_FILE="$LOG_DIR/import_guidelines_${TIMESTAMP}.log"

# Defaults (overridden by app.properties or CLI flags)
ENV_DB_PASSWORD="${DB_PASSWORD:-}"
DB_HOST="127.0.0.1"
DB_PORT="3306"
DB_NAME="biomed"
DB_USER="biomed"
DB_PASSWORD=""

RUN_MODE="seed"          # seed | file | crawler | guideline | kb
TSV_PATH=""
SKIP_MIGRATION=0
SKIP_IMPORT=0
SKIP_VERIFY=0
DRY_RUN=0
STRICT_VERIFY=0

MIGRATION_FILES=(
  "$MIGRATION_DIR/V20260410_01_add_evidence_level_to_dosing_guideline.sql"
  "$MIGRATION_DIR/V20260410_02_backfill_guideline_source_and_drug_id.sql"
)

usage() {
  cat <<'EOF'
Usage:
  scripts/import_guidelines.sh [options]

Options:
  --mode <seed|file|crawler|guideline|kb>  Import mode (default: seed)
  --tsv <path>                   TSV file path (required when --mode file)
  --db-host <host>               DB host
  --db-port <port>               DB port
  --db-name <name>               DB name
  --db-user <user>               DB user
  --db-password <password>       DB password (or set DB_PASSWORD env var)
  --skip-migration               Skip SQL migration stage
  --skip-import                  Skip importer stage
  --skip-verify                  Skip verification SQL stage
  --dry-run                      Print planned actions only
  --strict                       Enable strict verification (non-zero fails)
  -h, --help                     Show help

Examples:
  # 1) Import classpath CPIC seed + migration + verification
  scripts/import_guidelines.sh --db-password 'biomed'

  # 2) Import a custom CPIC TSV
  scripts/import_guidelines.sh --mode file --tsv /path/to/cpic.tsv --db-password 'biomed'

  # 3) Full crawler refresh (drug/label/guideline + CPIC seed in Main)
  scripts/import_guidelines.sh --mode crawler --db-password 'biomed'

  # 4) Guideline-only refresh (recommended for evidence backfill)
  scripts/import_guidelines.sh --mode guideline --db-password 'biomed'

  # 5) Refresh drug + dosing guideline together
  scripts/import_guidelines.sh --mode kb --db-password 'biomed'

  # 6) Periodic run with strict checks
  scripts/import_guidelines.sh --mode seed --strict --db-password 'biomed'
EOF
}

log() {
  local level="$1"
  shift
  local msg="$*"
  local line="[$(date +"%F %T")] [$level] $msg"
  echo "$line" | tee -a "$LOG_FILE"
}

die() {
  log "ERROR" "$*"
  exit 1
}

ensure_log_dir() {
  mkdir -p "$LOG_DIR"
}

# Parse jdbc.url/jdbc.username/jdbc.password from app.properties as defaults.
load_defaults_from_app_properties() {
  [[ -f "$APP_PROPERTIES" ]] || return 0

  local jdbc_url
  jdbc_url="$(grep -E '^jdbc.url=' "$APP_PROPERTIES" | head -n1 | cut -d'=' -f2- || true)"
  local jdbc_user
  jdbc_user="$(grep -E '^jdbc.username=' "$APP_PROPERTIES" | head -n1 | cut -d'=' -f2- || true)"
  local jdbc_password
  jdbc_password="$(grep -E '^jdbc.password=' "$APP_PROPERTIES" | head -n1 | cut -d'=' -f2- || true)"

  if [[ -n "$jdbc_url" ]]; then
    # Example: jdbc:mysql://127.0.0.1:3306/biomed?serverTimezone=GMT%2B8
    DB_HOST="$(echo "$jdbc_url" | sed -E 's#^jdbc:mysql://([^:/?]+).*$#\1#')"
    DB_PORT="$(echo "$jdbc_url" | sed -E 's#^jdbc:mysql://[^:/?]+:([0-9]+)/.*$#\1#')"
    DB_NAME="$(echo "$jdbc_url" | sed -E 's#^jdbc:mysql://[^/]+/([^?]+).*$#\1#')"
  fi

  [[ -n "$jdbc_user" ]] && DB_USER="$jdbc_user"
  if [[ -n "${DB_PASSWORD:-}" ]]; then
    # Keep env-provided password if already present.
    :
  elif [[ -n "$jdbc_password" ]]; then
    DB_PASSWORD="$jdbc_password"
  fi
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --mode)
        RUN_MODE="${2:-}"
        shift 2
        ;;
      --tsv)
        TSV_PATH="${2:-}"
        shift 2
        ;;
      --db-host)
        DB_HOST="${2:-}"
        shift 2
        ;;
      --db-port)
        DB_PORT="${2:-}"
        shift 2
        ;;
      --db-name)
        DB_NAME="${2:-}"
        shift 2
        ;;
      --db-user)
        DB_USER="${2:-}"
        shift 2
        ;;
      --db-password)
        DB_PASSWORD="${2:-}"
        shift 2
        ;;
      --skip-migration)
        SKIP_MIGRATION=1
        shift
        ;;
      --skip-import)
        SKIP_IMPORT=1
        shift
        ;;
      --skip-verify)
        SKIP_VERIFY=1
        shift
        ;;
      --dry-run)
        DRY_RUN=1
        shift
        ;;
      --strict)
        STRICT_VERIFY=1
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        die "Unknown argument: $1"
        ;;
    esac
  done

  case "$RUN_MODE" in
    seed|file|crawler|guideline|kb) ;;
    *) die "Invalid --mode '$RUN_MODE' (must be seed|file|crawler|guideline|kb)" ;;
  esac

  if [[ "$RUN_MODE" == "file" && -z "$TSV_PATH" ]]; then
    die "--mode file requires --tsv <path>"
  fi
  if [[ "$RUN_MODE" == "file" && ! -f "$TSV_PATH" ]]; then
    die "TSV file not found: $TSV_PATH"
  fi
}

check_prerequisites() {
  command -v mysql >/dev/null 2>&1 || die "mysql client not found in PATH"
  command -v java >/dev/null 2>&1 || die "java not found in PATH"

  [[ -d "$ROOT_DIR/target/classes" ]] || die "Missing target/classes. Build project first (IntelliJ Build or Maven compile)."
  [[ -d "$ROOT_DIR/target/haining_biomed/WEB-INF/lib" ]] || die "Missing target/haining_biomed/WEB-INF/lib. Build project artifact first."

  for f in "${MIGRATION_FILES[@]}"; do
    [[ -f "$f" ]] || die "Migration file not found: $f"
  done
}

mysql_exec() {
  local sql="$1"
  MYSQL_PWD="$DB_PASSWORD" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -D "$DB_NAME" -N -e "$sql"
}

mysql_exec_file() {
  local file="$1"
  MYSQL_PWD="$DB_PASSWORD" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" < "$file"
}

run_migrations() {
  log "INFO" "Stage 1/3 - Running migrations"
  for f in "${MIGRATION_FILES[@]}"; do
    log "INFO" "Applying migration: $f"
    if [[ "$DRY_RUN" -eq 0 ]]; then
      mysql_exec_file "$f"
    fi
  done
  log "INFO" "Migrations completed"
}

build_classpath() {
  # Runtime classpath for running Java main classes without Maven exec plugin.
  echo "$ROOT_DIR/target/classes:$ROOT_DIR/target/haining_biomed/WEB-INF/lib/*"
}

run_import() {
  log "INFO" "Stage 2/3 - Running importer mode=$RUN_MODE"
  local cp
  cp="$(build_classpath)"

  case "$RUN_MODE" in
    seed)
      log "INFO" "Running cn.edu.zju.importer.CpicImporter (classpath seed TSV)"
      if [[ "$DRY_RUN" -eq 0 ]]; then
        java -cp "$cp" cn.edu.zju.importer.CpicImporter 2>&1 | tee -a "$LOG_FILE"
      fi
      ;;
    file)
      log "INFO" "Running cn.edu.zju.importer.CpicImporter with TSV=$TSV_PATH"
      if [[ "$DRY_RUN" -eq 0 ]]; then
        java -cp "$cp" cn.edu.zju.importer.CpicImporter "$TSV_PATH" 2>&1 | tee -a "$LOG_FILE"
      fi
      ;;
    crawler)
      log "INFO" "Running cn.edu.zju.crawler.Main (full refresh flow)"
      if [[ "$DRY_RUN" -eq 0 ]]; then
        java -cp "$cp" cn.edu.zju.crawler.Main 2>&1 | tee -a "$LOG_FILE"
      fi
      ;;
    guideline)
      log "INFO" "Running cn.edu.zju.crawler.GuidelineRefreshMain (guideline-only refresh)"
      if [[ "$DRY_RUN" -eq 0 ]]; then
        java -cp "$cp" cn.edu.zju.crawler.GuidelineRefreshMain 2>&1 | tee -a "$LOG_FILE"
      fi
      ;;
    kb)
      log "INFO" "Running cn.edu.zju.crawler.KnowledgeBaseRefreshMain (drug + guideline refresh)"
      if [[ "$DRY_RUN" -eq 0 ]]; then
        java -cp "$cp" cn.edu.zju.crawler.KnowledgeBaseRefreshMain 2>&1 | tee -a "$LOG_FILE"
      fi
      ;;
  esac

  log "INFO" "Importer stage completed"
}

verify_data() {
  log "INFO" "Stage 3/3 - Running verification SQL"

  local source_stats
  local evidence_stats
  local null_evidence
  local unresolved_cpic_drug

  source_stats="$(mysql_exec "SELECT CONCAT(COALESCE(source,'NULL'), ':', COUNT(*)) FROM dosing_guideline GROUP BY source ORDER BY COUNT(*) DESC;")"
  evidence_stats="$(mysql_exec "SELECT CONCAT(COALESCE(evidence_level,'NULL'), ':', COUNT(*)) FROM dosing_guideline GROUP BY evidence_level ORDER BY FIELD(evidence_level,'1A','1B','2A','2B','3','4'), evidence_level;")"
  null_evidence="$(mysql_exec "SELECT COUNT(*) FROM dosing_guideline WHERE evidence_level IS NULL OR TRIM(evidence_level)='';")"
  unresolved_cpic_drug="$(mysql_exec "SELECT COUNT(*) FROM dosing_guideline WHERE source='cpic' AND (drug_id IS NULL OR TRIM(drug_id)='');")"

  log "INFO" "Source distribution:"
  while IFS= read -r line; do
    [[ -n "$line" ]] && log "INFO" "  $line"
  done <<< "$source_stats"

  log "INFO" "Evidence distribution:"
  while IFS= read -r line; do
    [[ -n "$line" ]] && log "INFO" "  $line"
  done <<< "$evidence_stats"

  log "INFO" "Null/empty evidence count: $null_evidence"
  log "INFO" "CPIC unresolved drug_id count: $unresolved_cpic_drug"

  if [[ "$STRICT_VERIFY" -eq 1 ]]; then
    # Strict mode can be used in CI/cron to fail early on bad data quality.
    if [[ "$null_evidence" -gt 0 ]]; then
      die "Strict check failed: null evidence exists ($null_evidence)"
    fi
    if [[ "$unresolved_cpic_drug" -gt 0 ]]; then
      die "Strict check failed: unresolved CPIC drug_id exists ($unresolved_cpic_drug)"
    fi
  fi

  log "INFO" "Verification completed"
}

print_summary() {
  log "INFO" "Done. Log file: $LOG_FILE"
  log "INFO" "Rollback tip: if this run imported wrong CPIC rows, execute:"
  log "INFO" "  DELETE FROM dosing_guideline WHERE source='cpic' AND id LIKE 'CPIC_%';"
}

main() {
  ensure_log_dir
  load_defaults_from_app_properties
  parse_args "$@"

  # Environment variable DB_PASSWORD has priority if CLI/app.properties did not provide one.
  if [[ -z "$DB_PASSWORD" && -n "$ENV_DB_PASSWORD" ]]; then
    DB_PASSWORD="$ENV_DB_PASSWORD"
  fi

  log "INFO" "Starting guideline import pipeline"
  log "INFO" "Root dir: $ROOT_DIR"
  log "INFO" "DB target: $DB_HOST:$DB_PORT/$DB_NAME user=$DB_USER"
  log "INFO" "Run mode: $RUN_MODE dryRun=$DRY_RUN strict=$STRICT_VERIFY"

  check_prerequisites

  if [[ "$SKIP_MIGRATION" -eq 0 ]]; then
    run_migrations
  else
    log "INFO" "Skipping migration stage"
  fi

  if [[ "$SKIP_IMPORT" -eq 0 ]]; then
    run_import
  else
    log "INFO" "Skipping import stage"
  fi

  if [[ "$SKIP_VERIFY" -eq 0 ]]; then
    if [[ "$DRY_RUN" -eq 0 ]]; then
      verify_data
    else
      log "INFO" "Skipping verification execution in dry-run mode"
    fi
  else
    log "INFO" "Skipping verification stage"
  fi

  print_summary
}

main "$@"



