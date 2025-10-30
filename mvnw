#!/bin/sh
BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  mkdir -p "$BASE_DIR/.mvn/wrapper"
  curl -s -L "$(grep -E '^wrapperUrl=' "$WRAPPER_PROPERTIES" | cut -d'=' -f2)" -o "$WRAPPER_JAR"
fi
JAVA_CMD="java"
exec "$JAVA_CMD" -jar "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$BASE_DIR" $MAVEN_OPTS -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
