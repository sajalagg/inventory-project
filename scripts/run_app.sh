#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_DIR="$ROOT_DIR/build/classes"
MAIN_CLASS="com.cloudkitchen.app.Main"

if [[ ! -f "$ROOT_DIR/lib/sqlite-jdbc-3.46.1.3.jar" ]]; then
  echo "Missing runtime jars. Run scripts/setup_dependencies.sh first."
  exit 1
fi

mkdir -p "$BUILD_DIR"
find "$BUILD_DIR" -type f -delete

javac \
  --class-path "$ROOT_DIR/lib/*" \
  -d "$BUILD_DIR" \
  $(find "$ROOT_DIR/src/main/java" -name "*.java")

java \
  --class-path "$BUILD_DIR:$ROOT_DIR/src/main/resources:$ROOT_DIR/lib/*" \
  "$MAIN_CLASS"
