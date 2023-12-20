Import-Module Pester

BeforeAll { 
    . $PSScriptRoot/../Docs-Onboarding.ps1
}

Describe 'Get-java-PackageIdentity' { 
    It 'should return the package identity' { 
        $package = [ordered]@{
            Name = 'packageName'
            Group = 'packageGroup'
        }

        $packageIdentity = Get-java-PackageIdentity $package
        $packageIdentity | Should -Be 'packageGroup:packageName'
    }
}