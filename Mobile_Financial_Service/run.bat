@echo off
REM run.bat - run app with MySQL connector on classpath
java -cp "bin;lib\mysql-connector-java-8.0.20.jar" app.SwingMain
pause