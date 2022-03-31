#!/bin/sh

# -----------------------------------------------------------------------------
# kpdfsync
# This script uses pdftocairo program to fix pdf errors of the source pdf file
# and creates a new pdf flie as output.
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# fault     - Prints an error message and exits with error code of 1
# #1        - (Optional) Error message
# -----------------------------------------------------------------------------
fault()
{
  if [ $# -gt 0 ]; then
    >&2 echo $1
  fi

  exit 1
}

# --------------------------
# Check argumets
# --------------------------
if [ $# -lt 2 ]; then
  fault "usage: pdftocairo.sh <source pdf> <output pdf>"
fi

# ---------------------------------
# Check if files provided are valid
# ---------------------------------
SOURCE_PDF_FILE=$1
OUTPUT_PDF_FILE=$2

if [ "$SOURCE_PDF_FILE" = "$OUTPUT_PDF_FILE" ]; then
  fault "Source and output pdf files must not be the same"
fi

if [ ! -e "$SOURCE_PDF_FILE" ]; then
  fault "Souce file is not present"
fi

# ---------------------------------
# Check availability
# ---------------------------------
command -v pdftocairo > /dev/null 2>&1 || fault "pdftocairo not found. Install 'poppler-utils' package."

# ---------------------------------
# Run pdftocairo
# ---------------------------------
>$2 pdftocairo -pdf "$SOURCE_PDF_FILE" "$OUTPUT_PDF_FILE" || fault
exit 0
