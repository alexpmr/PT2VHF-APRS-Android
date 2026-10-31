#!/usr/bin/env bash
# APRS symbols: https://github.com/hessu/aprs-symbols (OH7LZB).
# Pinned upstream commit and Git blob SHA protect against changed sprites.
set -euo pipefail
upstream="f2286a9cd43eb6ba4501250b4c39fff111e3796c"
target="app/src/main/assets/aprs"
mkdir -p "$target"
download() {
  local table="$1" blob="$2" file="$target/aprs-symbols-48-$table.png"
  if [[ -f "$file" && "$(git hash-object "$file")" == "$blob" ]]; then return; fi
  curl --fail --location --silent --show-error --retry 3 \
    "https://raw.githubusercontent.com/hessu/aprs-symbols/$upstream/png/aprs-symbols-48-$table.png" -o "$file"
  if [[ "$(git hash-object "$file")" != "$blob" ]]; then
    echo "Invalid APRS symbol sprite SHA for table $table" >&2
    exit 1
  fi
}
download 0 79e6ef113924451e61ed93c6328b7a31b0ea307a
download 1 f7e1a781876724fa3494cc65584c297d2e49af14
