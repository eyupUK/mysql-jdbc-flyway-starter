@ECHO OFF
SETLOCAL
SET BASE_DIR=%~dp0
SET WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar
SET WRAPPER_PROPERTIES=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties
IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Downloading Maven Wrapper...
  mkdir "%BASE_DIR%\.mvn\wrapper" 2> NUL
  for /f "tokens=2 delims==" %%a in ('findstr /r "^wrapperUrl=" "%WRAPPER_PROPERTIES%"') do set URL=%%a
  powershell -Command "Invoke-WebRequest -Uri %URL% -OutFile %WRAPPER_JAR%"
)
SET JAVA_CMD=java
"%JAVA_CMD%" -jar "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
