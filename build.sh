#!/usr/bin/env bash
# Compiles Command Launcher into ./out
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
rm sources.txt
echo "Build complete -> out/"
