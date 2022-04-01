#!/bin/sh
# -----------------------------------------------------------------------------
# Unix Start script                                                    kpdfsync
# -----------------------------------------------------------------------------
# Changes directory to the application path and runs kpdfsync
# -----------------------------------------------------------------------------
SELF=$0
LINK=$(readlink $0) && SELF=$LINK
BASEDIR=$(dirname "$SELF")
cd "$BASEDIR/bin"
java -jar kpdfsync.jar&
