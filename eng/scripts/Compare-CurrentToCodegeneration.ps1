<#
.SYNOPSIS
Runs code generation for TypeSpec, based on configuration, and compares the generated code against
the state of the current codebase.

.DESCRIPTION
Runs code generation for TypeSpec, based on configuration, and compares the generated code against
the state of the current codebase.

If the regenerated code is different than the current codebase this will report the differences and exit with a failure
status.

.PARAMETER ServiceDirectories
The service directories that will be searched for 'tsp-location.yaml' files to
run code regeneration. If this parameter is not specified, the script will not check any directories and will exit
with a success status.

.PARAMETER Parallelization
The number of parallel jobs to run. The default is the number of processors on the machine. If unspecified or
less than 1, it will default to 1.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$ServiceDirectories,

  [Parameter(Mandatory = $false)]
  [int]$Parallelization = [Environment]::ProcessorCount
)

$sdkFolder = Join-Path -Path $PSScriptRoot ".." ".." "sdk"
$tspClientFolder = Join-Path -Path $PSScriptRoot ".." ".." "eng" "common" "tsp-client"

class GenerationInformation {
  # The directory where the library is located. Used for logging and validation.
  [string]$LibraryFolder

  # The path to the script that will perform the code generation.
  # This can be 'tsp-location.yaml' for TypeSpec.
  [string]$ScriptPath

  # The type of code generation this script performs, 'TypeSpec'.
  # This is used to determine actions to take based on the type of code generation.
  [ValidateSet('TypeSpec')]
  [string]$Type

  GenerationInformation([string]$libraryFolder, [string]$scriptPath, [string]$type) {
    $this.LibraryFolder = $libraryFolder
    $this.ScriptPath = $scriptPath
    $this.Type = $type
  }
}

function Find-GenerationInformation {
  param (
    [System.Collections.ArrayList]$GenerationInformations,
    [string]$LibraryFolder
  )

  $path = Join-Path -Path $sdkFolder $LibraryFolder

  if ($LibraryFolder.Contains("-v2")) {
    # Skip v2 libraries for TypeSpec regeneration as they are not supported.
    Write-Host "Skipping TypeSpec regeneration for v2 library: $LibraryFolder"
    return
  }

  # Search for 'tsp-location.yaml' script in the specified service directory.
  Get-ChildItem -Path $path -Filter "tsp-location.yaml" -Recurse | ForEach-Object {
    $GenerationInformations.Add([GenerationInformation]::new($path, $_, 'TypeSpec')) | Out-Null
  }
}

# No ServiceDirectories specified, no validation will be performed.
if (-not $ServiceDirectories) {
  Write-Host "No ServiceDirectories specified, no validation will be performed."
  exit 0
}

# If Parallelization is not specified or less than 1, set it to 1.
if ($Parallelization -lt 1) {
  $Parallelization = 1
}

# Normalize, deduplicate, and sort ServiceDirectories so top-level services are processed
# before specific libraries. This prevents duplicate work when both are provided.
# Then search for 'tsp-location.yaml' files in those directories and store the results in a
# list of GenerationInformation objects.
$generationInformations = New-Object 'Collections.ArrayList'
$orderedServiceDirectories = $ServiceDirectories.Split(',') |
  ForEach-Object { $_.Trim().Trim('/') } |
  Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
  Sort-Object -Unique |
  Sort-Object @(
    @{ Expression = { ($_ -split '/').Count } },
    @{ Expression = { $_ } }
  )

$processedTopLevelServiceDirectories = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)

foreach ($serviceDirectory in $orderedServiceDirectories) {
  if ($serviceDirectory.Contains('/')) {
    # The service directory is a specific library, e.g., "communication/azure-communication-chat".
    # If the top-level service has already been processed, skip the specific library.
    $topLevelServiceDirectory = ($serviceDirectory -split '/')[0]
    if ($processedTopLevelServiceDirectories.Contains($topLevelServiceDirectory)) {
      Write-Host "Skipping '$serviceDirectory' because top-level service '$topLevelServiceDirectory' is already included."
      continue
    }

    Find-GenerationInformation $generationInformations $serviceDirectory
  } else {
    # The service directory is a top-level service, e.g., "storage".
    # Track it so specific libraries under it can be skipped if present in input.
    $processedTopLevelServiceDirectories.Add($serviceDirectory) | Out-Null

    # Search for all libraries under the service directory.
    $searchPath = Join-Path -Path $sdkFolder $serviceDirectory
    Get-ChildItem -Path $searchPath -Directory | ForEach-Object {
      Find-GenerationInformation $generationInformations "$serviceDirectory/$($_.Name)"
    }
  }
}

