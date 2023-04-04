& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=package-2021-04-30-Preview-searchindex'
& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=package-2021-04-30-Preview-searchservice'
