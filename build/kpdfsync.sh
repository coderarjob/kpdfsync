#!/bin/sh
# -----------------------------------------------------------------------------
# Unix Start script                                                    kpdfsync
# -----------------------------------------------------------------------------
# Changes directory to the application path and runs kpdfsync
# -----------------------------------------------------------------------------
BASEDIR=$(dirname $0)
cd "$BASEDIR/bin"
java -jar kpdfsync.jar&