if ($generationInformations.Count -eq 0) {
  Write-Host "No TypeSpec generation files to regenerate in directories: $ServiceDirectories."
  exit 0
}

$output = (& npm --prefix "$tspClientFolder" ci 2>&1)
if ($LASTEXITCODE -ne 0) {
  Write-Error "Error installing @azure-tools/typespec-client-generator-cli`n$output"
  exit 1
}

$generateScript = {
  $separatorBar = "======================================"
  $directory = $_.LibraryFolder
  
  if ($_.Type -eq 'TypeSpec') {
    Push-Location $directory
    try {
      try {
        $generateOutput = (& npx --no --prefix "$using:tspClientFolder" tsp-client update 2>&1)
        if ($LastExitCode -ne 0) {
          Write-Host "$separatorBar`nError running TypeSpec regeneration in directory $directory`n$([String]::Join("`n", $generateOutput))`n$separatorBar"
          throw
        }
      } finally {
        # Sort by descending path length so deepest TempTypeSpecFiles copies are
        # removed first. An outer TempTypeSpecFiles can contain nested copies inside
        # node_modules/@azure-tools/<emitter>/TempTypeSpecFiles; without sorting,
        # the recursive removal of an outer match can wipe a path that a later
        # iteration still expects to exist. SilentlyContinue covers the residual
        # cross-root race where deletion order alone is not enough.
        Get-ChildItem -Path $directory -Filter TempTypeSpecFiles -Recurse -Directory |
          Sort-Object -Property { $_.FullName.Length } -Descending |
          ForEach-Object {
            Remove-Item -Path $_.FullName -Recurse -Force -ErrorAction SilentlyContinue | Out-Null
          }
      }

      # Update code snippets before comparing the diff
      $mvnOutput = (& mvn --no-transfer-progress codesnippet:update-codesnippet 2>&1)
      if ($LastExitCode -ne 0) {
        Write-Host "$separatorBar`nError updating TypeSpec codesnippets in directory $directory`n$([String]::Join("`n", $mvnOutput))`n$separatorBar"
        throw
      }
    } finally {
      Pop-Location
    }

    # prevent warning related to EOL differences which triggers an exception for some reason
    (& git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "$directory/*.java" ":(exclude)**/src/test/**" ":(exclude)**/src/samples/**" ":(exclude)**/src/main/**/implementation/**") | Out-Null

    if ($LastExitCode -ne 0) {
      $status = (git status -s "$directory" | Out-String)
      Write-Host "$separatorBar`nThe following TypeSpec files in directoy $directory are out of date`n$status`n$separatorBar"
      throw
    } else {
      Write-Host "$separatorBar`nSuccessfully ran TypeSpec update in directory with no diff $directory`n$separatorBar"
    }
  } else {
    Write-Host "$separatorBar`nUnknown generation type: $($_.Type), directory: $directory`n$separatorBar"
    throw
  }
}

# Timeout is set to 60 seconds per script.
$scriptTimeoutInSeconds = 60
$timeout = $scriptTimeoutInSeconds * $generationInformations.Count
# Ensure a minimum timeout of 5 times the script timeout
# This is for scenarios where there are only a few scripts to run. Some script with large TypeSpec source can take a few minutes.
$minimumTimeout = 5 * $scriptTimeoutInSeconds
if ($timeout -lt $minimumTimeout) {
  $timeout = $minimumTimeout
}

$job = $generationInformations | ForEach-Object -Parallel $generateScript -ThrottleLimit $Parallelization -AsJob

# Out-Null to suppress output information from the job and 2>$null to suppress any error messages from Receive-Job.
$job | Wait-Job -Timeout $timeout | Out-Null
$job | Receive-Job 2>$null | Out-Null

$jobTimeout = $job.State -eq 'Running'
$jobFailed = $job.State -eq 'Failed'
if ($jobTimeout) {
  Write-Host "The aggregated generate job timed out after $timeout seconds."
}

# Clean up generated code, so that next step will not be affected.
git reset --hard | Out-Null
git clean -fd . | Out-Null

exit $jobFailed -or $jobTimeout
