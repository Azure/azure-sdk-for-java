# Copy generated implementation classes to main source tree for review/versioning

$srcDir = $PSScriptRoot + "/target/generated-sources/io/clientcore/annotation/processor/test/"
$destDir = $PSScriptRoot + "/src/main/java/io/clientcore/annotation/processor/test/"

if (!(Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir | Out-Null
}
Copy-Item -Path $srcDir -Destination $destDir -Filter *.java -Force -Recurse
Write-Host "Copied generated impl classes to $destDir"

