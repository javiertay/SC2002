#!/bin/bash

set -e

# Configuration
SOURCE_PATH="src"
OUTPUT_DIR="docs"
LIB_DIR="lib"
PACKAGES="controller model util view"

echo "Generating Javadoc..."
echo "Source path: $SOURCE_PATH"
echo "Output directory: $OUTPUT_DIR"
echo "Library directory: $LIB_DIR"

mkdir -p "$OUTPUT_DIR"

javadoc -d "$OUTPUT_DIR" \
  -sourcepath "$SOURCE_PATH" \
  -classpath "$LIB_DIR/*" \
  $PACKAGES

echo "Javadoc successfully generated in '$OUTPUT_DIR'"


# to run
# chmod +x javadoc.sh
# ./javadoc
# to view -> on terminal type "open docs" then double click on the html
