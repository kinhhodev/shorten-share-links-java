#!/usr/bin/env bash
# Cursor hook: plays a short "attention" chime when a shell command is about to run
# (beforeShellExecution — often when Cursor is waiting for you to approve the command).
# Does not block: always returns permission allow.
# stdin: JSON with at least .command

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=chime-lib.sh
source "${ROOT}/chime-lib.sh"

input=$(cat)

command=""
if command -v python3 >/dev/null 2>&1; then
  command=$(
    printf '%s' "$input" | python3 -c "
import sys, json
try:
    d = json.load(sys.stdin)
    print(d.get('command') or '')
except Exception:
    print('')
" 2>/dev/null || true
  )
fi

should_chime() {
  local c="$1"
  [[ -z "$c" ]] && return 1
  # Commands that often need UI approval, credentials, or remote/network interaction.
  case "$c" in
    *git\ push*|*git\ pull*|*git\ fetch*|*git\ clone*|*git\ rebase*|*git\ merge*) return 0 ;;
    *docker\ push*|*docker\ login*|*docker\ pull*) return 0 ;;
    *podman\ push*|*podman\ login*) return 0 ;;
    *npm\ publish*|*pnpm\ publish*|*yarn\ publish*) return 0 ;;
    *ssh\ *|*scp\ *|*rsync\ *) return 0 ;;
    *gh\ auth*|*gh\ pr*) return 0 ;;
    *curl\ *|*wget\ *) return 0 ;;
  esac
  return 1
}

if should_chime "$command"; then
  play_attention_chime
fi

echo '{"permission":"allow"}'
exit 0
