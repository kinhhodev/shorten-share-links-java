#!/usr/bin/env bash
# Cursor hook: plays a short chime sequence when the agent run completes ("stop" event).
# stdin: JSON payload from Cursor (read and discard).
# stdout: minimal JSON for hook protocol.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=chime-lib.sh
source "${ROOT}/chime-lib.sh"

cat >/dev/null

play_done_chimes

echo '{}'
exit 0
