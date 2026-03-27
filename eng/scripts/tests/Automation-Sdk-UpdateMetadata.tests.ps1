# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Pester tests for Automation-Sdk-UpdateMetadata.ps1

.DESCRIPTION
    This file contains unit tests for the metadata update automation functions
    using the Pester testing framework.

.NOTES
    How to run:
    1. Install Pester if not already installed:
       Install-Module Pester -Force -MinimumVersion 5.3.3
    
    2. Run the tests:
       Invoke-Pester ./Automation-Sdk-UpdateMetadata.tests.ps1
#>

BeforeAll {
    # Import YAML module for CI tests
    Import-Module powershell-yaml -ErrorAction Stop

    # Import metadata helper functions
    $helperPath = Join-Path $PSScriptRoot ".." "helpers" "Metadata-Helpers.ps1"
    . $helperPath

    # Create a test directory structure
    $script:TestRoot = Join-Path ([System.IO.Path]::GetTempPath()) "MetadataAutomationTests_$(New-Guid)"
    New-Item -ItemType Directory -Path $script:TestRoot -Force | Out-Null

    # Sample POM with direct groupId
    $script:SamplePomXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-network</artifactId>
    <version>2.0.0</version>
</project>
"@

    # Sample POM with groupId inherited from parent
    $script:SamplePomWithParentXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.azure</groupId>
        <artifactId>azure-client-sdk-parent</artifactId>
        <version>1.7.0</version>
    </parent>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.0.0</version>
</project>
"@

    # Sample service aggregator POM
    $script:SampleServicePomXml = @"
<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>azure-network-service</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version>

  <modules>
    <module>azure-resourcemanager-network</module>
  </modules>
</project>
"@

    # Sample root POM
    $script:SampleRootPomXml = @"
<project>
  <modules>
    <module>sdk/advisor</module>
    <module>sdk/network</module>
    <module>sdk/storage</module>
  </modules>
</project>
"@

    # Sample POM with profiles (as in the root pom)
    $script:SamplePomWithProfiles = @"
<project>
  <profiles>
    <profile>
      <id>default</id>
      <modules>
        <module>sdk/advisor</module>
        <module>sdk/storage</module>
      </modules>
    </profile>
    <profile>
      <id>other</id>
      <modules>
        <module>sdk/other</module>
      </modules>
    </profile>
  </profiles>
</project>
"@

    # Sample ci.yml with no parameters (simple artifact)
    $script:SampleCiYmlNoParams = @"
# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.

trigger:
  branches:
    include:
      - main
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/communication/azure-resourcemanager-communication/
    exclude:
      - sdk/communication/azure-resourcemanager-communication/pom.xml

pr:
  branches:
    include:
      - main
      - feature/*
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/communication/azure-resourcemanager-communication/
    exclude:
      - sdk/communication/azure-resourcemanager-communication/pom.xml

extends:
  template: /eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: communication/azure-resourcemanager-communication
    Artifacts:
      - name: azure-resourcemanager-communication
        groupId: com.azure.resourcemanager
        safeName: azureresourcemanagercommunication
"@

    # Sample ci.yml with release parameters
    $script:SampleCiYmlWithParams = @"
# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.

trigger:
  branches:
    include:
      - main
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/network/
    exclude:
      - sdk/network/pom.xml
      - sdk/network/azure-resourcemanager-network/pom.xml

pr:
  branches:
    include:
      - main
      - feature/*
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/network/
    exclude:
      - sdk/network/pom.xml
      - sdk/network/azure-resourcemanager-network/pom.xml

parameters:
- name: release_azureresourcemanagernetwork
  displayName: 'azure-resourcemanager-network'
  type: boolean
  default: false

extends:
  template: ../../eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: network
    Artifacts:
      - name: azure-resourcemanager-network
        groupId: com.azure.resourcemanager
        safeName: azureresourcemanagernetwork
        releaseInBatch: `${{ parameters.release_azureresourcemanagernetwork }}
"@

    # Sample ci.yml with SDKType=data
    $script:SampleCiYmlDataType = @"
# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.

trigger:
  branches:
    include:
      - main

extends:
  template: ../../eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: myservice
    SDKType: data
    Artifacts:
      - name: azure-myservice
        groupId: com.azure
        safeName: azuremyservice
"@
}

AfterAll {
    # Clean up test directory
    if (Test-Path $script:TestRoot) {
        Remove-Item -Path $script:TestRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# ============================================================================
# Get-ServiceAndModuleFromPath tests
# ============================================================================
Describe "Get-ServiceAndModuleFromPath" {
    It "Should extract service and module from Windows-style path" {
        $result = Get-ServiceAndModuleFromPath `
            -PackagePath "C:\repos\azure-sdk-for-java\sdk\network\azure-resourcemanager-network" `
            -SdkRepoPath "C:\repos\azure-sdk-for-java"

        $result.Service | Should -Be "network"
        $result.Module | Should -Be "azure-resourcemanager-network"
    }

    It "Should extract service and module from forward-slash path with different repo name" {
        $result = Get-ServiceAndModuleFromPath `
            -PackagePath "C:/repos/my-java-sdk/sdk/storage/azure-storage-blob" `
            -SdkRepoPath "C:/repos/my-java-sdk"

        $result.Service | Should -Be "storage"
        $result.Module | Should -Be "azure-storage-blob"
    }

    It "Should handle trailing slashes" {
        $result = Get-ServiceAndModuleFromPath `
            -PackagePath "C:\repos\azure-sdk-for-java\sdk\network\azure-resourcemanager-network\" `
            -SdkRepoPath "C:\repos\azure-sdk-for-java\"

        $result.Service | Should -Be "network"
        $result.Module | Should -Be "azure-resourcemanager-network"
    }

    It "Should throw when path is not under sdk/ directory" {
        { Get-ServiceAndModuleFromPath `
            -PackagePath "C:\repos\other-repo\lib\something" `
            -SdkRepoPath "C:\repos\azure-sdk-for-java" } | Should -Throw "*not under sdk/ directory*"
    }

    It "Should throw when path has insufficient parts" {
        { Get-ServiceAndModuleFromPath `
            -PackagePath "C:\repos\azure-sdk-for-java\sdk\network" `
            -SdkRepoPath "C:\repos\azure-sdk-for-java" } | Should -Throw "*Cannot determine service and module*"
    }
}

# ============================================================================
# Get-GroupIdFromPom tests
# ============================================================================
Describe "Get-GroupIdFromPom" {
    BeforeEach {
        $script:TestPomPath = Join-Path $script:TestRoot "pom.xml"
    }

    AfterEach {
        if (Test-Path $script:TestPomPath) {
            Remove-Item $script:TestPomPath -Force
        }
    }

    It "Should extract direct groupId" {
        Set-Content -Path $script:TestPomPath -Value $script:SamplePomXml
        $result = Get-GroupIdFromPom -PomPath $script:TestPomPath
        $result | Should -Be "com.azure.resourcemanager"
    }

    It "Should extract groupId from parent when not in project" {
        Set-Content -Path $script:TestPomPath -Value $script:SamplePomWithParentXml
        $result = Get-GroupIdFromPom -PomPath $script:TestPomPath
        $result | Should -Be "com.azure"
    }

    It "Should throw when groupId is missing entirely" {
        $invalidPom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <artifactId>some-artifact</artifactId>
</project>
"@
        Set-Content -Path $script:TestPomPath -Value $invalidPom
        { Get-GroupIdFromPom -PomPath $script:TestPomPath } | Should -Throw "*Could not extract groupId*"
    }
}

# ============================================================================
# Add-ModuleToModulesBlock tests
# ============================================================================
Describe "Add-ModuleToModulesBlock" {
    It "Should add a new module in sorted order" {
        $block = @"
  <modules>
    <module>sdk/advisor</module>
    <module>sdk/storage</module>
  </modules>
"@
        $result = Add-ModuleToModulesBlock -ModulesBlock $block -Module "sdk/network"
        $result | Should -Match "<module>sdk/advisor</module>"
        $result | Should -Match "<module>sdk/network</module>"
        $result | Should -Match "<module>sdk/storage</module>"

        # Verify order: advisor < network < storage
        $advisorIdx = $result.IndexOf("sdk/advisor")
        $networkIdx = $result.IndexOf("sdk/network")
        $storageIdx = $result.IndexOf("sdk/storage")
        $networkIdx | Should -BeGreaterThan $advisorIdx
        $storageIdx | Should -BeGreaterThan $networkIdx
    }

    It "Should not duplicate an existing module" {
        $block = @"
  <modules>
    <module>sdk/network</module>
    <module>sdk/storage</module>
  </modules>
"@
        $result = Add-ModuleToModulesBlock -ModulesBlock $block -Module "sdk/network"
        $count = ([regex]::Matches($result, 'sdk/network')).Count
        $count | Should -Be 1
    }

    It "Should preserve indentation from the source block" {
        $block = "    <modules>`n        <module>sdk/a</module>`n    </modules>"
        $result = Add-ModuleToModulesBlock -ModulesBlock $block -Module "sdk/b"
        # The closing </modules> had indent "    " (4 spaces), so module indent should be "      " (6 spaces)
        $result | Should -Match "      <module>sdk/b</module>"
    }
}

# ============================================================================
# Add-ModuleToPom tests
# ============================================================================
Describe "Add-ModuleToPom" {
    It "Should add module to a simple POM" {
        $pom = @"
<project>
  <modules>
    <module>azure-resourcemanager-network</module>
  </modules>
</project>
"@
        $result = Add-ModuleToPom -PomContent $pom -Module "azure-resourcemanager-compute"
        $result.Success | Should -Be $true
        $result.Content | Should -Match "<module>azure-resourcemanager-compute</module>"
        $result.Content | Should -Match "<module>azure-resourcemanager-network</module>"
    }

    It "Should skip when module already exists (idempotent)" {
        $pom = @"
<project>
  <modules>
    <module>azure-resourcemanager-network</module>
  </modules>
</project>
"@
        $result = Add-ModuleToPom -PomContent $pom -Module "azure-resourcemanager-network"
        $result.Success | Should -Be $true
        $result.Content | Should -Be $pom
    }

    It "Should add module to root POM style (sdk/ prefixed)" {
        $result = Add-ModuleToPom -PomContent $script:SampleRootPomXml -Module "sdk/newservice"
        $result.Success | Should -Be $true
        $result.Content | Should -Match "<module>sdk/newservice</module>"

        # Verify sorted order
        $networkIdx = $result.Content.IndexOf("sdk/network")
        $newserviceIdx = $result.Content.IndexOf("sdk/newservice")
        $storageIdx = $result.Content.IndexOf("sdk/storage")
        $newserviceIdx | Should -BeGreaterThan $networkIdx
        $storageIdx | Should -BeGreaterThan $newserviceIdx
    }

    It "Should fail when no <modules> block exists" {
        $pom = "<project><groupId>com.azure</groupId></project>"
        $result = Add-ModuleToPom -PomContent $pom -Module "new-module"
        $result.Success | Should -Be $false
    }

    It "Should fail when multiple <modules> blocks without profiles" {
        $pom = @"
<project>
  <modules>
    <module>a</module>
  </modules>
  <modules>
    <module>b</module>
  </modules>
</project>
"@
        $result = Add-ModuleToPom -PomContent $pom -Module "c"
        $result.Success | Should -Be $false
    }
}

# ============================================================================
# Add-ModuleToDefaultProfile tests
# ============================================================================
Describe "Add-ModuleToDefaultProfile" {
    It "Should add module to the default profile's modules" {
        $result = Add-ModuleToDefaultProfile -PomContent $script:SamplePomWithProfiles -Module "sdk/network"
        $result.Success | Should -Be $true
        $result.Content | Should -Match "<module>sdk/network</module>"

        # Verify it was added to the default profile, not the other
        # Check order in default profile: advisor < network < storage
        $defaultProfileMatch = [regex]::Match($result.Content, '(?s)<id>default</id>.*?</profile>')
        $defaultProfile = $defaultProfileMatch.Value
        $defaultProfile | Should -Match "<module>sdk/advisor</module>"
        $defaultProfile | Should -Match "<module>sdk/network</module>"
        $defaultProfile | Should -Match "<module>sdk/storage</module>"
    }

    It "Should not modify other profiles" {
        $result = Add-ModuleToDefaultProfile -PomContent $script:SamplePomWithProfiles -Module "sdk/network"
        $result.Success | Should -Be $true

        # The "other" profile should still only have sdk/other
        $otherProfileMatch = [regex]::Match($result.Content, '(?s)<id>other</id>.*?</profile>')
        $otherProfile = $otherProfileMatch.Value
        $otherProfile | Should -Not -Match "sdk/network"
        $otherProfile | Should -Match "sdk/other"
    }

    It "Should fail when no default profile exists" {
        $pom = @"
<project>
  <profiles>
    <profile>
      <id>custom</id>
      <modules>
        <module>sdk/a</module>
      </modules>
    </profile>
  </profiles>
</project>
"@
        $result = Add-ModuleToDefaultProfile -PomContent $pom -Module "sdk/b"
        $result.Success | Should -Be $false
    }
}

# ============================================================================
# Add-ModuleToPom with profiles tests
# ============================================================================
Describe "Add-ModuleToPom with profiles" {
    It "Should delegate to default profile when multiple <modules> and <profiles> exist" {
        $result = Add-ModuleToPom -PomContent $script:SamplePomWithProfiles -Module "sdk/network"
        $result.Success | Should -Be $true
        $result.Content | Should -Match "<module>sdk/network</module>"
    }
}

# ============================================================================
# Update-RootPom tests
# ============================================================================
Describe "Update-RootPom" {
    BeforeEach {
        $script:TestSdkRoot = Join-Path $script:TestRoot "sdk-root-$(New-Guid)"
        New-Item -ItemType Directory -Path $script:TestSdkRoot -Force | Out-Null
    }

    AfterEach {
        if (Test-Path $script:TestSdkRoot) {
            Remove-Item -Path $script:TestSdkRoot -Recurse -Force
        }
    }

    It "Should add new service module to root pom" {
        Set-Content -Path (Join-Path $script:TestSdkRoot "pom.xml") -Value $script:SampleRootPomXml

        Update-RootPom -SdkRepoPath $script:TestSdkRoot -Service "compute"

        $content = Get-Content (Join-Path $script:TestSdkRoot "pom.xml") -Raw
        $content | Should -Match "<module>sdk/compute</module>"
    }

    It "Should skip when service already exists" {
        Set-Content -Path (Join-Path $script:TestSdkRoot "pom.xml") -Value $script:SampleRootPomXml

        Update-RootPom -SdkRepoPath $script:TestSdkRoot -Service "network"

        $content = Get-Content (Join-Path $script:TestSdkRoot "pom.xml") -Raw
        $count = ([regex]::Matches($content, '<module>sdk/network</module>')).Count
        $count | Should -Be 1
    }

    It "Should not throw when root pom does not exist" {
        { Update-RootPom -SdkRepoPath $script:TestSdkRoot -Service "newservice" } | Should -Not -Throw
    }
}

# ============================================================================
# Update-ServicePom tests
# ============================================================================
Describe "Update-ServicePom" {
    BeforeEach {
        $script:TestSdkRoot = Join-Path $script:TestRoot "sdk-svc-$(New-Guid)"
        New-Item -ItemType Directory -Path (Join-Path $script:TestSdkRoot "sdk" "network") -Force | Out-Null
    }

    AfterEach {
        if (Test-Path $script:TestSdkRoot) {
            Remove-Item -Path $script:TestSdkRoot -Recurse -Force
        }
    }

    It "Should create new service pom when it does not exist" {
        Update-ServicePom -SdkRepoPath $script:TestSdkRoot -Service "newservice" -Module "azure-resourcemanager-newservice"

        $pomFile = Join-Path $script:TestSdkRoot "sdk" "newservice" "pom.xml"
        Test-Path $pomFile | Should -Be $true

        $content = Get-Content $pomFile -Raw
        $content | Should -Match "azure-newservice-service"
        $content | Should -Match "<module>azure-resourcemanager-newservice</module>"
    }

    It "Should add module to existing service pom" {
        $existingPom = Join-Path $script:TestSdkRoot "sdk" "network" "pom.xml"
        Set-Content -Path $existingPom -Value $script:SampleServicePomXml

        Update-ServicePom -SdkRepoPath $script:TestSdkRoot -Service "network" -Module "azure-resourcemanager-network-extra"

        $content = Get-Content $existingPom -Raw
        $content | Should -Match "<module>azure-resourcemanager-network</module>"
        $content | Should -Match "<module>azure-resourcemanager-network-extra</module>"
    }

    It "Should skip when module already exists in service pom" {
        $existingPom = Join-Path $script:TestSdkRoot "sdk" "network" "pom.xml"
        Set-Content -Path $existingPom -Value $script:SampleServicePomXml

        Update-ServicePom -SdkRepoPath $script:TestSdkRoot -Service "network" -Module "azure-resourcemanager-network"

        $content = Get-Content $existingPom -Raw
        $count = ([regex]::Matches($content, '<module>azure-resourcemanager-network</module>')).Count
        $count | Should -Be 1
    }
}

# ============================================================================
# Script integration tests
# ============================================================================
Describe "Script Integration" {
    It "Should verify the main script imports the helper correctly" {
        $scriptPath = Join-Path $PSScriptRoot ".." "Automation-Sdk-UpdateMetadata.ps1"
        $scriptContent = Get-Content $scriptPath -Raw

        $scriptContent | Should -Match '\. \$helperPath'
        $scriptContent | Should -Match 'Metadata-Helpers\.ps1'
    }

    It "Should verify the main script does not duplicate function definitions" {
        $scriptPath = Join-Path $PSScriptRoot ".." "Automation-Sdk-UpdateMetadata.ps1"
        $scriptContent = Get-Content $scriptPath -Raw

        $scriptContent | Should -Not -Match 'function Get-ServiceAndModuleFromPath'
        $scriptContent | Should -Not -Match 'function Add-ModuleToPom'
        $scriptContent | Should -Not -Match 'function Update-RootPom'
        $scriptContent | Should -Not -Match 'function Update-ServicePom'
    }

    It "Should verify the helper file exports all required functions" {
        $helperPath = Join-Path $PSScriptRoot ".." "helpers" "Metadata-Helpers.ps1"

        { . $helperPath } | Should -Not -Throw

        Get-Command Get-ServiceAndModuleFromPath -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Get-GroupIdFromPom -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Add-ModuleToModulesBlock -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Add-ModuleToPom -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Add-ModuleToDefaultProfile -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Update-RootPom -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Update-ServicePom -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command New-CiYmlContent -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Add-ArtifactToCiYml -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command ConvertTo-CiYmlString -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
        Get-Command Update-CiYml -ErrorAction SilentlyContinue | Should -Not -BeNullOrEmpty
    }

    It "Should verify swagger_to_sdk_config.json references the metadata script" {
        $configPath = Join-Path $PSScriptRoot ".." ".." "swagger_to_sdk_config.json"
        $config = Get-Content $configPath -Raw | ConvertFrom-Json

        $config.packageOptions.updateMetadataScript.path | Should -Be "./eng/scripts/Automation-Sdk-UpdateMetadata.ps1"
    }
}

# ============================================================================
# End-to-end: Case 1 - Existing service, new resourcemanager package
# ============================================================================
Describe "End-to-End: Existing Service, New Package" {
    BeforeEach {
        $script:E2ERoot = Join-Path $script:TestRoot "e2e-case1-$(New-Guid)"
        New-Item -ItemType Directory -Path $script:E2ERoot -Force | Out-Null

        # Set up SDK repo with existing service (already in root pom, has service pom)
        Set-Content -Path (Join-Path $script:E2ERoot "pom.xml") -Value $script:SampleRootPomXml
        $svcDir = Join-Path $script:E2ERoot "sdk" "network"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null
        Set-Content -Path (Join-Path $svcDir "pom.xml") -Value $script:SampleServicePomXml

        # Package directory exists
        $pkgDir = Join-Path $svcDir "azure-resourcemanager-network-extra"
        New-Item -ItemType Directory -Path $pkgDir -Force | Out-Null
        Set-Content -Path (Join-Path $pkgDir "pom.xml") -Value $script:SamplePomXml
    }

    AfterEach {
        if (Test-Path $script:E2ERoot) {
            Remove-Item -Path $script:E2ERoot -Recurse -Force
        }
    }

    It "Should update service pom but skip root pom" {
        $service = "network"
        $module = "azure-resourcemanager-network-extra"

        $rootPomBefore = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw

        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module

        # Root pom unchanged (service already existed)
        $rootPomAfter = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw
        $rootPomAfter | Should -Be $rootPomBefore

        # Service pom has the new module
        $svcPomContent = Get-Content (Join-Path $script:E2ERoot "sdk" "network" "pom.xml") -Raw
        $svcPomContent | Should -Match "<module>azure-resourcemanager-network</module>"
        $svcPomContent | Should -Match "<module>azure-resourcemanager-network-extra</module>"
    }

    It "Should be idempotent when run twice" {
        $service = "network"
        $module = "azure-resourcemanager-network-extra"

        # First run
        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module

        $rootPom1 = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw
        $svcPom1 = Get-Content (Join-Path $script:E2ERoot "sdk" "network" "pom.xml") -Raw

        # Second run
        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module

        $rootPom2 = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw
        $svcPom2 = Get-Content (Join-Path $script:E2ERoot "sdk" "network" "pom.xml") -Raw

        $rootPom2 | Should -Be $rootPom1
        $svcPom2 | Should -Be $svcPom1
    }
}

# ============================================================================
# End-to-end: Case 2 - Brand new service
# ============================================================================
Describe "End-to-End: New Service" {
    BeforeEach {
        $script:E2ERoot = Join-Path $script:TestRoot "e2e-case2-$(New-Guid)"
        New-Item -ItemType Directory -Path $script:E2ERoot -Force | Out-Null

        # Set up minimal SDK repo structure (root pom only, no service dir)
        Set-Content -Path (Join-Path $script:E2ERoot "pom.xml") -Value $script:SampleRootPomXml
        $pkgDir = Join-Path $script:E2ERoot "sdk" "compute" "azure-resourcemanager-compute"
        New-Item -ItemType Directory -Path $pkgDir -Force | Out-Null
        Set-Content -Path (Join-Path $pkgDir "pom.xml") -Value $script:SamplePomXml
    }

    AfterEach {
        if (Test-Path $script:E2ERoot) {
            Remove-Item -Path $script:E2ERoot -Recurse -Force
        }
    }

    It "Should update root pom and create service pom" {
        $service = "compute"
        $module = "azure-resourcemanager-compute"

        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module

        # Verify root pom has new service
        $rootPom = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw
        $rootPom | Should -Match "<module>sdk/compute</module>"

        # Verify service pom was created
        $svcPom = Join-Path $script:E2ERoot "sdk" "compute" "pom.xml"
        Test-Path $svcPom | Should -Be $true
        $svcPomContent = Get-Content $svcPom -Raw
        $svcPomContent | Should -Match "<module>azure-resourcemanager-compute</module>"
    }

    It "Should be idempotent when run twice" {
        $service = "compute"
        $module = "azure-resourcemanager-compute"

        # First run
        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module

        $rootPom1 = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw
        $svcPom1 = Get-Content (Join-Path $script:E2ERoot "sdk" "compute" "pom.xml") -Raw

        # Second run
        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module

        $rootPom2 = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw
        $svcPom2 = Get-Content (Join-Path $script:E2ERoot "sdk" "compute" "pom.xml") -Raw

        $rootPom2 | Should -Be $rootPom1
        $svcPom2 | Should -Be $svcPom1
    }
}

# ============================================================================
# New-CiYmlContent tests
# ============================================================================
Describe "New-CiYmlContent" {
    It "Should generate valid YAML with correct service and module" {
        $result = New-CiYmlContent -Service "network" -Module "azure-resourcemanager-network"

        $result | Should -Match "sdk/network/ci.yml"
        $result | Should -Match "sdk/network/azure-resourcemanager-network/"
        $result | Should -Match "sdk/network/azure-resourcemanager-network/pom.xml"
        $result | Should -Match "ServiceDirectory: network"
        $result | Should -Match "Artifacts: \[\]"
    }

    It "Should be parseable YAML" {
        $content = New-CiYmlContent -Service "compute" -Module "azure-resourcemanager-compute"
        $parsed = ConvertFrom-Yaml $content -Ordered
        $parsed["trigger"] | Should -Not -BeNullOrEmpty
        $parsed["pr"] | Should -Not -BeNullOrEmpty
        $parsed["extends"]["parameters"]["ServiceDirectory"] | Should -Be "compute"
    }
}

# ============================================================================
# Add-ArtifactToCiYml tests
# ============================================================================
Describe "Add-ArtifactToCiYml" {
    It "Should add artifact without releaseInBatch when no parameters exist" {
        $content = New-CiYmlContent -Service "newservice" -Module "azure-resourcemanager-newservice"
        $ciYml = ConvertFrom-Yaml $content -Ordered

        $result = Add-ArtifactToCiYml -CiYml $ciYml -Module "azure-resourcemanager-newservice" -GroupId "com.azure.resourcemanager"

        $result | Should -Be $true
        $artifacts = $ciYml["extends"]["parameters"]["Artifacts"]
        $artifacts.Count | Should -Be 1
        $artifacts[0]["name"] | Should -Be "azure-resourcemanager-newservice"
        $artifacts[0]["groupId"] | Should -Be "com.azure.resourcemanager"
        $artifacts[0]["safeName"] | Should -Be "azureresourcemanagernewservice"
        $artifacts[0].Keys | Should -Not -Contain "releaseInBatch"
    }

    It "Should add artifact with releaseInBatch when parameters list exists" {
        $ciYml = ConvertFrom-Yaml $script:SampleCiYmlWithParams -Ordered

        $result = Add-ArtifactToCiYml -CiYml $ciYml -Module "azure-resourcemanager-network-extra" -GroupId "com.azure.resourcemanager"

        $result | Should -Be $true
        $artifacts = $ciYml["extends"]["parameters"]["Artifacts"]
        $artifacts.Count | Should -Be 2
        $newArtifact = $artifacts[1]
        $newArtifact["name"] | Should -Be "azure-resourcemanager-network-extra"
        $newArtifact["releaseInBatch"] | Should -Not -BeNullOrEmpty

        # Check release parameter was added
        $params = $ciYml["parameters"]
        $params.Count | Should -Be 2
        $newParam = $params[1]
        $newParam["name"] | Should -Be "release_azureresourcemanagernetworkextra"
        $newParam["default"] | Should -Be $false  # management-plane
    }

    It "Should set release param default=true for data-plane packages" {
        $ciYml = ConvertFrom-Yaml $script:SampleCiYmlWithParams -Ordered

        $result = Add-ArtifactToCiYml -CiYml $ciYml -Module "azure-storage-blob" -GroupId "com.azure"

        $result | Should -Be $true
        $params = $ciYml["parameters"]
        $newParam = $params | Where-Object { $_["name"] -eq "release_azurestorageblob" }
        $newParam["default"] | Should -Be $true  # data-plane
    }

    It "Should skip when module already exists" {
        $ciYml = ConvertFrom-Yaml $script:SampleCiYmlWithParams -Ordered

        $result = Add-ArtifactToCiYml -CiYml $ciYml -Module "azure-resourcemanager-network" -GroupId "com.azure.resourcemanager"

        $result | Should -Be $false
        $ciYml["extends"]["parameters"]["Artifacts"].Count | Should -Be 1
    }

    It "Should return false for unexpected format" {
        $ciYml = [ordered]@{ "trigger" = @{} }

        $result = Add-ArtifactToCiYml -CiYml $ciYml -Module "azure-test" -GroupId "com.azure"

        $result | Should -Be $false
    }
}

# ============================================================================
# Update-CiYml tests
# ============================================================================
Describe "Update-CiYml" {
    BeforeEach {
        $script:CiTestRoot = Join-Path $script:TestRoot "ci-test-$(New-Guid)"
        New-Item -ItemType Directory -Path $script:CiTestRoot -Force | Out-Null
    }

    AfterEach {
        if (Test-Path $script:CiTestRoot) {
            Remove-Item -Path $script:CiTestRoot -Recurse -Force
        }
    }

    It "Should create ci.yml when it does not exist" {
        $svcDir = Join-Path $script:CiTestRoot "sdk" "newservice"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null

        Update-CiYml -SdkRepoPath $script:CiTestRoot -Service "newservice" -Module "azure-resourcemanager-newservice" -GroupId "com.azure.resourcemanager"

        $ciFile = Join-Path $svcDir "ci.yml"
        Test-Path $ciFile | Should -Be $true

        $content = Get-Content $ciFile -Raw
        $content | Should -Match "azure-resourcemanager-newservice"
        $content | Should -Match "com.azure.resourcemanager"
        $content | Should -Match "azureresourcemanagernewservice"
    }

    It "Should add artifact to existing ci.yml without parameters" {
        $svcDir = Join-Path $script:CiTestRoot "sdk" "communication"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null
        Set-Content -Path (Join-Path $svcDir "ci.yml") -Value $script:SampleCiYmlNoParams

        Update-CiYml -SdkRepoPath $script:CiTestRoot -Service "communication" -Module "azure-resourcemanager-communication-extra" -GroupId "com.azure.resourcemanager"

        $content = Get-Content (Join-Path $svcDir "ci.yml") -Raw
        $content | Should -Match "azure-resourcemanager-communication"
        $content | Should -Match "azure-resourcemanager-communication-extra"
    }

    It "Should add artifact to existing ci.yml with release parameters" {
        $svcDir = Join-Path $script:CiTestRoot "sdk" "network"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null
        Set-Content -Path (Join-Path $svcDir "ci.yml") -Value $script:SampleCiYmlWithParams

        Update-CiYml -SdkRepoPath $script:CiTestRoot -Service "network" -Module "azure-resourcemanager-network-extra" -GroupId "com.azure.resourcemanager"

        $content = Get-Content (Join-Path $svcDir "ci.yml") -Raw
        $content | Should -Match "azure-resourcemanager-network-extra"
        $content | Should -Match "release_azureresourcemanagernetworkextra"
    }

    It "Should skip when module already exists in ci.yml" {
        $svcDir = Join-Path $script:CiTestRoot "sdk" "network"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null
        Set-Content -Path (Join-Path $svcDir "ci.yml") -Value $script:SampleCiYmlWithParams

        $before = Get-Content (Join-Path $svcDir "ci.yml") -Raw

        Update-CiYml -SdkRepoPath $script:CiTestRoot -Service "network" -Module "azure-resourcemanager-network" -GroupId "com.azure.resourcemanager"

        $after = Get-Content (Join-Path $svcDir "ci.yml") -Raw
        $after | Should -Be $before
    }

    It "Should rename ci.yml to ci.data.yml when SDKType=data and create new ci.yml" {
        $svcDir = Join-Path $script:CiTestRoot "sdk" "myservice"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null
        Set-Content -Path (Join-Path $svcDir "ci.yml") -Value $script:SampleCiYmlDataType

        Update-CiYml -SdkRepoPath $script:CiTestRoot -Service "myservice" -Module "azure-resourcemanager-myservice" -GroupId "com.azure.resourcemanager"

        # ci.data.yml should exist
        Test-Path (Join-Path $svcDir "ci.data.yml") | Should -Be $true

        # New ci.yml should have the new module
        $content = Get-Content (Join-Path $svcDir "ci.yml") -Raw
        $content | Should -Match "azure-resourcemanager-myservice"
        $content | Should -Not -Match "SDKType"
    }

    It "Should be idempotent when run twice" {
        $svcDir = Join-Path $script:CiTestRoot "sdk" "network"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null
        Set-Content -Path (Join-Path $svcDir "ci.yml") -Value $script:SampleCiYmlWithParams

        Update-CiYml -SdkRepoPath $script:CiTestRoot -Service "network" -Module "azure-resourcemanager-network-extra" -GroupId "com.azure.resourcemanager"
        $content1 = Get-Content (Join-Path $svcDir "ci.yml") -Raw

        Update-CiYml -SdkRepoPath $script:CiTestRoot -Service "network" -Module "azure-resourcemanager-network-extra" -GroupId "com.azure.resourcemanager"
        $content2 = Get-Content (Join-Path $svcDir "ci.yml") -Raw

        $content2 | Should -Be $content1
    }
}

# ============================================================================
# End-to-end: CI.yml with POM - Existing service, new package
# ============================================================================
Describe "End-to-End: Existing Service with CI update" {
    BeforeEach {
        $script:E2ERoot = Join-Path $script:TestRoot "e2e-ci-case1-$(New-Guid)"
        New-Item -ItemType Directory -Path $script:E2ERoot -Force | Out-Null

        Set-Content -Path (Join-Path $script:E2ERoot "pom.xml") -Value $script:SampleRootPomXml
        $svcDir = Join-Path $script:E2ERoot "sdk" "network"
        New-Item -ItemType Directory -Path $svcDir -Force | Out-Null
        Set-Content -Path (Join-Path $svcDir "pom.xml") -Value $script:SampleServicePomXml
        Set-Content -Path (Join-Path $svcDir "ci.yml") -Value $script:SampleCiYmlWithParams

        $pkgDir = Join-Path $svcDir "azure-resourcemanager-network-extra"
        New-Item -ItemType Directory -Path $pkgDir -Force | Out-Null
        Set-Content -Path (Join-Path $pkgDir "pom.xml") -Value $script:SamplePomXml
    }

    AfterEach {
        if (Test-Path $script:E2ERoot) {
            Remove-Item -Path $script:E2ERoot -Recurse -Force
        }
    }

    It "Should update service pom, ci.yml, and skip root pom" {
        $service = "network"
        $module = "azure-resourcemanager-network-extra"
        $groupId = "com.azure.resourcemanager"

        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module
        Update-CiYml -SdkRepoPath $script:E2ERoot -Service $service -Module $module -GroupId $groupId

        # Service pom has the new module
        $svcPomContent = Get-Content (Join-Path $script:E2ERoot "sdk" "network" "pom.xml") -Raw
        $svcPomContent | Should -Match "<module>azure-resourcemanager-network-extra</module>"

        # ci.yml has the new artifact
        $ciContent = Get-Content (Join-Path $script:E2ERoot "sdk" "network" "ci.yml") -Raw
        $ciContent | Should -Match "azure-resourcemanager-network-extra"
        $ciContent | Should -Match "release_azureresourcemanagernetworkextra"
    }
}

# ============================================================================
# End-to-end: CI.yml with POM - Brand new service
# ============================================================================
Describe "End-to-End: New Service with CI update" {
    BeforeEach {
        $script:E2ERoot = Join-Path $script:TestRoot "e2e-ci-case2-$(New-Guid)"
        New-Item -ItemType Directory -Path $script:E2ERoot -Force | Out-Null

        Set-Content -Path (Join-Path $script:E2ERoot "pom.xml") -Value $script:SampleRootPomXml
        $pkgDir = Join-Path $script:E2ERoot "sdk" "compute" "azure-resourcemanager-compute"
        New-Item -ItemType Directory -Path $pkgDir -Force | Out-Null
        Set-Content -Path (Join-Path $pkgDir "pom.xml") -Value $script:SamplePomXml
    }

    AfterEach {
        if (Test-Path $script:E2ERoot) {
            Remove-Item -Path $script:E2ERoot -Recurse -Force
        }
    }

    It "Should create ci.yml, service pom, and update root pom" {
        $service = "compute"
        $module = "azure-resourcemanager-compute"
        $groupId = "com.azure.resourcemanager"

        Update-RootPom -SdkRepoPath $script:E2ERoot -Service $service
        Update-ServicePom -SdkRepoPath $script:E2ERoot -Service $service -Module $module
        Update-CiYml -SdkRepoPath $script:E2ERoot -Service $service -Module $module -GroupId $groupId

        # Root pom updated
        $rootPom = Get-Content (Join-Path $script:E2ERoot "pom.xml") -Raw
        $rootPom | Should -Match "<module>sdk/compute</module>"

        # Service pom created
        $svcPom = Get-Content (Join-Path $script:E2ERoot "sdk" "compute" "pom.xml") -Raw
        $svcPom | Should -Match "<module>azure-resourcemanager-compute</module>"

        # ci.yml created with artifact
        $ciFile = Join-Path $script:E2ERoot "sdk" "compute" "ci.yml"
        Test-Path $ciFile | Should -Be $true
        $ciContent = Get-Content $ciFile -Raw
        $ciContent | Should -Match "azure-resourcemanager-compute"
        $ciContent | Should -Match "com.azure.resourcemanager"
        $ciContent | Should -Match "ServiceDirectory: compute"
    }
}

# ============================================================================
# End-to-end: Unsupported SDK type should fail the run
# ============================================================================
Describe "End-to-End: Unsupported SDK Type" {
    BeforeEach {
        $script:E2ERoot = Join-Path $script:TestRoot "e2e-unsupported-$(New-Guid)"
        New-Item -ItemType Directory -Path $script:E2ERoot -Force | Out-Null

        Set-Content -Path (Join-Path $script:E2ERoot "pom.xml") -Value $script:SampleRootPomXml
        $pkgDir = Join-Path $script:E2ERoot "sdk" "spring" "spring-cloud-azure-core"
        New-Item -ItemType Directory -Path $pkgDir -Force | Out-Null

        $springPom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-core</artifactId>
    <version>4.0.0</version>
</project>
"@
        Set-Content -Path (Join-Path $pkgDir "pom.xml") -Value $springPom
    }

    AfterEach {
        if (Test-Path $script:E2ERoot) {
            Remove-Item -Path $script:E2ERoot -Recurse -Force
        }
    }

    It "Should fail for unsupported groupId (e.g., com.azure.spring)" {
        $scriptPath = Join-Path $PSScriptRoot ".." "Automation-Sdk-UpdateMetadata.ps1"
        $pkgPath = Join-Path $script:E2ERoot "sdk" "spring" "spring-cloud-azure-core"

        $output = pwsh -NoProfile -File $scriptPath -PackagePath $pkgPath -SdkRepoPath $script:E2ERoot 2>&1
        $LASTEXITCODE | Should -Be 1
        $output | Out-String | Should -Match "Unsupported SDK type"
    }
}
