#!/bin/bash

echo :: Building packages

rm -rf dist
mkdir dist

pushd build/classes

jar cfm kpdfsync.jar ../../Manifest.txt \
                     coderarjob 
popd

mv build/classes/kpdfsync.jar ./dist/
cp -v lib/pdfclown.jar ./dist/
cp -v -r ./tools ./dist

echo :: Building packages completed

