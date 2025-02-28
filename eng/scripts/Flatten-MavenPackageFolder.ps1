#Requires -Version 7
# The SignedDirectory is the packages-esrp-gpg-signed directory. The contents of
# the SignedDirectory are copied to the FlattenedDirectory and then the directory
# is flattened. The FlattenedDirectory is only needed for ESRP signing's bulk
# release. The SignedDirectory will needed for feed publishing.
param(
  [Parameter(Mandatory=$true)][string]$SignedDirectory,
  [Parameter(Mandatory=$true)][string]$FlattenedDirectory
)

Set-StrictMode -Version 2.0

$ErrorActionPreference = "Stop"

$SignedDirectory = Resolve-Path $SignedDirectory
$FlattenedDirectory = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($FlattenedDirectory)

Write-Host "Copying $SignedDirectory to $FlattenedDirectory to flatten"
Copy-Item -Path $SignedDirectory -Destination $FlattenedDirectory -Recurse

$files = Get-ChildItem -Path $FlattenedDirectory -Recurse -File -Force -ErrorAction Stop

$paths = @{}

foreach($file in $files) {
    $name = $file.Name

    # Skip maven-metadata.xml files. These are generated for each package and are not needed by ESRP.
    if($name -match "^maven-metadata\.xml(\..*)?$") {
        continue
    }

    if($paths.Keys -contains $name) {
        Write-Error "Duplicate file name: $name`n  $($paths[$name]) and $($file.FullName)"
        Write-Error "Unable to flatten: $FlattenedDirectory"
        exit 1
    }

    $paths[$name] = $file.FullName
}

# Move the files to the root of the directory.
foreach($name in $paths.Keys) {
    $oldPath = $paths[$name]
    $newPath = Join-Path -Path $FlattenedDirectory -ChildPath $name
    Move-Item -Path $oldPath -Destination $newPath -Force -ErrorAction Stop
}

$dirs = Get-ChildItem -Path $FlattenedDirectory -Directory -Force -ErrorAction Stop

# Remove all child directories.
foreach($dir in $dirs) {
    Remove-Item $dir.FullName -Force -Recurse -ErrorAction Stop
}

exit 0
