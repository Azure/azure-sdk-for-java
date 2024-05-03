& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=searchindex'
& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=searchservice'
