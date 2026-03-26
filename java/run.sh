#!/usr/bin/env bash
# run.sh
# Compiles (if needed) and launches the Fruit Collector Game.
#
# Usage:
#   cd java
#   chmod +x run.sh   (first time only)
#   ./run.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

OUT_DIR="out"

# Auto-compile if class files are missing or sources are newer
if [ ! -f "$OUT_DIR/Main.class" ] ||
   find src -name "*.java" -newer "$OUT_DIR/Main.class" | grep -q .; then
    echo "🔨  Sources changed – recompiling…"
    ./compile.sh
fi

echo "🍓  Starting Fruit Collector Game…"
echo ""

# -cp out          tells Java where to find the compiled .class files
# -Dfile.encoding=UTF-8  ensures emoji display correctly on all platforms
java -cp "$OUT_DIR" -Dfile.encoding=UTF-8 Main
