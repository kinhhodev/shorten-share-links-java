#!/usr/bin/env bash
# Shared sounds for Cursor hooks (sourced by other scripts).

play_done_chimes() {
  if command -v afplay >/dev/null 2>&1; then
    afplay /System/Library/Sounds/Glass.aiff 2>/dev/null || true
    sleep 0.18
    afplay /System/Library/Sounds/Ping.aiff 2>/dev/null || true
    sleep 0.18
    afplay /System/Library/Sounds/Pop.aiff 2>/dev/null || true
    return 0
  fi

  if command -v paplay >/dev/null 2>&1; then
    for s in /usr/share/sounds/freedesktop/stereo/complete.oga /usr/share/sounds/freedesktop/stereo/message.oga; do
      if [[ -f "$s" ]]; then
        paplay "$s" 2>/dev/null || true
        sleep 0.18
      fi
    done
    return 0
  fi

  for _ in 1 2 3; do
    printf '\a'
    sleep 0.2
  done
}

# Shorter cue: terminal command pending / often needs review (e.g. git push, docker login).
play_attention_chime() {
  if command -v afplay >/dev/null 2>&1; then
    afplay /System/Library/Sounds/Submarine.aiff 2>/dev/null || true
    sleep 0.12
    afplay /System/Library/Sounds/Tink.aiff 2>/dev/null || true
    return 0
  fi

  if command -v paplay >/dev/null 2>&1; then
    for s in /usr/share/sounds/freedesktop/stereo/message.oga; do
      if [[ -f "$s" ]]; then
        paplay "$s" 2>/dev/null || true
      fi
    done
    return 0
  fi

  printf '\a'
  sleep 0.15
  printf '\a'
}
