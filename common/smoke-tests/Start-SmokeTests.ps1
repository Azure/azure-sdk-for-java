
$env:AZURE_LOG_LEVEL=4

mvn `
    -f common/smoke-tests/pom.xml `
    versions:use-latest-versions `
    --batch-mode `
    --fail-at-end `
    --no-transfer-progress `
    -DskipTests `
    "-Dincludes=com.azure:*"

mvn `
    -f common/smoke-tests/pom.xml `
    package `
    --batch-mode `
    --fail-at-end `
    --no-transfer-progress `
    -DskipTests


mvn `
    -f common/smoke-tests/pom.xml `
    exec:java `
    --batch-mode `
    --fail-at-end `
    --no-transfer-progress `
    -DskipTests `
    '-Dexec.mainClass="com.azure.App"'

