#!/usr/bin/env bash
# Build and run the Tetris game. Requires JDK 17 or newer.
set -euo pipefail
cd "$(dirname "$0")"

if command -v mvn >/dev/null 2>&1; then
  mvn -q -DskipTests package
  exec java -jar target/tetris.jar "$@"
fi

if ! command -v javac >/dev/null 2>&1; then
  echo "JDK 17+ is required (javac was not found)." >&2
  exit 1
fi

rm -rf build/classes
mkdir -p build/classes
find src/main/java -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -release 17 -d build/classes
exec java -cp build/classes tetris.Main "$@"
