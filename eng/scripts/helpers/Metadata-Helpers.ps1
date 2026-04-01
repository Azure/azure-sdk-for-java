# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Helper functions for metadata update automation.

.DESCRIPTION
    This file provides helper functions for updating parent-level and root-level
    pom.xml files, as well as ci.yml files for Java SDK packages.
    Logic is ported from eng/automation/utils.py and parameters.py (Python).
#>

function Get-ServiceAndModuleFromPath {
    <#
    .SYNOPSIS
        Derives service name and module (artifact) name from a package path.

    .DESCRIPTION
        Given a package path like "C:\repos\azure-sdk-for-java\sdk\network\azure-resourcemanager-network",
        extracts the service name ("network") and module name ("azure-resourcemanager-network").

    .PARAMETER PackagePath
        Absolute path to the package directory.

    .PARAMETER SdkRepoPath
        Absolute path to the SDK repository root.

    .OUTPUTS
        Hashtable with Service and Module properties.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$PackagePath,

        [Parameter(Mandatory = $true)]
        [string]$SdkRepoPath
    )

    # Normalize paths to forward slashes for consistent comparison
    $normalizedPackagePath = $PackagePath.TrimEnd('\', '/').Replace('\', '/')
    $normalizedSdkRepoPath = $SdkRepoPath.TrimEnd('\', '/').Replace('\', '/')

    $sdkDir = "$normalizedSdkRepoPath/sdk/"
    if (-not $normalizedPackagePath.StartsWith($sdkDir)) {
        throw "PackagePath '$PackagePath' is not under sdk/ directory of '$SdkRepoPath'. Expected format: {SdkRepoPath}/sdk/{service}/{module}"
    }

    $relativePath = $normalizedPackagePath.Substring($sdkDir.Length)
    $parts = $relativePath.Split('/')
    if ($parts.Count -lt 2) {
        throw "Cannot determine service and module from path: $PackagePath. Expected format: sdk/{service}/{module}"
    }

    return @{
        Service = $parts[0]
        Module  = $parts[1]
    }
}

function Get-GroupIdFromPom {
    <#
    .SYNOPSIS
        Extracts groupId from a Maven POM file.

    .PARAMETER PomPath
        The absolute path to the pom.xml file.

    .OUTPUTS
        String representing the groupId.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidateScript({Test-Path $_})]
        [string]$PomPath
    )

    [xml]$pomXml = Get-Content $PomPath
    $groupId = $pomXml.project.groupId

    # If groupId is not in the current project, check parent
    if ([string]::IsNullOrEmpty($groupId)) {
        $groupId = $pomXml.project.parent.groupId
    }

    if ([string]::IsNullOrEmpty($groupId)) {
        throw "Could not extract groupId from POM file: $PomPath"
    }

    return $groupId
}

# Equivalent of Python add_module_to_modules()
function Add-ModuleToModulesBlock {
    <#
    .SYNOPSIS
        Adds a module entry to a <modules> XML block, sorted alphabetically.

    .PARAMETER ModulesBlock
        The existing <modules>...</modules> XML string.

    .PARAMETER Module
        The module name to add.

    .OUTPUTS
        String representing the updated <modules> block.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$ModulesBlock,

        [Parameter(Mandatory = $true)]
        [string]$Module
    )

    # Extract indent from the closing </modules> tag (matches Python: re.search(r"([^\S\n\r]*)</modules>", modules))
    if ($ModulesBlock -match '([^\S\n\r]*)</modules>') {
        $closingIndent = $matches[1]
        $indent = $closingIndent + "  "
    }
    else {
        $closingIndent = "  "
        $indent = "    "
    }

    # Collect all existing modules into a set and add new one
    $allModules = [System.Collections.Generic.HashSet[string]]::new()
    $moduleMatches = [regex]::Matches($ModulesBlock, '<module>(.*?)</module>')
    foreach ($m in $moduleMatches) {
        [void]$allModules.Add($m.Groups[1].Value)
    }
    [void]$allModules.Add($Module)

    # Sort and build module lines (matches Python: indent + POM_MODULE_FORMAT.format(module))
    $sortedModules = $allModules | Sort-Object
    $moduleLines = ($sortedModules | ForEach-Object { "${indent}<module>$_</module>" }) -join "`n"

    return "<modules>`n${moduleLines}`n${closingIndent}</modules>"
}

