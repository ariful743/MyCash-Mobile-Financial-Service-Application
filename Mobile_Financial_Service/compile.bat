@echo off
REM compile.bat - compile all Java sources into bin\
IF EXIST bin ( rmdir /s /q bin )
mkdir bin
FOR /R src %%f IN (*.java) DO javac -d bin "%%f"
echo Compilation finished.
pause