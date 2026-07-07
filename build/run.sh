#!/usr/bin/env bash
# Runs Command Launcher (build.sh first if out/ doesn't exist)
set -e
cd "$(dirname "$0")"
if [ ! -d out ]; then
  ./build.sh
fi
java -cp out launcher.LauncherApp
