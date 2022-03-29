#!/bin/bash

echo :: Building packages

rm -rf dist
mkdir dist

pushd classes

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

mv classes/*.jar ./dist/
cp ../lib/pdfclown.jar ./dist/
cp -r ../tools ./dist

echo :: Building packages completed

