#- task: PowerShell@2
#displayName: Fetch package list from JavaDoc jar
#inputs:
#  pwsh: true
#  filePath: ${{ parameters.WorkingDirectory }}/eng/scripts/Fetch-PackageList-Javadoc.ps1
#  arguments: >
#    -JavaDocJarLocation ${{ parameters.JavaDocJarLocation }}
#    -ArtifactName ${{ parameters.ArtifactName }}
#    -DocRepoLocation ${{ parameters.DocRepoLocation }}
#continueOnError: true
# Use case:
# Given the root of the maven .m2 cache folder, clean any Azure artifacts (aka. things built
# as part of this repository) out of it.
# MavenCacheFolder - The root of the maven cache folder. Most likely would be the
#                    $(MAVEN_CACHE_FOLDER) environment variable.
#
param(
  [Parameter(Mandatory=$true)][string]$MavenCacheFolder
)

$StartTime = $(get-date)

# Any new subdirectories to clean would be added here.
$cacheSubdirsToClean = ("/com/azure", "/com/microsoft/azure")

foreach ($cacheSubDir in $cacheSubdirsToClean) {
    $fullPathToClean = Join-Path -Path $MavenCacheFolder -ChildPath $cacheSubDir
    Write-Host "Cleaning $fullPathToClean"
    Remove-Item $fullPathToClean -Recurse -ErrorAction Ignore
}

Write-Host "POM files left in the cache folder"
Get-ChildItem $MavenCacheFolder -Recurse -Include *.pom | Select-Object Name

$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Host "Total run time=$($TotalRunTime)"
