#Requires -Version 7

param(
  [Parameter(Mandatory=$true)][string]$Path
)

Set-StrictMode -Version 2.0

$ErrorActionPreference = "Stop"

$files = Get-ChildItem -Path $Path -Recurse -File -Force -ErrorAction Stop
$names = @{}

foreach($file in $files) {
    $name = $file.Name
    if($names.Keys -contains $name) {
        Write-Error "Duplicate file name: $name`n  $($names[$name]) and $($file.FullName)"
        Write-Error "Unable to flatten: $Path"
        exit 1
    }
    $names[$name] = $file.FullName
}

foreach($file in $files) {
    $newPath = Join-Path -Path $Path -ChildPath $file.Name
    Move-Item -Path $file.FullName -Destination $newPath -Force -ErrorAction Stop
}

$dirs = Get-ChildItem -Path $Path -Directory -Force -ErrorAction Stop
foreach($dir in $dirs) {
    Remove-Item $dir.FullName -Force -Recurse -ErrorAction Stop
}

exit 0
