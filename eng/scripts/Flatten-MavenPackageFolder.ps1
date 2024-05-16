#Requires -Version 7
param(
  [Parameter(Mandatory=$true)][string]$Path
)

Set-StrictMode -Version 2.0

$ErrorActionPreference = "Stop"

$files = Get-ChildItem -Path $Path -Recurse -File -Force -ErrorAction Stop

$paths = @{}

foreach($file in $files) {
    $name = $file.Name

    # Skip maven-metadata.xml files. These are generated for each package and are not needed by ESRP.
    if($name -match "^maven-metadata\.xml(\..*)?$") {
        continue
    }

    if($paths.Keys -contains $name) {
        Write-Error "Duplicate file name: $name`n  $($paths[$name]) and $($file.FullName)"
        Write-Error "Unable to flatten: $Path"
        exit 1
    }

    $paths[$name] = $file.FullName
}

# Move the files to the root of the directory.
foreach($name in $paths.Keys) {
    $oldPath = $paths[$name]
    $newPath = Join-Path -Path $Path -ChildPath $name
    Move-Item -Path $oldPath -Destination $newPath -Force -ErrorAction Stop
}

$dirs = Get-ChildItem -Path $Path -Directory -Force -ErrorAction Stop

# Remove all child directories.
foreach($dir in $dirs) {
    Remove-Item $dir.FullName -Force -Recurse -ErrorAction Stop
}

exit 0
