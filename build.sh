#!/bin/sh

BIN_DIR=bin
rm -rf $BIN_DIR/*

export CLASSPATH="lib/:bin/"

# Build AJL
javac -d "$BIN_DIR/" src/coderarjob/ajl/file/*.java || exit

# Build Pattern Matcher
javac -d "$BIN_DIR/" src/coderarjob/kpdfsync/lib/pm/*.java || exit

# Build Kindle Clippings File Parser
javac -d "$BIN_DIR/" src/coderarjob/kpdfsync/lib/clipparser/*.java || exit

# Build kpdfsync library
javac -d "$BIN_DIR/" src/coderarjob/kpdfsync/lib/*.java || exit

# Build POC
javac -d "$BIN_DIR/" src/coderarjob/kpdfsync/poc/*.java || exit

# Run
java coderarjob.kpdfsync.poc.Main
