$repoRoot = Resolve-Path "${PSScriptRoot}..\..\.."
$inputDir = Join-Path ${PSScriptRoot} "inputDir"
$outputDir = Join-Path ${PSScriptRoot} "outputDir"
$versionClientFileName = "version_client.txt"
$pomFileName = "pom.xml"
$defaultVersionClientFilePath = Join-Path $inputDir $versionClientFileName
$defaultPomFilePath = Join-Path $inputDir $pomFileName
$versionClientFilePath = Join-Path $repoRoot "eng" "versioning" $versionClientFileName
$bomPomFilePath = Join-Path $repoRoot "sdk" "containerregistry" "azure-containers-containerregistry" $pomFileName

if(! (Test-Path $inputDir)) { 
  New-Item -Path $PSScriptRoot -Name "inputDir" -ItemType "directory"
}

if(! (Test-Path $defaultVersionClientFilePath)) {
 Copy-Item $versionClientFilePath -Destination $inputDir
}

if(! (Test-Path $defaultPomFilePath)) {
 Copy-Item $bomPomFilePath -Destination $inputDir
}
  

"mvn exec:java -Dexec.args='-inputDir=$inputDir -outputDir=$outputDir -mode=analyze'"
if($LASTEXITCODE -ne 0) {
  LogError "Failed to generate the BOM."
  exit 1
}