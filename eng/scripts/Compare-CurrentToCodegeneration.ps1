<#
.SYNOPSIS
Runs code generation for either Swagger or TypeSpec, based on configuration, and compares the generated code against
the state of the current codebase.

.DESCRIPTION
Runs code generation for either Swagger or TypeSpec, based on configuration, and compares the generated code against
the state of the current codebase.

If the regenerated code is different than the current codebase this will report the differences and exit with a failure
status.

.PARAMETER ServiceDirectories
The service directories that will be searched for either 'Update-Codegeneration.ps1' or 'tsp-location.yaml' files to
run code regeneration. If this parameter is not specified, the script will not check any directories and will exit
with a success status.

.PARAMETER RegenerationType
The type of regeneration to perform. This can be 'All', 'Swagger', or 'TypeSpec'. If not specified, the script will use
'All' as the default, which means it will run both Swagger and TypeSpec code generation.

.PARAMETER Parallelization
The number of parallel jobs to run. The default is the number of processors on the machine. If unspecified or
less than 1, it will default to 1.
#>

param(
  [Parameter(Mandatory = $false)]
  [string]$ServiceDirectories,

  [Parameter(Mandatory = $false)]
  [ValidateSet('All', 'Swagger', 'TypeSpec')]
  [string]$RegenerationType = 'All',

  [Parameter(Mandatory = $false)]
  [int]$Parallelization = [Environment]::ProcessorCount
)

class GenerationInformation {
  # The directory where the library is located. Used for logging and validation.
  [string]$LibraryFolder

  # The path to the script that will perform the code generation.
  # This can be 'Update-Codegeneration.ps1' for Swagger or 'tsp-location.yaml' for TypeSpec.
  [string]$ScriptPath

  # The type of code generation this script performs, either 'Swagger' or 'TypeSpec'.
  # This is used to determine actions to take based on the type of code generation.
  [ValidateSet('Swagger', 'TypeSpec')]
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
    [string]$ServiceDirectory,
    [string]$RegenerationType
  )

  $path = "$PSScriptRoot/../../sdk/$ServiceDirectory"
  if ($RegenerationType -eq 'Swagger' -or $RegenerationType -eq 'All') {
    # Search for 'Update-Codegeneration.ps1' script in the specified service directory.
    Get-ChildItem -Path $path -Filter "Update-Codegeneration.ps1" -Recurse | ForEach-Object {
      $GenerationInformations.Add([GenerationInformation]::new($path, $_, 'Swagger')) | Out-Null
    }
  }

  if ($RegenerationType -eq 'TypeSpec' -or $RegenerationType -eq 'All') {
    # Search for 'tsp-location.yaml' script in the specified service directory.
    Get-ChildItem -Path $path -Filter "tsp-location.yaml" -Recurse | ForEach-Object {
      $GenerationInformations.Add([GenerationInformation]::new($path, $_, 'TypeSpec')) | Out-Null
    }
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

# Split the ServiceDirectories by comma and trim whitespace.
# Then search for 'Update-Codegeneration.ps1' or 'tsp-location.yaml' scripts in those directories,
# based on RegenerationType, and store the results in a list of GenerationInformation objects.
$generationInformations = New-Object 'Collections.ArrayList'
foreach ($serviceDirectory in $ServiceDirectories.Split(',')) {
  $serviceDirectory = $serviceDirectory.Trim()
  if ($serviceDirectory -match '\w+/\w+') {
    # The service directory is a specific library, e.g., "communication/azure-communication-chat"
    # Search the directory directly for an "Update-Codegeneration.ps1" script.
    Find-GenerationInformation $generationInformations $serviceDirectory $RegenerationType
  } else {
    # The service directory is a top-level service, e.g., "storage"
    # Search for all libraries under the service directory.
    foreach ($libraryFolder in Get-ChildItem -Path "$PSScriptRoot/../../sdk/$serviceDirectory" -Directory) {
      Find-GenerationInformation $generationInformations "$serviceDirectory/$($libraryFolder.Name)" $RegenerationType
    }
  }
}

if ($generationInformations.Count -eq 0) {
  $kind = $RegenerationType -eq 'All' ? 'Swagger or TypeSpec' : $RegenerationType
  Write-Host @"
======================================
No $kind generation files to regenerate in directories: $ServiceDirectories.
======================================
"@
  exit 0
}

if ($RegenerationType -eq 'Swagger' -or $RegenerationType -eq 'All') {
  # Ensure Autorest is installed.
  $output = (& npm install -g autorest 2>&1)
  if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to install Autorest for Swagger regeneration."
    Write-Error $output
    exit 1
  }
}

