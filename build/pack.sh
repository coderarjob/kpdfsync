#!/bin/bash
# -----------------------------------------------------------------------------
# Package creation script                                              kpdfsync
# -----------------------------------------------------------------------------
# This script is responsible for packing all the java .class files,
# dependencies and scripts and place them in correct places.
#
# This placement also ensures, that when distributed on different systems,
# kpdfsync can be run using the two helper scripts, kpdfsync.sh and
# kpdfsync.bat.
# -----------------------------------------------------------------------------

echo :: Building packages

# -----------------------------------------------------------------------------
# Create destination folder
# -----------------------------------------------------------------------------
rm -rf dist
mkdir -p dist/bin

pushd classes

# -----------------------------------------------------------------------------
# Create .jar files for individual packages.
# -----------------------------------------------------------------------------

# Package Arjob's Java Library
echo :: Building ajl.jar
jar cf ajl.jar coderarjob/ajl || exit

# Package kpdfsync library
echo :: Building libkpdfsync.jar
jar cf libkpdfsync.jar coderarjob/kpdfsync/lib || exit

# Package kpdfsync gui
echo :: Building kpdfsync.jar
jar cfm kpdfsync.jar ../Manifest.txt coderarjob/kpdfsync/poc || exit
popd

# -----------------------------------------------------------------------------
# Move .jar files and scripts to proper places under the dist folder.
# -----------------------------------------------------------------------------

# Move the jar files to dist/bin folder.
mv classes/*.jar ./dist/bin
cp ../lib/pdfclown.jar ./dist/bin
cp -r ../tools ./dist/bin

# Move the kpdfsync.sh and kpdfsync.bat to dist folder.
cp kpdfsync.sh ./dist
cp kpdfsync.bat ./dist

echo :: Building packages completed
