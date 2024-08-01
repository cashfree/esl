#!/bin/sh
set -e
FILE="esl-1.0.0.jar"
if test -f "$FILE"; then
    echo "$FILE already exists; using that."
else
    echo "Please download the jar file from '<location of your esl jar which developers can download from>'"
    exit 1
fi
if [ "$SERVICE" = "" ]; then
    echo "Please set environment variable 'SERVICE' to the directory of 'environments.esl'"
    exit 1
else
    echo "Processing $SERVICE"
fi
REPO=https://repo.maven.apache.org/maven2
mkdir -p lib
cd lib
if [ ! -f "groovy-4.0.10.jar" ]; then
    curl -O $REPO/org/apache/groovy/groovy/4.0.10/groovy-4.0.10.jar
fi
if [ ! -f "groovy-json-4.0.10.jar" ]; then
    curl -O $REPO/org/apache/groovy/groovy-json/4.0.10/groovy-json-4.0.10.jar
fi
if [ ! -f "groovy-yaml-4.0.10.jar" ]; then
    curl -O $REPO/org/apache/groovy/groovy-yaml/4.0.10/groovy-yaml-4.0.10.jar
fi
if [ ! -f "slf4j-api-1.7.30.jar" ]; then
    curl -O $REPO/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar
fi
if [ ! -f "jackson-annotations-2.14.2.jar" ]; then
    curl -O $REPO/com/fasterxml/jackson/core/jackson-annotations/2.14.2/jackson-annotations-2.14.2.jar
fi
if [ ! -f "jackson-core-2.14.2.jar" ]; then
    curl -O $REPO/com/fasterxml/jackson/core/jackson-core/2.14.2/jackson-core-2.14.2.jar
fi
if [ ! -f "jackson-databind-2.14.2.jar" ]; then
    curl -O $REPO/com/fasterxml/jackson/core/jackson-databind/2.14.2/jackson-databind-2.14.2.jar
fi
if [ ! -f "jackson-dataformat-yaml-2.14.2.jar" ]; then
    curl -O $REPO/com/fasterxml/jackson/dataformat/jackson-dataformat-yaml/2.14.2/jackson-dataformat-yaml-2.14.2.jar
fi
if [ ! -f "snakeyaml-1.33.jar" ]; then
    curl -O $REPO/org/yaml/snakeyaml/1.33/snakeyaml-1.33.jar
fi
cd ..

ARCH=$(arch)
OS=$(uname -s)

java_exists=0
mvn_exists=0
check_java_installed() {
    exists=$(java --version)
    java_exists=$?
    echo "Using $(which java)"
}

install_java() {
    check_java_installed
    if [ "$java_exists" = "0" ]; then
        return
    fi
    if [ "$ARCH" = "x86_64" ]; then
        ARCH="x64"
    fi
    if [ "$OS" = "Linux" ]; then
        OS="linux"
    fi
    if [ "$OS" = "Darwin" ]; then
        OS="macos"
        if [ "$ARCH" = "arm64" ]; then
            ARCH="aarch64"
        fi
    fi
    FILE="jdk-17_${OS}-${ARCH}_bin.tar.gz"
    if test -f "$FILE"; then
      echo "$FILE exists."
    else
      echo "Downloading JDK"
      wget -c --header "Cookie: oraclelicense=accept-securebackup-cookie" "https://download.oracle.com/java/17/latest/$FILE"
      tar -xvzf $FILE
    fi
    if [ "$OS" = "macos" ]; then
      export JAVA_HOME="$(pwd)/jdk-17.0.9.jdk/Contents/Home"
    else
      export JAVA_HOME="$(pwd)/jdk-17.0.9"
    fi
    echo "JAVA_HOME: $JAVA_HOME"
    export PATH=$JAVA_HOME/bin:$PATH
}

install_java
java -cp lib -cp esl-1.0.0.jar com.cashfree.esl.Main $@