if ($RegenerationType -eq 'TypeSpec' -or $RegenerationType -eq 'All') {
  $output = (& npm install -g @azure-tools/typespec-client-generator-cli 2>&1)
  if ($LASTEXITCODE -ne 0) {
    Write-Error "Error installing @azure-tools/typespec-client-generator-cli"
    Write-Error "$output"
    exit 1
  }
}

$generateScript = {
  $directory = $_.LibraryFolder
  $updateCodegenScript = $_.ScriptPath

  if ($_.Type -eq 'Swagger') {
    # 6>&1 redirects Write-Host calls in the script to the output stream, so we can capture it.
    $generateOutput = (& $updateCodegenScript 6>&1)

    if ($LastExitCode -ne 0) {
      Write-Host @"
======================================
Error running Swagger regeneration $updateCodegenScript
======================================
$([String]::Join("`n", $generateOutput))
"@
      throw
    } else {
      # prevent warning related to EOL differences which triggers an exception for some reason
      (& git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "$directory/*.java") | Out-Null

      if ($LastExitCode -ne 0) {
        $status = (git status -s "$directory" | Out-String)
        Write-Host @"
======================================
The following Swagger generated files in directoy $directory are out of date:
======================================
$status
"@
        throw
      } else {
        Write-Host @"
======================================
Successfully ran Swagger regneration with no diff $updateCodegenScript
======================================
"@
      }
    }
  } elseif ($_.Type -eq 'TypeSpec') {
    Push-Location $Directory
    try {
      $generateOutput = (& tsp-client update 2>&1)
      if ($LastExitCode -ne 0) {
        Write-Host @"
======================================
Error running TypeSpec regeneration in directory $Directory
======================================
$([String]::Join("`n", $generateOutput))
"@
        throw
      }

      # Update code snippets before comparing the diff
      $mvnOutput = (& mvn --no-transfer-progress codesnippet:update-codesnippet 2>&1)
      if ($LastExitCode -ne 0) {
        Write-Host @"
======================================
Error updating TypeSpec codesnippets in directory $Directory
======================================
$([String]::Join("`n", $mvnOutput))
"@
        throw
      }
    } finally {
      Pop-Location
    }
    
    # prevent warning related to EOL differences which triggers an exception for some reason
    (& git -c core.safecrlf=false diff --ignore-space-at-eol --exit-code -- "$Directory/*.java" ":(exclude)**/src/test/**" ":
  (exclude)**/src/samples/**" ":(exclude)**/src/main/**/implementation/**" ":(exclude)**/src/main/**/resourcemanager/**/*Manager.java") | Out-Null

    if ($LastExitCode -ne 0) {
      $status = (git status -s "$Directory" | Out-String)
      Write-Host @"
======================================
The following TypeSpec files in directoy $Directory are out of date:
======================================
$status
"@
      throw
    } else {
      Write-Host @"
======================================
Successfully ran TypeSpec update in directory with no diff $Directory
======================================
"@
    }
  } else {
    Write-Error "Unknown generation type: $($_.Type), directory: $directory"
    throw
  }
}

# Timeout is set to 60 seconds per script.
$timeout = 60 * $generationInformations.Count

$job = $generationInformations | ForEach-Object -Parallel $generateScript -ThrottleLimit $Parallelization -AsJob

# Out-Null to suppress output information from the job and 2>$null to suppress any error messages from Receive-Job.
$job | Wait-Job -Timeout $timeout | Out-Null
$job | Receive-Job 2>$null | Out-Null

# Clean up generated code, so that next step will not be affected.
Get-ChildItem -Path "$PSScriptRoot/../../" -Filter TempTypeSpecFiles -Recurse -Directory | ForEach-Object {
  Remove-Item -Path $_.FullName -Recurse -Force | Out-Null
}
git reset --hard | Out-Null
git clean -fd . | Out-Null

exit $job.State -eq 'Failed'
