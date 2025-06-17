<#yaml
.SYNOPSIS
Sync TypeSpec defintion and generate SDK for RPs under the input directory and have 'tsp-location.yaml'.

.DESCRIPTION
Sync TypeSpec defintion and generate SDK for RPs under the input directory and have 'tsp-location.yaml'.

If the regenerated code is different from current code, this will tell the files the differences
are in, and exit with a failure status.

.PARAMETER ScanDirectories
A comma-separated list of directories that will be scanned for 'tsp-location.yaml'. If nothing is passed the default
behavior is a no-op. CI jobs should determine which directories to scan based either on the 'ServiceDirectory',
such as /sdk/storage, of the ci.yml being used or for Pull Request pipelines determine all directories based on what files
were changed.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$ScanDirectories
)

$SeparatorBars = "==========================================================================="

function Reset-Repository {
  # Clean up generated code, so that next step will not be affected.
  git reset --hard
  git clean -fd .
}

function Install-typespec-client-generator-cli {
  Write-Host "$SeparatorBars"
  Write-Host "Installing typespec-client-generator-cli"
  Write-Host "npm install -g @azure-tools/typespec-client-generator-cli"
  Write-Host "$SeparatorBars"

  $output = npm install -g @azure-tools/typespec-client-generator-cli | Out-String
  if ($LastExitCode -ne 0) {
    Write-Host "Error installing @azure-tools/typespec-client-generator-cli"
    Write-Host "$output"
    exit 1
  }
}

# Returns true if there's an error, false otherwise
function TypeSpec-Compare-CurrentToCodegeneration {
  param(
    [Parameter(Mandatory=$true)]
    $ScanDirectory
  )

  $tspYamls = Get-ChildItem -Path $ScanDirectory -Filter "tsp-location.yaml" -Recurse
  if ($tspYamls.Count -eq 0) {
    Write-Host "$SeparatorBars"
    Write-Host "No TypeSpec files to regenerate for $ScanDirectory"
    Write-Host "$SeparatorBars"
    return $false
  }

  Write-Host "$SeparatorBars"
  Write-Host "Invoking tsp-client update for tsp-location.yaml files in $ScanDirectory"
  Write-Host "$SeparatorBars"

  $failedSdk = $null
  foreach ($tspLocationPath in $tspYamls) {
    $sdkPath = (get-item $tspLocationPath).Directory.FullName
    Write-Host "Generate SDK for $sdkPath"
    Push-Location
    Set-Location -Path $sdkPath
    tsp-client update | Out-Null
    if ($LastExitCode -ne 0) {
      $failedSdk += $sdkPath
    }
    # Update code snippets before comparing the diff
    Write-Host "Update code snippets"
    mvn --no-transfer-progress codesnippet:update-codesnippet | Out-Null
    Pop-Location
  }
  if ($failedSdk.Length -gt 0) {
    Write-Host "Code generation failed for following modules: $failedSdk"
    return $true
  }

  Write-Host "$SeparatorBars"
  Write-Host "Verify no diff for TypeSpec generated files in $ScanDirectory"
  Write-Host "$SeparatorBars"

  # prevent warning related to EOL differences which triggers an exception for some reason
  git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "*.java" ":(exclude)**/src/test/**" ":
  (exclude)**/src/samples/**" ":(exclude)**/src/main/**/implementation/**" ":(exclude)**/src/main/**/resourcemanager/**/*Manager.java"

  if ($LastExitCode -ne 0) {
    $status = git status -s | Out-String
    Write-Host "The following files are out of date:"
    Write-Host "$status"
    return $true
  }

  # Delete out TypeSpec temporary folders if they still exist.
  Get-ChildItem -Path $ScanDirectory -Filter TempTypeSpecFiles -Recurse -Directory | ForEach-Object {
    Remove-Item -Path $_.FullName -Recurse -Force
  }
  return $false
}

$hasError = $false
if ($ScanDirectories) {
  Install-typespec-client-generator-cli
  foreach ($ScanDirectory in $ScanDirectories.Split(',')) {
    $path = "sdk/$ScanDirectory"
    $result = TypeSpec-Compare-CurrentToCodegeneration $path
    if ($result) {
      $hasError = $true
    }
    Reset-Repository
  }
} else {
  Write-Host "The service directory list was empty for this PR, no TypeSpec files to regenerate"
}
if ($hasError) {
  exit 1
}
exit 0