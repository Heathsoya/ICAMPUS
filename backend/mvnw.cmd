@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_WRAPPER_DIR=%~dp0.mvn\wrapper
set MAVEN_WRAPPER_JAR=%MAVEN_WRAPPER_DIR%\maven-wrapper.jar
set MAVEN_USER_HOME=%MAVEN_WRAPPER_DIR%

if not exist "%MAVEN_WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%MAVEN_WRAPPER_JAR%'}"
)

set MAVEN_OPTS=-Xmx1024m

java -cp "%MAVEN_WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