# Equivalent of Python add_module_to_default_profile()
function Add-ModuleToDefaultProfile {
    <#
    .SYNOPSIS
        Adds a module to the default profile's <modules> section in a POM with profiles.

    .PARAMETER PomContent
        The full content of the POM file.

    .PARAMETER Module
        The module name to add.

    .OUTPUTS
        Hashtable with Success (boolean) and Content (string) properties.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$PomContent,

        [Parameter(Mandatory = $true)]
        [string]$Module
    )

    $profileMatches = [regex]::Matches($PomContent, '(?s)<profile>.*?</profile>')
    foreach ($profileMatch in $profileMatches) {
        $profileValue = $profileMatch.Value
        if ($profileValue -match '<id>default</id>') {
            if (([regex]::Matches($profileValue, '<modules>')).Count -gt 1) {
                if (Get-Command LogError -ErrorAction SilentlyContinue) {
                    LogError "[POM][Profile][Skip] find more than one <modules> in <profile> default"
                }
                return @{ Success = $false; Content = "" }
            }

            $modulesMatch = [regex]::Match($profileValue, '(?s)<modules>.*</modules>')
            if (-not $modulesMatch.Success) {
                if (Get-Command LogError -ErrorAction SilentlyContinue) {
                    LogError "[POM][Profile][Skip] Cannot find <modules> in <profile> default"
                }
                return @{ Success = $false; Content = "" }
            }

            $updatedModules = Add-ModuleToModulesBlock -ModulesBlock $modulesMatch.Value -Module $Module

            # Calculate absolute position (matches Python: pom[: profile.start() + modules.start()])
            $absStart = $profileMatch.Index + $modulesMatch.Index
            $absEnd = $absStart + $modulesMatch.Length
            $updatedPom = $PomContent.Substring(0, $absStart) + $updatedModules + $PomContent.Substring($absEnd)

            return @{ Success = $true; Content = $updatedPom }
        }
    }

    if (Get-Command LogError -ErrorAction SilentlyContinue) {
        LogError "[POM][Profile][Skip] cannot find <profile> with <id> default"
    }
    return @{ Success = $false; Content = "" }
}

# Equivalent of Python add_module_to_pom()
function Add-ModuleToPom {
    <#
    .SYNOPSIS
        Adds a module entry to a POM file's <modules> section.

    .DESCRIPTION
        Handles POMs with single <modules> blocks and POMs with <profiles> containing
        a default profile with its own <modules> block.

    .PARAMETER PomContent
        The full content of the POM file as a string.

    .PARAMETER Module
        The module name to add.

    .OUTPUTS
        Hashtable with Success (boolean) and Content (string) properties.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$PomContent,

        [Parameter(Mandatory = $true)]
        [string]$Module
    )

    # Check if module already exists
    if ($PomContent.Contains("<module>$Module</module>")) {
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[POM][Skip] pom already has module $Module"
        }
        return @{ Success = $true; Content = $PomContent }
    }

    # Count <modules> blocks
    $modulesCount = ([regex]::Matches($PomContent, '<modules>')).Count

    if ($modulesCount -gt 1) {
        if ($PomContent.Contains('<profiles>')) {
            return Add-ModuleToDefaultProfile -PomContent $PomContent -Module $Module
        }
        if (Get-Command LogError -ErrorAction SilentlyContinue) {
            LogError "[POM][Skip] find more than one <modules> in pom"
        }
        return @{ Success = $false; Content = "" }
    }

    # Find the single <modules> block
    $modulesMatch = [regex]::Match($PomContent, '(?s)<modules>.*?</modules>')
    if (-not $modulesMatch.Success) {
        if (Get-Command LogError -ErrorAction SilentlyContinue) {
            LogError "[POM][Skip] Cannot find <modules> in pom"
        }
        return @{ Success = $false; Content = "" }
    }

    $updatedModules = Add-ModuleToModulesBlock -ModulesBlock $modulesMatch.Value -Module $Module
    $updatedPom = $PomContent.Substring(0, $modulesMatch.Index) + $updatedModules + $PomContent.Substring($modulesMatch.Index + $modulesMatch.Length)

    return @{ Success = $true; Content = $updatedPom }
}

