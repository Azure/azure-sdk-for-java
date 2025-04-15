# Copy generated implementation classes to main source tree for review/versioning

$srcDir = "target/generated-sources//io/clientcore/annotation/processor/test/"
$destDir = "src/main/java/io/clientcore/annotation/processor/test/"

if (!(Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir | Out-Null
}
Copy-Item "$srcDir\*.java" $destDir -Force
Write-Host "Copied generated impl classes to $destDir"

