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
DIST_DIR=kpdfsync
rm -rf $DIST_DIR       || exit
mkdir -p $DIST_DIR/bin || exit

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
# Move .jar files and scripts to proper places under the kpdfsync dist folder.
# -----------------------------------------------------------------------------

# Move the jar files to kpdfsync/bin folder.
mv classes/*.jar ./$DIST_DIR/bin        || exit
cp ../lib/pdfclown.jar ./$DIST_DIR/bin  || exit
cp -r ../tools ./$DIST_DIR/bin          || exit

# Copy the kpdfsync.sh and kpdfsync.bat to kpdfsync dist folder.
cp kpdfsync.sh ./$DIST_DIR/kpdfsync     || exit
cp kpdfsync.bat ./$DIST_DIR             || exit

# Copy LICENSE Readme.md and HowTo.pdf to kpdfsync dist folder.
cp ../docs/QuickReference.pdf ./$DIST_DIR || exit
cp ../README.md ./$DIST_DIR               || exit
cp ../LICENSE ./$DIST_DIR                 || exit

echo :: Building packages completed
