<#
.SYNOPSIS
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code and syncs TypeSpec defintion and generates SDK for RPs under the input directory and have 'tsp-location.yaml'.

.DESCRIPTION
Invokes all 'Update-Codegeneration.ps1' scripts found within the specified directory and compares it against current
code and syncs TypeSpec defintion and generates SDK for RPs under the input directory and have 'tsp-location.yaml'.

If the regenerated code is different than the current code this will tell the differences, the files the differences
are in, and exit with a failure status.

.PARAMETER Directory
The directory that will be searched for 'Update-Codegeneration.ps1' scripts and 'tsp-location.yaml'. The default is
the root directory of the Azure SDK for Java repository. One can also input service directory like:
'-Directory /sdk/storage' or '-Directory sdk/anomalydetector/azure-ai-anomalydetector'.
#>

param(
    [Parameter(Mandatory = $false)]
    [string]$Directory
)

function Reset-Repository {
    # Clean up generated code, so that next step will not be affected.
    git reset --hard
    git clean -fd .
}

$path = ""
if ($Directory) {
    $path = $Directory
}

$swaggers = Get-ChildItem -Path $path -Filter "Update-Codegeneration.ps1" -Recurse
$tspYamls = Get-ChildItem -Path $path -Filter "tsp-location.yaml" -Recurse
if ($swaggers.Count -eq 0 -and $tspYamls.Count -eq 0) {
    Write-Host "

===========================================
No Swagger or TypeSpec files to regenerate
===========================================

"
    exit 0
}

# Stores SDKs that failed to regenerate code successfully
$failedSdk = $null
if ($swaggers.Count -gt 0) {
    Write-Host "

===================================
Invoking Autorest code regeneration
===================================

"

    foreach ($script in $swaggers) {
        Invoke-Expression $script.FullName
        if ($LastExitCode -ne 0) {
            $failedSdk += $script.Directory.FullName
        }
    }
}

if ($tspYamls.Count -gt 0) {
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
    foreach ($tspLocationPath in $tpsYamls) {
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
Get-ChildItem -Path $path -Filter TempTypeSpecFiles -Recurse -Directory | ForEach-Object {
  Remove-Item -Path $_.FullName -Recurse -Force
}

Reset-Repository
