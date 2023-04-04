$env:AZURE_TEST_MODE="Live"

# Note: this script adapts these existing env vars to Java's expected format. Ensure these valuess are defined in your
# user-defined environment variables before running.
$env:AZURE_CLIENT_ID=$env:DEVCENTER_CLIENT_ID
$env:AZURE_CLIENT_SECRET=$env:DEVCENTER_CLIENT_SECRET
$env:AZURE_TENANT_ID=$env:DEVCENTER_TENANT_ID

mvn install -f .\pom.xml -Dgpg=skip -Drevapi=skip -Djacoco=skip
