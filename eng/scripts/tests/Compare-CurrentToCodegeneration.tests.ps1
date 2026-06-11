# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:compareCurrentToCodegenerationScript = Join-Path $PSScriptRoot ".." "Compare-CurrentToCodegeneration.ps1"
}

Describe "Compare-CurrentToCodegeneration.ps1" {
    It "does not expose the removed RegenerationType parameter" {
        $command = Get-Command $script:compareCurrentToCodegenerationScript

        $command.Parameters.Keys | Should -Contain "ServiceDirectories"
        $command.Parameters.Keys | Should -Contain "Parallelization"
        $command.Parameters.Keys | Should -Not -Contain "RegenerationType"
    }

    It "returns success when no service directories are provided" {
        $result = & pwsh -NoProfile -File $script:compareCurrentToCodegenerationScript 2>&1
        $output = $result | ForEach-Object { $_.ToString() }

        $LASTEXITCODE | Should -Be 0
        $output | Should -Contain "No ServiceDirectories specified, no validation will be performed."
    }
}
