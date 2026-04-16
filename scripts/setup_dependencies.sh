#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LIB_DIR="$ROOT_DIR/lib"

mkdir -p "$LIB_DIR"

download_if_missing() {
  local target_file="$1"
  local source_url="$2"
  if [[ ! -f "$target_file" ]]; then
    echo "Downloading $(basename "$target_file")"
    curl -L --fail --output "$target_file" "$source_url"
  fi
}

download_if_missing "$LIB_DIR/sqlite-jdbc-3.46.1.3.jar" \
  "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.3/sqlite-jdbc-3.46.1.3.jar"

download_if_missing "$LIB_DIR/javafx-base-21.0.4.jar" \
  "https://repo1.maven.org/maven2/org/openjfx/javafx-base/21.0.4/javafx-base-21.0.4.jar"
download_if_missing "$LIB_DIR/javafx-base-21.0.4-mac-aarch64.jar" \
  "https://repo1.maven.org/maven2/org/openjfx/javafx-base/21.0.4/javafx-base-21.0.4-mac-aarch64.jar"
download_if_missing "$LIB_DIR/javafx-controls-21.0.4.jar" \
  "https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21.0.4/javafx-controls-21.0.4.jar"
download_if_missing "$LIB_DIR/javafx-controls-21.0.4-mac-aarch64.jar" \
  "https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21.0.4/javafx-controls-21.0.4-mac-aarch64.jar"
download_if_missing "$LIB_DIR/javafx-graphics-21.0.4.jar" \
  "https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21.0.4/javafx-graphics-21.0.4.jar"
download_if_missing "$LIB_DIR/javafx-graphics-21.0.4-mac-aarch64.jar" \
  "https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21.0.4/javafx-graphics-21.0.4-mac-aarch64.jar"

echo "Dependencies are ready in $LIB_DIR"
