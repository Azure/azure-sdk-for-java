& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=rbac'
& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=backuprestore'
& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=settings'
