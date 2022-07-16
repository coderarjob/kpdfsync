#!/bin/sh
# -----------------------------------------------------------------------------
# Compiles .java files and version string creator script               kpdfsync
# -----------------------------------------------------------------------------
# This script is responsible for taking the source code and creating a complete
# runnable program.
#
# Creates file system and then builds kpdfsync. It creates the version string
# for each of the packages and puts them to their respective app.settings
# files. It packs the resource files for each of the packages in the end.
# -----------------------------------------------------------------------------

BIN_DIR=build/classes

mkdir -p $BIN_DIR
rm -rf $BIN_DIR/*

# -----------------------------------------------------------------------------
# Remove trailing spaces from java source files
# -----------------------------------------------------------------------------
find src -name "*.java" -exec sed -i s/\ \*$//g {} \; || exit

# -----------------------------------------------------------------------------
# Building
# -----------------------------------------------------------------------------
export CLASSPATH="lib/pdfclown.jar:$BIN_DIR"
JDK_VER_TARGET=8

# Build AJL
javac --release $JDK_VER_TARGET -Xlint -d "$BIN_DIR/" \
      src/coderarjob/ajl/file/*.java \
      src/coderarjob/ajl/*.java || exit

# Build Pattern Matcher
javac --release $JDK_VER_TARGET -Xlint -d "$BIN_DIR/" \
      src/coderarjob/kpdfsync/lib/pm/*.java || exit

# Build Annotator
javac --release $JDK_VER_TARGET -Xlint -d "$BIN_DIR/" \
      src/coderarjob/kpdfsync/lib/annotator/*.java || exit

# Build Kindle Clippings File Parser
javac --release $JDK_VER_TARGET -Xlint -d "$BIN_DIR/" \
      src/coderarjob/kpdfsync/lib/clipparser/*.java || exit

# Build kpdfsync library
javac --release $JDK_VER_TARGET -Xlint -d "$BIN_DIR/" \
      src/coderarjob/kpdfsync/lib/*.java || exit

# Build POC
javac --release $JDK_VER_TARGET -Xlint -d "$BIN_DIR/" \
      src/coderarjob/kpdfsync/poc/*.java \
      src/coderarjob/kpdfsync/poc/pdffixes/*.java \
      || exit

# -----------------------------------------------------------------------------
# Copy resources
# -----------------------------------------------------------------------------
cp -r src/coderarjob/ajl/res $BIN_DIR/coderarjob/ajl || exit
cp -r src/coderarjob/kpdfsync/lib/res $BIN_DIR/coderarjob/kpdfsync/lib || exit
cp -r src/coderarjob/kpdfsync/poc/res $BIN_DIR/coderarjob/kpdfsync/poc || exit

# -----------------------------------------------------------------------------
# Replace placeholder information in resource files.
# -----------------------------------------------------------------------------
VER=0.10.0
TAG=alpha
buildid=$(date +%y%m%d)

find $BIN_DIR -type f -name app.settings         \
    -exec sed -i "s/<build>/$buildid/g" {} \;    \
    -exec sed -i "s/<kpdfsync-ver>/$VER/g" {} \; \
    -exec sed -i "s/<tag>/$TAG/g" {} \; || exit

command -v git > /dev/null && (
    path=coderarjob/ajl
    commitid=$(git log --format="%h" -n 1 src/$path)
    find $BIN_DIR/$path -type f -name app.settings \
        -exec sed -i "s/<commitid>/$commitid/g" {} \; || exit

    path=coderarjob/kpdfsync/lib
    commitid=$(git log --format="%h" -n 1 src/$path)
    find $BIN_DIR/$path -type f -name app.settings \
        -exec sed -i "s/<commitid>/$commitid/g" {} \; || exit

    path=coderarjob/kpdfsync/poc
    commitid=$(git log --format="%h" -n 1 src/$path)
    find $BIN_DIR/$path -type f -name app.settings \
        -exec sed -i "s/<commitid>/$commitid/g" {} \; || exit
) || exit

# -----------------------------------------------------------------------------
# Generate tags file
# -----------------------------------------------------------------------------
command -v ctags > /dev/null && (
  ctags --recurse ./src || exit
)

# -----------------------------------------------------------------------------
# Call pack.sh, to generate the jar files.
# -----------------------------------------------------------------------------
cd build
./pack.sh
cd -
