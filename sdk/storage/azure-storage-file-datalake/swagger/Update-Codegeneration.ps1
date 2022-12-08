npm install -s -g autorest

$originalLocation = (Get-Location).Path
Set-Location -Path $PSScriptRoot

autorest

Set-Location -Path $originalLocation
