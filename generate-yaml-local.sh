#!/bin/sh
#set -e
ARCH=$(arch)
OS=$(uname -s)

java_exists=0
mvn_exists=0
check_java_installed() {
    exists=$(java --version)
    java_exists=$?
    echo "Using $(which java)"
}

check_mvn_installed() {
    exists=$(mvn --version)
    mvn_exists=$?
    echo "Using $(which mvn)"
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

install_maven() {
    check_mvn_installed
    if [ "$mvn_exists" = "0" ]; then
        return
    fi
    FILE="apache-maven-3.9.5"
    if [ -d "$FILE" ]; then
      echo "$FILE exists."
    else
      echo "Downloading maven"
      wget "https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/$FILE-bin.tar.gz"
      tar -xvzf $FILE
    fi
    export MVN_HOME="$(pwd)/$FILE"
    echo "MVN_HOME: $MVN_HOME"
    export PATH=$MVN_HOME/bin:$PATH
}

install_java
install_maven
mvn package
java -cp target/lib -cp target/esl-1.0.0.jar com.cashfree.esl.Main $@