<#
.SYNOPSIS
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory.

.DESCRIPTION
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory.

The regenerated code is then compared for differences to ensure that the code generation files are up-to-date.

.PARAMETER Directory
The directory that will be searched for 'Update-Codegeneration.ps1' scripts. The default is the root directory of the
Azure SDK for Java repository. CI jobs should use the 'ServiceDirectory', such as /sdk/storage.
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

foreach ($script in (Get-ChildItem -Path $path -Filter "Update-Codegeneration.ps1" -Recurse)) {
  Invoke-Expression $script.FullName
}

Write-Host "

==============
Verify no diff
==============

"

# prevent warning related to EOL differences which triggers an exception for some reason
& git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code

if ($LastExitCode -ne 0) {
  $status = git status -s | Out-String
  Write-Host "
The following files are out of date:
$status
"
  exit 1
}
