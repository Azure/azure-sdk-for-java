<#yaml
.SYNOPSIS
Sync TypeSpec defintion and generate SDK for RPs under the input service directories and have 'tsp-location.yaml'.

.DESCRIPTION
Sync TypeSpec defintion and generate SDK for RPs under the input service directories and have 'tsp-location.yaml'.

If the regenerated code is different from current code, this will tell the files the differences
are in, and exit with a failure status.

.PARAMETER ServiceDirectories
The service directories that will be used to get 'tsp-location.yaml' and generate SDK. If this parameter is not specified,
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

$directoryAndScriptTuples = New-Object 'Collections.ArrayList'
foreach ($serviceDirectory in $ServiceDirectories.Split(',')) {
  if ($serviceDirectory.Contains("-v2")) {
    Write-Host @"
======================================
ServiceDirectory is V2 which isn't supported at this time: $serviceDirectory
======================================
"@
    continue
  }

  if ($serviceDirectory.Contains('/')) {
    # The service directory is a specific library, e.g., "communication/azure-communication-jobrouter"
    # Search the directory directly for an "tsp-location.yaml" script.
    $path = "sdk/$serviceDirectory"
    Get-ChildItem -Path $path -Filter "tsp-location.yaml" -Recurse | ForEach-Object {
      $directoryAndScriptTuples.Add($_.Directory.FullName) | Out-Null
    }
  } else {
    # The service directory is a top-level service, e.g., "keyvault"
    # Search for all libraries under the service directory.
    foreach ($libraryFolder in Get-ChildItem -Path "sdk/$serviceDirectory" -Directory) {
      $path = "sdk/$serviceDirectory/$($libraryFolder.Name)"
      Get-ChildItem -Path $path -Filter "tsp-location.yaml" -Recurse | ForEach-Object {
        $directoryAndScriptTuples.Add($_.Directory.FullName) | Out-Null
      }
    }
  }
}

if ($directoryAndScriptTuples.Count -eq 0) {
  Write-Host @"
======================================
No TypeSpec files to regenerate in directories: $ServiceDirectories.
======================================
"@
  exit 0
}

$output = (& npm install -g @azure-tools/typespec-client-generator-cli) 2>&1
if ($LastExitCode -ne 0) {
  Write-Host "Error installing @azure-tools/typespec-client-generator-cli"
  Write-Host "$output"
  exit 1
}

$generateScript = {
  $directory = $_

  Push-Location $directory
  try {
    $generateOutput = (& tsp-client update)
    if ($LastExitCode -ne 0) {
      Write-Host @"
======================================
Error running tsp-client update in directory $directory
======================================
$([String]::Join("`n", $generateOutput))
"@
      throw
    }

    # Update code snippets before comparing the diff
    $mvnOutput = (& mvn --no-transfer-progress codesnippet:update-codesnippet)
    if ($LastExitCode -ne 0) {
      Write-Host @"
======================================
Error updating codesnippets
======================================
$([String]::Join("`n", $mvnOutput))
"@
      throw
    }
  } finally {
    Pop-Location
  }
  
  # prevent warning related to EOL differences which triggers an exception for some reason
  (& git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "$directory/*.java" ":(exclude)**/src/test/**" ":
(exclude)**/src/samples/**" ":(exclude)**/src/main/**/implementation/**" ":(exclude)**/src/main/**/resourcemanager/**/*Manager.java") | Out-Null

  if ($LastExitCode -ne 0) {
    $status = (git status -s "$directory" | Out-String)
    Write-Host @"
======================================
The following files in directoy $directory are out of date:
======================================
$status
"@
    throw
  } else {
        Write-Host @"
======================================
Successfully ran tsp-client update in directory with no diff $directory
======================================
"@
  }
}

# Timeout is set to 60 seconds per script.
$timeout = 60 * $directoryAndScriptTuples.Count

$job = $directoryAndScriptTuples | ForEach-Object -Parallel $generateScript -ThrottleLimit $Parallelization -AsJob

# Out-Null to suppress output information from the job and 2>$null to suppress any error messages from Receive-Job.
$job | Wait-Job -Timeout $timeout | Out-Null
$job | Receive-Job 2>$null | Out-Null

# Clean up generated code, so that next step will not be affected.
# git reset --hard | Out-Null
# git clean -fd . | Out-Null

exit $job.State -eq 'Failed'
