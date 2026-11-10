#!/usr/bin/env bash
# APRS symbols: https://github.com/hessu/aprs-symbols (OH7LZB).
# Pinned upstream commit and Git blob SHA protect against changed sprites.
# #63: atlas de alta resolução (64 px @2x = células de 128 px) para ícones nítidos em telas densas;
# o app reduz para o tamanho escolhido com filtragem, nunca amplia bitmap pequeno.
set -euo pipefail
upstream="f2286a9cd43eb6ba4501250b4c39fff111e3796c"
target="app/src/main/assets/aprs"
mkdir -p "$target"
rm -f "$target"/aprs-symbols-48-*.png
download() {
  local table="$1" blob="$2"
  local file="$target/aprs-symbols-128-$table.png"
  if [[ -f "$file" && "$(git hash-object "$file")" == "$blob" ]]; then return; fi
  curl --fail --location --silent --show-error --retry 3 \
    "https://raw.githubusercontent.com/hessu/aprs-symbols/$upstream/png/aprs-symbols-64-$table@2x.png" -o "$file"
  if [[ "$(git hash-object "$file")" != "$blob" ]]; then
    echo "Invalid APRS symbol sprite SHA for table $table" >&2
    exit 1
  fi
}
download 0 e314445bb6058b95bb64d98209583211d7fef4cd
download 1 a0ca0a7b8b08ec190faa4d6439d329a9959952be
