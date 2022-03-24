#!/bin/bash

echo :: Building packages

rm -rf dist
mkdir dist

pushd build/classes

# Create testplib.jar
# BasicParser is what that can change. So it is packaged separately.
jar cfm kpdfsync.jar ../../Manifest.txt \
                     coderarjob 
popd

mv build/classes/kpdfsync.jar ./dist/
cp lib/pdfclown.jar ./dist/


echo :: Building packages completed