# Equivalent of Python update_root_pom()
function Update-RootPom {
    <#
    .SYNOPSIS
        Adds sdk/{service} as a module to the root pom.xml.

    .PARAMETER SdkRepoPath
        Absolute path to the SDK repository root.

    .PARAMETER Service
        The service directory name (e.g., "network").
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$SdkRepoPath,

        [Parameter(Mandatory = $true)]
        [string]$Service
    )

    $pomFile = Join-Path $SdkRepoPath "pom.xml"
    if (-not (Test-Path $pomFile)) {
        if (Get-Command LogError -ErrorAction SilentlyContinue) {
            LogError "[POM][Skip] cannot find root pom"
        }
        return
    }

    $module = "sdk/$Service"
    $pomContent = Get-Content -Path $pomFile -Raw

    if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
        LogInfo "[POM][Process] dealing with root pom"
    }

    $result = Add-ModuleToPom -PomContent $pomContent -Module $module
    if ($result.Success) {
        Set-Content -Path $pomFile -Value $result.Content -NoNewline
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[POM][Success] Write to root pom"
        }
    }
}

# Equivalent of the POM update part of Python update_service_files_for_new_lib()
function Update-ServicePom {
    <#
    .SYNOPSIS
        Adds a module to the service-level pom.xml, creating it if it doesn't exist.

    .PARAMETER SdkRepoPath
        Absolute path to the SDK repository root.

    .PARAMETER Service
        The service directory name.

    .PARAMETER Module
        The artifact/module name to add.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$SdkRepoPath,

        [Parameter(Mandatory = $true)]
        [string]$Service,

        [Parameter(Mandatory = $true)]
        [string]$Module
    )

    $pomFile = Join-Path $SdkRepoPath "sdk" $Service "pom.xml"

    if (Test-Path $pomFile) {
        $pomContent = Get-Content -Path $pomFile -Raw
    }
    else {
        # Create from template (matches Python POM_FORMAT)
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[POM][Process] creating new service pom.xml"
        }
        $pomContent = @"
<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>azure-${Service}-service</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version><!-- Need not change for every release-->

  <modules>
    <module>${Module}</module>
  </modules>
</project>
"@
        $pomDir = Split-Path $pomFile -Parent
        if (-not (Test-Path $pomDir)) {
            New-Item -ItemType Directory -Path $pomDir -Force | Out-Null
        }
        Set-Content -Path $pomFile -Value $pomContent -NoNewline
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[POM][Success] Created new service pom.xml at: $pomFile"
        }
        return
    }

    if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
        LogInfo "[POM][Process] dealing with service pom.xml"
    }

    $result = Add-ModuleToPom -PomContent $pomContent -Module $Module
    if ($result.Success) {
        Set-Content -Path $pomFile -Value $result.Content -NoNewline
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[POM][Success] Write to service pom.xml"
        }
    }
}

# ============================================================================
# CI.yml update functions
# Ported from eng/automation/utils.py update_service_files_for_new_lib() and
# eng/automation/parameters.py CI_FORMAT / CI_HEADER
# ============================================================================

