:: -----------------------------------------------------------------------------
:: Windows Start script                                                 kpdfsync
:: -----------------------------------------------------------------------------
:: Changes directory to the application path and runs kpdfsync
:: -----------------------------------------------------------------------------
@echo off
set BASEDIR=%~dp0
start /D "%BASEDIR%\bin" /B java -jar coderarjob.kpdfsync.jar
