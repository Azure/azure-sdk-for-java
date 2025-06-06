<#
.SYNOPSIS
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code.

.DESCRIPTION
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code.

If the regenerated code is different than the current code this will tell the differences, the files the differences
are in, and exit with a failure status.

.PARAMETER ScanDirectories
A comma-separated list of directories that will be scanned for'Update-Codegeneration.ps1' scripts. If nothing is passed
the default behavior is a no-op. CI jobs should determine which directories to scan based either on the 'ServiceDirectory',
such as /sdk/storage, of the ci.yml being used or for Pull Request pipelines determine all directories based on what files
were changed.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$ScanDirectories
)

$SeparatorBars = "==========================================================================="

# Returns true if there's an error, false otherwise
function Compare-CurrentToCodegeneration {
  param(
    [Parameter(Mandatory=$true)]
    $ScanDirectory
  )

  $swaggers = Get-ChildItem -Path $ScanDirectory -Filter "Update-Codegeneration.ps1" -Recurse
  if ($swaggers.Count -eq 0) {
    Write-Host "$SeparatorBars"
    Write-Host "No Swagger files to regenerate for $ScanDirectory"
    Write-Host "$SeparatorBars"
    return $false
  }


  Write-Host "$SeparatorBars"
  Write-Host "Invoking Autorest code regeneration for $ScanDirectory"
  Write-Host "$SeparatorBars"

  foreach ($script in $swaggers) {
    Write-Host "Calling Invoke-Expression $($script.FullName)"
    (& $script.FullName) | Write-Host
  }

  Write-Host "$SeparatorBars"
  Write-Host "Verify no diff for $ScanDirectory"
  Write-Host "$SeparatorBars"

  # prevent warning related to EOL differences which triggers an exception for some reason
  & git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "*.java"

  if ($LastExitCode -ne 0) {
    $status = git status -s | Out-String
    Write-Host "The following files in $ScanDirectory are out of date:"
    Write-Host "$status"
    return $true
  }
  return $false
}

$hasError = $false

# If a list of ScanDirectories was passed in, process the entire list otherwise
# pass in an empty string to verify everything
if ($ScanDirectories) {
  foreach ($ScanDirectory in $ScanDirectories.Split(',')) {
    $path = "sdk/$ScanDirectory"
    $result = Compare-CurrentToCodegeneration $path
    if ($result) {
      $hasError = $true
    }
  }
} else {
  Write-Host "The service directory list was empty for this PR, no Swagger files check"
}

if ($hasError) {
  exit 1
}
exit 0
