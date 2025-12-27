#!/bin/bash

# Set JAVA_HOME explicitly for this script
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Print Java version for verification
echo "Using Java:"
$JAVA_HOME/bin/java -version

# Run the build with the correct Java version
./gradlew build --info