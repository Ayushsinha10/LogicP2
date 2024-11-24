#!/bin/bash

# Set Java executable and classpath
JAVA_EXEC="java"
SRC_DIR="src"
BIN_DIR="bin"
MAIN_CLASS="interpreter.Main"

# Ensure the bin directory exists
if [ ! -d "$BIN_DIR" ]; then
   
   
    mkdir -p "$BIN_DIR"
    javac -d "$BIN_DIR" -sourcepath "$SRC_DIR" $(find "$SRC_DIR" -name "*.java")
fi

# Run the program
if [ $# -lt 1 ]; then
    echo "Usage: ./run.sh <filepath> [--trace]"
    exit 1
fi

$JAVA_EXEC -cp "$BIN_DIR" "$MAIN_CLASS" "$@"

