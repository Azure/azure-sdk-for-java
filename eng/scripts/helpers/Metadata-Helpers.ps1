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

# Equivalent of the CI update part of Python update_service_files_for_new_lib()
function Update-CiYml {
    <#
    .SYNOPSIS
        Adds an artifact entry to the service ci.yml file, creating it if it doesn't exist.

    .DESCRIPTION
        Follows the same logic as Python update_service_files_for_new_lib() for ci.yml:
        - If ci.yml doesn't exist, create from template
        - If ci.yml exists with SDKType=data, rename to ci.data.yml and create new ci.yml
        - If artifact already exists, skip
        - Otherwise add the artifact (with release parameter)
        Uses text-based operations to preserve the existing YAML format.

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

    $ciFile = Join-Path $SdkRepoPath "sdk" $Service "ci.yml"
    $safeName = $Module -replace '-', ''
    $isMgmt = $Module -match '-resourcemanager-'
    $releaseDefault = if ($isMgmt) { "false" } else { "true" }
    $releaseParameterName = "release_$safeName"

    if (-not (Test-Path $ciFile)) {
        # ci.yml creation for new services is handled by a separate script
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[CI][Skip] ci.yml does not exist, skipping (new service ci.yml creation is handled separately)"
        }
        return
    }

    # Read existing ci.yml
    $existingContent = Get-Content -Path $ciFile -Raw

    # Check if artifact already exists
    if ($existingContent -match "name:\s+$([regex]::Escape($Module))\s") {
        if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
            LogInfo "[CI][Skip] ci.yml already has artifact $Module"
        }
        return
    }

    if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
        LogInfo "[CI][Process] updating ci.yml with artifact $Module"
    }

    # Add path entries to trigger and pr includes/excludes
    $updatedContent = Add-PathToCiYml -CiContent $existingContent -Service $Service -Module $Module

    # Add release parameter before "extends:" line
    $paramBlock = "- name: ${releaseParameterName}`n  displayName: '${Module}'`n  type: boolean`n  default: ${releaseDefault}`n`n"

    $extendsIdx = $updatedContent.IndexOf("`nextends:")
    if ($extendsIdx -ge 0) {
        $extendsIdx += 1  # skip the newline
        $updatedContent = $updatedContent.Substring(0, $extendsIdx) + $paramBlock + $updatedContent.Substring($extendsIdx)
    }
    elseif ($updatedContent.IndexOf("extends:") -ge 0) {
        $extendsIdx = $updatedContent.IndexOf("extends:")
        $updatedContent = $updatedContent.Substring(0, $extendsIdx) + $paramBlock + $updatedContent.Substring($extendsIdx)
    }

    # Add artifact entry at end of file
    $artifactBlock = "      - name: ${Module}`n        groupId: ${GroupId}`n        safeName: ${SafeName}`n        releaseInBatch: `${{ parameters.${releaseParameterName} }}"
    $updatedContent = $updatedContent.TrimEnd() + "`n" + $artifactBlock + "`n"

    Set-Content -Path $ciFile -Value $updatedContent -NoNewline
    if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
        LogInfo "[CI][Success] Updated ci.yml with artifact $Module"
    }
}

function Write-NewCiYmlFile {
    <#
    .SYNOPSIS
        Creates a brand-new ci.yml file from scratch.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)][string]$CiFile,
        [Parameter(Mandatory = $true)][string]$Service,
        [Parameter(Mandatory = $true)][string]$Module,
        [Parameter(Mandatory = $true)][string]$GroupId,
        [Parameter(Mandatory = $true)][string]$SafeName,
        [Parameter(Mandatory = $true)][string]$ReleaseParameterName,
        [Parameter(Mandatory = $true)][string]$ReleaseDefault
    )

    $content = @"
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

parameters:
- name: ${ReleaseParameterName}
  displayName: '${Module}'
  type: boolean
  default: ${ReleaseDefault}

