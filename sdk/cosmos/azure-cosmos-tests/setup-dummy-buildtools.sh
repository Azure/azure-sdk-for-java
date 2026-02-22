#!/bin/bash
# Create a dummy sdk-build-tools artifact so Maven's checkstyle plugin
# resolves its dependencies. The actual checkstyle check is skipped via
# -Dcheckstyle.skip=true, but Maven resolves deps before checking the flag.

DIR="/root/.m2/repository/com/azure/sdk-build-tools/1.0.0"
mkdir -p "$DIR"
cd "$DIR"

# Create minimal POM
cat > sdk-build-tools-1.0.0.pom << 'EOF'
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>sdk-build-tools</artifactId>
  <version>1.0.0</version>
</project>
EOF

# Create empty JAR
mkdir -p /tmp/empty-jar
jar cf sdk-build-tools-1.0.0.jar -C /tmp/empty-jar .
rm -rf /tmp/empty-jar

# Tell Maven this artifact doesn't need remote resolution
echo "sdk-build-tools-1.0.0.jar>=" > _remote.repositories
echo "sdk-build-tools-1.0.0.pom>=" >> _remote.repositories

echo "sdk-build-tools dummy artifact created:"
ls -la "$DIR"
