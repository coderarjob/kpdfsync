#!/bin/sh

BIN_DIR=build/classes

mkdir -p $BIN_DIR
rm -rf $BIN_DIR/*

# Remove trailing spaces from java source files
find src -name "*.java" -exec sed -i s/\ \*$//g {} \; || exit

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

# Copy resources
cp -r src/coderarjob/ajl/res $BIN_DIR/coderarjob/ajl || exit
cp -r src/coderarjob/kpdfsync/lib/res $BIN_DIR/coderarjob/kpdfsync/lib || exit
cp -r src/coderarjob/kpdfsync/poc/res $BIN_DIR/coderarjob/kpdfsync/poc || exit

# Replace placeholder information in resource files.
buildid=$(date +%y%m%d)
find $BIN_DIR -type f -name app.settings \
    -exec sed -i "s/<build>/$buildid/g" {} \;

command -v git && (
    path=coderarjob/ajl
    commitid=$(git log --format="%h" -n 1 src/$path)
    find $BIN_DIR/$path -type f -name app.settings \
        -exec sed -i "s/<commitid>/$commitid/g" {} \;

    path=coderarjob/kpdfsync/lib
    commitid=$(git log --format="%h" -n 1 src/$path)
    find $BIN_DIR/$path -type f -name app.settings \
        -exec sed -i "s/<commitid>/$commitid/g" {} \;

    path=coderarjob/kpdfsync/poc
    commitid=$(git log --format="%h" -n 1 src/$path)
    find $BIN_DIR/$path -type f -name app.settings \
        -exec sed -i "s/<commitid>/$commitid/g" {} \;
) || exit

# Generate tags file
ctags --recurse ./src || exit

# Run
GTKLOOK="com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
MOTIFLOOK="com.sun.java.swing.plaf.motif.MotifLookAndFeel"
METALLOOK="javax.swing.plaf.metal.MetalLookAndFeel"
WINLOOK="com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
NIMBUSLLOOK="com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
LOOK=$METALLOOK
#java -Dswing.defaultlaf=$LOOK \
java coderarjob.kpdfsync.poc.Main
