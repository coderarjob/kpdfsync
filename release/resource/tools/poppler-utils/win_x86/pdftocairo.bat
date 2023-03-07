@echo off
:: ----------------------------------------------------------------------------
:: kpdfsync 
:: This script uses pdftocairo program to fix pdf errors of the source pdf file
:: and creates a new pdf flie as output.
:: ----------------------------------------------------------------------------

:: ----------------------------
:: Check validity of arguments
:: ----------------------------
set argc=0
call:acount argc %*
if %argc% lss 2 (
	call:error "pdftocairo.sh <source pdf> <output pdf>"
	goto:fail
)

:: ----------------------------------
:: Check if files provided are valid.
:: ----------------------------------
set SOURCE_PDF_FILE=%1
set OUTPUT_PDF_FILE=%2

if %SOURCE_PDF_FILE%==%OUTPUT_PDF_FILE% (
	call:error "Source and output pdf files must not be the same."
	goto:fail
)	

if not exist %SOURCE_PDF_FILE% (
	call:error "Source file is not present"
	goto:fail
)

:: ----------------------------------
:: Check poppler-utils availability
:: ----------------------------------
set PDFTOCAIRO=".\tools\poppler-utils\win_x86\bin\pdftocairo.exe"
if not exist %PDFTOCAIRO% (
	call:error "%PDFTOCAIRO% file was not found"
	goto:fail
)

:: ----------------------------------
:: Check poppler-utils availability
:: ----------------------------------
>&2 %PDFTOCAIRO% -pdf %SOURCE_PDF_FILE% %OUTPUT_PDF_FILE%

if %ERRORLEVEL% neq 0 (
	goto:fail
)
exit /b 0

:: ----------------------------------------------------------------------------
:: Failure jump point
:: ----------------------------------------------------------------------------
:fail
exit /b 1

:: ----------------------------------------------------------------------------
:: acount 	- Returns the number of arguments passed to this function
:: %1		- name of variable where the output will be stored
:: %*		- Arguments. Count of which will be returned.
:: ----------------------------------------------------------------------------
:acount
set count=-1
set "ref=%~1"

:again
if not "%~1"=="%~2" (
	shift
	set /a count+=1
	goto :again
)
set "%ref%=%count%"
goto:eof

:: ----------------------------------------------------------------------------
:: error	- Prints an error message to stderr
:: %1		- Error message.
:: ----------------------------------------------------------------------------
:error
set argc=0
call :acount argc %*
if %argc% gtr 0 >&2 echo %*
goto:eof
