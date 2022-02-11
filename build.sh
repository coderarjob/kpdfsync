#!/bin/sh

BIN_DIR=bin
rm -rf $BIN_DIR/*

# Remove trailing spaces from java source files
find src -name "*.java" -exec sed -i s/\ \*$//g {} \; || exit

export CLASSPATH="lib/pdfclown.jar:bin/"

# Build AJL
javac -Xlint -d "$BIN_DIR/" src/coderarjob/ajl/file/*.java || exit

# Build Pattern Matcher
javac -Xlint -d "$BIN_DIR/" src/coderarjob/kpdfsync/lib/pm/*.java || exit

# Build Annotator
javac -Xlint -d "$BIN_DIR/" src/coderarjob/kpdfsync/lib/annotator/*.java || exit

# Build Kindle Clippings File Parser
javac -Xlint -d "$BIN_DIR/" src/coderarjob/kpdfsync/lib/clipparser/*.java || exit

# Build kpdfsync library
javac -Xlint -d "$BIN_DIR/" src/coderarjob/kpdfsync/lib/*.java || exit

# Build POC
javac -Xlint -d "$BIN_DIR/" src/coderarjob/kpdfsync/poc/*.java || exit

# Generate tags file
ctags --recurse ./src || exit

# Run
java coderarjob.kpdfsync.poc.Main