extends:
  template: ../../eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: ${Service}
    Artifacts:
      - name: ${Module}
        groupId: ${GroupId}
        safeName: ${SafeName}
        releaseInBatch: `${{ parameters.${ReleaseParameterName} }}
"@

    $ciDir = Split-Path $CiFile -Parent
    if (-not (Test-Path $ciDir)) {
        New-Item -ItemType Directory -Path $ciDir -Force | Out-Null
    }
    Set-Content -Path $CiFile -Value $content
    if (Get-Command LogInfo -ErrorAction SilentlyContinue) {
        LogInfo "[CI][Success] Created new ci.yml at: $CiFile"
    }
}

function Add-PathToCiYml {
    <#
    .SYNOPSIS
        Adds include/exclude paths for a module to both trigger and pr sections.

    .PARAMETER CiContent
        The ci.yml content as a string.

    .PARAMETER Service
        The service directory name.

    .PARAMETER Module
        The module name.

    .OUTPUTS
        Updated ci.yml content string.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$CiContent,

        [Parameter(Mandatory = $true)]
        [string]$Service,

        [Parameter(Mandatory = $true)]
        [string]$Module
    )

    $includeEntry = "      - sdk/${Service}/${Module}/"
    $excludeEntry = "      - sdk/${Service}/${Module}/pom.xml"
    $modulePathEscaped = [regex]::Escape("sdk/${Service}/${Module}/")

    $lines = $CiContent -split "`n"
    $newLines = [System.Collections.Generic.List[string]]::new()

    $inSection = ""  # "trigger" or "pr"
    $inPaths = $false
    $inInclude = $false
    $inExclude = $false
    $addedInclude = $false
    $addedExclude = $false
    $hasModuleInclude = $CiContent -match $modulePathEscaped

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $trimmed = $line.Trim()

        # Track which top-level section we're in
        if ($line -match '^trigger:') { $inSection = "trigger"; $addedInclude = $false; $addedExclude = $false }
        elseif ($line -match '^pr:') { $inSection = "pr"; $addedInclude = $false; $addedExclude = $false }
        elseif ($line -match '^[a-zA-Z]') {
            if ($trimmed -ne 'trigger:' -and $trimmed -ne 'pr:') {
                $inSection = ""
                $inPaths = $false
                $inInclude = $false
                $inExclude = $false
            }
        }

        if ($inSection -ne "" -and $trimmed -eq 'paths:') { $inPaths = $true }
        if ($inPaths -and $trimmed -eq 'include:') { $inInclude = $true; $inExclude = $false }
        if ($inPaths -and $trimmed -eq 'exclude:') { $inExclude = $true; $inInclude = $false }

        # Detect end of include block
        if ($inInclude -and -not $addedInclude -and $trimmed -match '^-') {
            $nextIdx = $i + 1
            if ($nextIdx -lt $lines.Count) {
                $nextTrimmed = $lines[$nextIdx].Trim()
                if ($nextTrimmed -eq 'exclude:' -or ($nextTrimmed -ne '' -and $nextTrimmed -notmatch '^-')) {
                    if (-not $hasModuleInclude) {
                        $newLines.Add($line)
                        $newLines.Add($includeEntry)
                        $addedInclude = $true
                        continue
                    }
                }
            }
        }

        # Detect end of exclude block
        if ($inExclude -and -not $addedExclude -and $trimmed -match '^-') {
            $nextIdx = $i + 1
            $isLastExcludeLine = $false
            if ($nextIdx -lt $lines.Count) {
                $nextTrimmed = $lines[$nextIdx].Trim()
                if ($nextTrimmed -eq '' -or ($nextTrimmed -notmatch '^-')) {
                    $isLastExcludeLine = $true
                }
            }
            else {
                $isLastExcludeLine = $true
            }

            if ($isLastExcludeLine) {
                $moduleExcludeEscaped = [regex]::Escape("sdk/${Service}/${Module}/pom.xml")
                if ($CiContent -notmatch $moduleExcludeEscaped) {
                    $newLines.Add($line)
                    $newLines.Add($excludeEntry)
                    $addedExclude = $true
                    $inExclude = $false
                    $inPaths = $false
                    continue
                }
            }
        }

        $newLines.Add($line)
    }

    return $newLines -join "`n"
}
