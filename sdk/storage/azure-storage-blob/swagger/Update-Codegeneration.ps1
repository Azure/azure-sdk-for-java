$invokeScript = (Resolve-Path (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1)).Path

Invoke-Expression "$invokeScript -Directory $PSScriptRoot"
