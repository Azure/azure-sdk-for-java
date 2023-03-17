<#
.SYNOPSIS
Sync CADL defintion and generate SDK for RPs under the input directory and have 'cadl-location.yaml'.

.DESCRIPTION
Sync CADL defintion and generate SDK for RPs under the input directory and have 'cadl-location.yaml'.

If the regenerated code is different from current code, this will tell the files the differences
are in, and exit with a failure status.

.PARAMETER Directory
The directory that will be used to get 'cadl-location.yaml' and generate SDK. The default is the root directory of the
Azure SDK for Java repository. One can also input service directory like: /sdk/storage, sdk/anomalydetector/azure-ai-anomalydetector.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$Directory
)

$path = ""
if ($Directory) {
  $path = $Directory
}

Write-Host "

===================================
Invoking Autorest code regeneration
===================================

"


foreach ($cadlLocationPath in (Get-ChildItem -Path $path -Filter "cadl-location.yaml" -Recurse)) {
  $sdkPath = (get-item $cadlLocationPath).Directory.FullName
  Write-Host "Generate SDK for $sdkPath"
  ./eng/common/scripts/Cadl-Project-Sync.ps1 $sdkPath
  ./eng/common/scripts/Cadl-Project-Generate.ps1 $sdkPath
}

Write-Host "

==============
Verify no diff
==============

"

# prevent warning related to EOL differences which triggers an exception for some reason
& git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "*.java"

if ($LastExitCode -ne 0) {
  $status = git status -s | Out-String
  Write-Host "
The following files are out of date:
$status
"
  exit 1
}
