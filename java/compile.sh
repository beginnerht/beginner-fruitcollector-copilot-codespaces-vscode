#!/usr/bin/env bash
# compile.sh
# Compiles all Java source files for the Fruit Collector Game.
#
# Usage:
#   cd java
#   chmod +x compile.sh   (first time only)
#   ./compile.sh
#
# Output class files are placed in the  java/out/  directory.

set -e  # exit immediately if any command fails

# Move to the directory containing this script (safe regardless of where
# you run it from)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

SRC_DIR="src"
OUT_DIR="out"

echo "🍎  Fruit Collector – Compiling Java sources…"

# Create the output directory if it doesn't exist
mkdir -p "$OUT_DIR"

# Compile every .java file in src/, outputting .class files to out/
# -encoding UTF-8  ensures emoji in source files are handled correctly
javac -encoding UTF-8 -d "$OUT_DIR" "$SRC_DIR"/*.java

echo "✅  Compilation successful!  Class files are in: $OUT_DIR/"
echo "    Run the game with:  ./run.sh"
