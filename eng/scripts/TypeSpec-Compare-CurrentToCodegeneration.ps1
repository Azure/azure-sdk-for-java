<#yaml
.SYNOPSIS
Sync TypeSpec defintion and generate SDK for RPs under the input directory and have 'tsp-location.yaml'.

.DESCRIPTION
Sync TypeSpec defintion and generate SDK for RPs under the input directory and have 'tsp-location.yaml'.

If the regenerated code is different from current code, this will tell the files the differences
are in, and exit with a failure status.

.PARAMETER Directory
The directory that will be used to get 'tsp-location.yaml' and generate SDK. The default is the root directory of the
Azure SDK for Java repository. One can also input service directory like: /sdk/storage, sdk/anomalydetector/azure-ai-anomalydetector.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$Directory
)

function Reset-Repository {
  # Clean up generated code, so that next step will not be affected.
  git reset --hard
  git clean -fd $sdkPath
}

Write-Host "

===========================================
Installing typespec-client-generator-cli
===========================================

"

npm install -g @azure-tools/typespec-client-generator-cli

Write-Host "

===========================================
Invoking tsp-client update
===========================================

"

$failedSdk = $null
foreach ($tspLocationPath in (Get-ChildItem -Path $Directory -Filter "tsp-location.yaml" -Recurse)) {
  $sdkPath = (get-item $tspLocationPath).Directory.FullName
  Write-Host "Generate SDK for $sdkPath"
  Push-Location
  Set-Location -Path $sdkPath
  tsp-client update
  if ($LastExitCode -ne 0) {
    $failedSdk += $sdkPath
  }
  Pop-Location
}

if ($failedSdk.Length -gt 0) {
  Write-Host "Code generation failed for following modules: $failedSdk"
  Reset-Repository
  exit 1
}

Write-Host "

==============
Verify no diff
==============

"

# prevent warning related to EOL differences which triggers an exception for some reason
git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "*.java" ":(exclude)**/src/test/**" ":(exclude)**/src/samples/**" ":(exclude)**/src/main/**/implementation/**"

if ($LastExitCode -ne 0) {
  $status = git status -s | Out-String
  Write-Host "
The following files are out of date:
$status
"
  Reset-Repository
  exit 1
}

# Delete out TypeSpec temporary folders if they still exist.
Get-ChildItem -Path $Directory -Filter TempTypeSpecFiles -Recurse -Directory | ForEach-Object {
  Remove-Item -Path $_.FullName -Recurse -Force
}

Reset-Repository
