& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=formrecognizer-v2.1'
& (Join-Path $PSScriptRoot ".." ".." ".." ".." eng scripts Invoke-Codegeneration.ps1) -Directory $PSScriptRoot -AutorestOptions '--tag=formrecognizer-documentanalysis'
