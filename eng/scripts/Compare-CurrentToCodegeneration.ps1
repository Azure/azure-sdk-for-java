<#
.SYNOPSIS
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code.

.DESCRIPTION
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code.

If the regenerated code is different than the current code this will tell the differences, the files the differences
are in, and exit with a failure status.

.PARAMETER Directory
The directory that will be searched for 'Update-Codegeneration.ps1' scripts. The default is the root directory of the
Azure SDK for Java repository. CI jobs should use the 'ServiceDirectory', such as /sdk/storage.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$ServiceDirectories
)

$SeparatorBars = "==========================================================================="

# Returns true if there's an error, false otherwise
function Compare-CurrentToCodegeneration {
  param(
    [Parameter(Mandatory=$true)]
    $ServiceDirectory
  )

  $swaggers = Get-ChildItem -Path $ServiceDirectory -Filter "Update-Codegeneration.ps1" -Recurse
  if ($swaggers.Count -eq 0) {
    Write-Host "$SeparatorBars"
    Write-Host "No Swagger files to regenerate for $ServiceDirectory"
    Write-Host "$SeparatorBars"
    return $false
  }


  Write-Host "$SeparatorBars"
  Write-Host "Invoking Autorest code regeneration for $ServiceDirectory"
  Write-Host "$SeparatorBars"

  foreach ($script in $swaggers) {
    Write-Host "Calling Invoke-Expression $($script.FullName)"
    (& $script.FullName) | Write-Host
  }

  Write-Host "$SeparatorBars"
  Write-Host "Verify no diff for $ServiceDirectory"
  Write-Host "$SeparatorBars"

  # prevent warning related to EOL differences which triggers an exception for some reason
  & git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "*.java"

  if ($LastExitCode -ne 0) {
    $status = git status -s | Out-String
    Write-Host "The following files in $ServiceDirectory are out of date:"
    Write-Host "$status"
    return $true
  }
  return $false
}

$hasError = $false

# If a list of ServiceDirectories was passed in, process the entire list otherwise
# pass in an empty string to verify everything
if ($ServiceDirectories) {
  foreach ($ServiceDirectory in $ServiceDirectories.Split(',')) {
    $path = "sdk/$ServiceDirectory"
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