# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Helper functions for metadata update automation.

.DESCRIPTION
    This file provides helper functions for updating parent-level and root-level
    pom.xml files, as well as ci.yml files for Java SDK packages.
    Logic is ported from eng/automation/utils.py (Python).
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
