<#
.SYNOPSIS
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code.

.DESCRIPTION
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code.

If the regenerated code is different than the current code this will tell the differences, the files the differences
are in, and exit with a failure status.

.PARAMETER ServiceDirectories
The directories that will be searched for 'Update-Codegeneration.ps1' scripts. If this parameter is not specified,
the script will not check any directories and will exit with a success status.

.PARAMETER Parallelization
The number of parallel jobs to run. The default is the number of processors on the machine.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$ServiceDirectories,

  [Parameter(Mandatory = $false)]
  [int]$Parallelization = [Environment]::ProcessorCount
)

if (-not $ServiceDirectories) {
  Write-Host "No ServiceDirectories specified, no validation will be performed."
  exit 0
}

if ($Parallelization -lt 1) {
  $Parallelization = 1
}

$global:hasError = $false

$directoryAndScriptTuples = New-Object 'Collections.ArrayList'
foreach ($serviceDirectory in $ServiceDirectories.Split(',')) {
  if ($serviceDirectory.Contains('/')) {
    # The service directory is a specific library, e.g., "communication/azure-communication-chat"
    # Search the directory directly for an "Update-Codegeneration.ps1" script.
    $path = "sdk/$serviceDirectory"
    Get-ChildItem -Path $path -Filter "Update-Codegeneration.ps1" -Recurse | ForEach-Object {
      $directoryAndScriptTuples.Add([Tuple]::Create($path, $_)) | Out-Null
    }
  } else {
    # The service directory is a top-level service, e.g., "storage"
    # Search for all libraries under the service directory.
    foreach ($libraryFolder in Get-ChildItem -Path "sdk/$serviceDirectory" -Directory) {
      $path = "sdk/$serviceDirectory/$($libraryFolder.Name)"
      Get-ChildItem -Path $path -Filter "Update-Codegeneration.ps1" -Recurse | ForEach-Object {
        $directoryAndScriptTuples.Add([Tuple]::Create($path, $_)) | Out-Null
      }
    }
  }
}

if ($directoryAndScriptTuples.Count -eq 0) {
  Write-Host @"
======================================
No Swagger files to regenerate in directories: $ServiceDirectories.
======================================
"@
  exit 0
}

# Ensure Autorest is installed.
npm install -g autorest
if ($LASTEXITCODE -ne 0) {
  Write-Error "Failed to install Autorest."
  exit 1
}
  
$generateScript = {
  $directory = $_.Item1
  $updateCodegenScript = $_.Item2

  $generateOutput = (& $updateCodegenScript.FullName) 

  if ($LastExitCode -ne 0) {
    Write-Host @"
======================================
Error running $updateCodegenScript
======================================
$([String]::Join("`n", $generateOutput))
"@
    $global:hasError = $true
  } else {
    Write-Host @"
======================================
Successfully ran $updateCodegenScript
======================================
"@

    # prevent warning related to EOL differences which triggers an exception for some reason
    & git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "$directory/*.java"

    if ($LastExitCode -ne 0) {
      $status = git status -s | Out-String
      Write-Host @"
======================================
The following files in directoy $directory are out of date:
======================================
$status
"@
      Write-Host "$status"
      return $global:hasError = $true
    }
  }
}

# Timeout is set to 60 seconds per script.
$timeout = 60 * $directoryAndScriptTuples.Count

$job = $directoryAndScriptTuples | ForEach-Object -Parallel $generateScript -ThrottleLimit $Parallelization -AsJob

$job | Wait-Job -Timeout $timeout
$job | Receive-Job

if ($global:hasError) {
  exit 1
}
exit 0