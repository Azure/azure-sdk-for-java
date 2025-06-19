Push-Location $PSScriptRoot
try {
    & autorest autorest.md --java --v4 --use=@autorest/java@4.0.16
} finally {
    Pop-Location
}