function New-CiYmlContent {
    <#
    .SYNOPSIS
        Generates a new ci.yml content string from template.

    .PARAMETER Service
        The service directory name (e.g., "network").

    .PARAMETER Module
        The artifact/module name (e.g., "azure-resourcemanager-network").

    .OUTPUTS
        String representing the ci.yml content.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Service,

        [Parameter(Mandatory = $true)]
        [string]$Module
    )

    # Matches Python CI_HEADER + CI_FORMAT from parameters.py
    return @"
# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.

trigger:
  branches:
    include:
      - main
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/${Service}/ci.yml
      - sdk/${Service}/${Module}/
    exclude:
      - sdk/${Service}/pom.xml
      - sdk/${Service}/${Module}/pom.xml

pr:
  branches:
    include:
      - main
      - feature/*
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/${Service}/ci.yml
      - sdk/${Service}/${Module}/
    exclude:
      - sdk/${Service}/pom.xml
      - sdk/${Service}/${Module}/pom.xml

parameters: []

extends:
  template: ../../eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: ${Service}
    Artifacts: []
"@
}

function Add-ArtifactToCiYml {
    <#
    .SYNOPSIS
        Adds an artifact entry to a parsed ci.yml object.

    .DESCRIPTION
        Handles two modes based on whether the ci.yml has a parameters list:
        - With parameters: adds artifact with releaseInBatch and a release parameter
        - Without parameters: adds artifact without releaseInBatch

    .PARAMETER CiYml
        The parsed ci.yml as a hashtable/ordered dictionary (from ConvertFrom-Yaml).

    .PARAMETER Module
        The artifact/module name.

    .PARAMETER GroupId
        The Maven groupId.

    .OUTPUTS
        Boolean indicating whether the artifact was added.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        $CiYml,

        [Parameter(Mandatory = $true)]
        [string]$Module,

        [Parameter(Mandatory = $true)]
        [string]$GroupId
    )

    # Validate structure
    $extends = $CiYml["extends"]
    if (-not ($extends -is [System.Collections.IDictionary])) {
        if (Get-Command LogError -ErrorAction SilentlyContinue) {
            LogError "[CI][Skip] Unexpected ci.yml format: missing 'extends'"
        }
        return $false
    }
    $params = $extends["parameters"]
    if (-not ($params -is [System.Collections.IDictionary])) {
        if (Get-Command LogError -ErrorAction SilentlyContinue) {
            LogError "[CI][Skip] Unexpected ci.yml format: missing 'extends.parameters'"
        }
        return $false
    }
    $artifacts = $params["Artifacts"]
    if (-not ($artifacts -is [System.Collections.IList])) {
        if (Get-Command LogError -ErrorAction SilentlyContinue) {
            LogError "[CI][Skip] Unexpected ci.yml format: 'Artifacts' is not a list"
        }
        return $false
    }

    # Check if module already exists
    foreach ($artifact in $artifacts) {
        if ($artifact["name"] -eq $Module -and $artifact["groupId"] -eq $GroupId) {
            if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
                LogInfo "[CI][Skip] ci.yml already has module $Module"
            }
            return $false
        }
    }

    $safeName = $Module.Replace("-", "")

    # Check if ci.yml has a parameters list (not just empty/null)
    $ciParameters = $CiYml["parameters"]
    $hasParametersList = ($ciParameters -is [System.Collections.IList]) -and ($ciParameters.Count -gt 0)

    if ($hasParametersList) {
        # Add artifact with releaseInBatch reference
        $releaseParameterName = "release_$safeName"
        $releaseInBatchRef = "`${{ parameters.$releaseParameterName }}"

        $newArtifact = [ordered]@{
            name           = $Module
            groupId        = $GroupId
            safeName       = $safeName
            releaseInBatch = $releaseInBatchRef
        }
        $artifacts.Add($newArtifact)

        # True for data-plane, False for management-plane
        $releaseInBatchDefault = -not ($Module -match "-resourcemanager-")

        $newParam = [ordered]@{
            name        = $releaseParameterName
            displayName = $Module
            type        = "boolean"
            default     = $releaseInBatchDefault
        }
        $ciParameters.Add($newParam)
    }
    else {
        # Add artifact without releaseInBatch
        $newArtifact = [ordered]@{
            name    = $Module
            groupId = $GroupId
            safeName = $safeName
        }
        $artifacts.Add($newArtifact)
    }

    return $true
}

function ConvertTo-CiYmlString {
    <#
    .SYNOPSIS
        Converts a ci.yml object back to a YAML string with proper formatting.

    .DESCRIPTION
        Uses powershell-yaml to serialize, then applies formatting fixes to match
        the expected ci.yml style (blank lines between top-level sections).

    .PARAMETER CiYml
        The ci.yml object to serialize.

    .OUTPUTS
        String representing the formatted YAML content.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        $CiYml
    )

    $yamlStr = ConvertTo-Yaml $CiYml

    # Add blank line before each top-level key (matches Python: re.sub(r"(\n\S)", r"\n\1", ci_yml_str))
    $yamlStr = $yamlStr -replace '(\n)(\S)', "`$1`n`$2"

    # Add CI header
    $header = "# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.`n`n"

    return $header + $yamlStr
}

function Update-CiYml {
    <#
    .SYNOPSIS
        Updates or creates the ci.yml file for a service.

    .DESCRIPTION
        Ported from Python update_service_files_for_new_lib() CI logic.
        Cases:
        1. ci.yml doesn't exist → create from template, add artifact
        2. ci.yml exists with SDKType=data → rename to ci.data.yml, create new, add artifact
        3. ci.yml exists, module already present → skip
        4. ci.yml exists, module not present → add artifact (with or without release param)

    .PARAMETER SdkRepoPath
        Absolute path to the SDK repository root.

    .PARAMETER Service
        The service directory name.

    .PARAMETER Module
        The artifact/module name.

    .PARAMETER GroupId
        The Maven groupId for the artifact.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$SdkRepoPath,

        [Parameter(Mandatory = $true)]
        [string]$Service,

        [Parameter(Mandatory = $true)]
        [string]$Module,

        [Parameter(Mandatory = $true)]
        [string]$GroupId
    )

    $ciYmlFile = Join-Path $SdkRepoPath "sdk" $Service "ci.yml"

    if (Test-Path $ciYmlFile) {
        $ciYmlContent = Get-Content -Path $ciYmlFile -Raw
        $ciYml = ConvertFrom-Yaml $ciYmlContent -Ordered

        # Check for SDKType=data → rename and create new
        $sdkType = ""
        try {
            $sdkType = $ciYml["extends"]["parameters"]["SDKType"]
        } catch {}

        if ($sdkType -is [string] -and $sdkType.ToLower() -eq "data") {
            $ciDataFile = Join-Path $SdkRepoPath "sdk" $Service "ci.data.yml"
            Move-Item -Path $ciYmlFile -Destination $ciDataFile -Force
            if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
                LogInfo "[CI][Process] Renamed existing ci.yml (SDKType=data) to ci.data.yml"
            }
            $ciYmlContent = New-CiYmlContent -Service $Service -Module $Module
            $ciYml = ConvertFrom-Yaml $ciYmlContent -Ordered
        }
    }
    else {
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[CI][Process] Creating new ci.yml for service: $Service"
        }
        $ciYmlContent = New-CiYmlContent -Service $Service -Module $Module
        $ciYml = ConvertFrom-Yaml $ciYmlContent -Ordered
    }

    $added = Add-ArtifactToCiYml -CiYml $ciYml -Module $Module -GroupId $GroupId
    if ($added) {
        $outputStr = ConvertTo-CiYmlString -CiYml $ciYml
        $ciDir = Split-Path $ciYmlFile -Parent
        if (-not (Test-Path $ciDir)) {
            New-Item -ItemType Directory -Path $ciDir -Force | Out-Null
        }
        Set-Content -Path $ciYmlFile -Value $outputStr -NoNewline
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[CI][Success] Write to ci.yml"
        }
    }
}
