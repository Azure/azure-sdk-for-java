#$SetDocsPackageOnboarding = "Set-${Language}-DocsPackageOnboarding"
function Set-java-DocsPackageOnboarding($moniker, $metadata, $docRepoLocation, $packageSourceOverride) {
    $packageJsonPath = Join-Path $docRepoLocation "package.json"
    $onboardingInfo = Get-Content $packageJsonPath | ConvertFrom-Json

    $monikerOutputPath = "docs-ref-autogen"
    if ($moniker -ne 'latest') {
        $monikerOutputPath = "$moniker/docs-ref-autogen"
    }
    $monikerIndex = -1
    for($i = 0; $i -lt $onboardingInfo.Count; $i++) {
        if ($onboardingInfo[$i].output_path -eq $monikerOutputPath) {
            $monikerIndex = $i
            break
        }
    }

    if ($monikerIndex -eq -1) {
        Write-Error "No appropriate index for moniker $moniker"
    }

    $packageDownloadUrl = 'https://pkgs.dev.azure.com/azure-sdk/public/_packaging/azure-sdk-for-java/maven/v1'
    if ($PackageSourceOverride) {
        $packageDownloadUrl = $PackageSourceOverride
    }

    $onboardedPackages = @()
    foreach ($package in $metadata) {

        $packageInfo = [ordered]@{
            packageArtifactId = $package.Name
            packageGroupId = $package.Group
            packageVersion = $package.Version
            packageDownloadUrl = $packageDownloadUrl
        }

        # Add items from 'DocsCiConfigProperties' into onboarding info. If a
        # property already exists, it will be overwritten.
        if ($package.ContainsKey('DocsCiConfigProperties')) {
            foreach ($key in $package['DocsCiConfigProperties'].Keys) {
                $packageInfo[$key] = $package['DocsCiConfigProperties'][$key]
            }
        }

        $onboardedPackages += $packageInfo
    }

    $onboardingInfo[$monikerIndex].packages = $onboardedPackages

    Set-Content -Path $packageJsonPath -Value (ConvertTo-Json -InputObject $onboardingInfo -Depth 100)
}

#$GetDocsPackagesAlreadyOnboarded = "Get-${Language}-DocsPackagesAlreadyOnboarded"
function Get-java-DocsPackagesAlreadyOnboarded($docRepoLocation, $moniker) {
    return Get-java-OnboardedDocsMsPackagesForMoniker $docRepoLocation $moniker
}

# $GetPackageIdentity = "Get-${Language}-PackageIdentity"
function Get-java-PackageIdentity($package) {
    return "$($package['Group']):$($package['Name'])"
}

# Declared in common.ps1 as
# $GetPackageIdentityFromCsvMetadata = "Get-${Language}-PackageIdentityFromCsvMetadata"
function Get-java-PackageIdentityFromCsvMetadata($package) {
    return "$($package.GroupId):$($Package.Package)"
}

function Get-java-Java2DocfxJar {
    if (!$Env:BUILD_BINARIESDIRECTORY) {
        throw 'BUILD_BINARIESDIRECTORY is not set. Run install-rex-validation-tool.yml before documentation validation.'
    }

    $version = (Get-Content -LiteralPath (Join-Path $PSScriptRoot 'java2docfx.version.txt') -Raw).Trim()
    $java2docfxJar = Join-Path $Env:BUILD_BINARIESDIRECTORY "java2docfx/java2docfx-$version.jar"
    if (!(Test-Path -LiteralPath $java2docfxJar -PathType Leaf)) {
        throw "The pinned java2docfx $version JAR was not found at '$java2docfxJar'."
    }

    return (Resolve-Path -LiteralPath $java2docfxJar).ProviderPath
}

function Copy-java-MavenPackageToRepository {
    param(
        [Parameter(Mandatory = $true)]$Package,
        [Parameter(Mandatory = $true)][string]$Repository
    )

    $packageDirectory = Join-Path $Repository ($Package.GroupID -replace '\.', [IO.Path]::DirectorySeparatorChar)
    $packageDirectory = Join-Path $packageDirectory $Package.ArtifactID
    $packageDirectory = Join-Path $packageDirectory $Package.Version
    $null = New-Item -ItemType Directory -Path $packageDirectory -Force
    foreach ($artifact in $Package.AssociatedArtifacts) {
        Copy-Item -LiteralPath $artifact.File.FullName -Destination $packageDirectory -Force
    }
}

function Invoke-java-Java2Docfx {
    param(
        [Parameter(Mandatory = $true)][string]$Java2DocfxJar,
        [Parameter(Mandatory = $true)][string]$Artifact,
        [Parameter(Mandatory = $true)][string]$LocalPackageDirectory,
        [Parameter(Mandatory = $true)][string]$OutputDirectory,
        [Parameter(Mandatory = $true)][string]$MavenHome
    )

    $settingsPath = Join-Path $MavenHome '.m2/settings.xml'
    $workingDirectory = Split-Path -Parent $MavenHome
    $localPackageArgument = [IO.Path]::GetRelativePath($workingDirectory, $LocalPackageDirectory)
    $outputArgument = [IO.Path]::GetRelativePath($workingDirectory, $OutputDirectory)
    $previousLocation = Get-Location
    $previousEnvironment = @{
        HOME = $Env:HOME
        JAVA_TOOL_OPTIONS = $Env:JAVA_TOOL_OPTIONS
        MAVEN_ARGS = $Env:MAVEN_ARGS
        MAVEN_USER_HOME = $Env:MAVEN_USER_HOME
        USERPROFILE = $Env:USERPROFILE
    }
    try {
        $Env:HOME = $MavenHome
        $Env:USERPROFILE = $MavenHome
        $Env:MAVEN_USER_HOME = $MavenHome
        $Env:MAVEN_ARGS = "--settings `"$settingsPath`""
        $userHomeOption = "-Duser.home=$MavenHome"
        $Env:JAVA_TOOL_OPTIONS = (@($previousEnvironment.JAVA_TOOL_OPTIONS, $userHomeOption) |
            Where-Object { $_ }) -join ' '

        Set-Location $workingDirectory
        $output = @(& java -jar $Java2DocfxJar -p $Artifact -l $localPackageArgument -i -o $outputArgument 2>&1)
        return [pscustomobject]@{
            ExitCode = $LASTEXITCODE
            Output = $output
        }
    }
    finally {
        Set-Location $previousLocation
        foreach ($entry in $previousEnvironment.GetEnumerator()) {
            Set-Item -Path "Env:$($entry.Key)" -Value $entry.Value
        }
    }
}

function Validate-java-LocalDocsMsPackage {
    param(
        [Parameter(Mandatory = $true)]$PackageInfo,
        [Parameter(Mandatory = $true)][string]$ArtifactRoot,
        [Parameter(Mandatory = $true)][string]$OutputDirectory
    )

    if (($PackageInfo.PSObject.Properties['IncludedForValidation'] -and $PackageInfo.IncludedForValidation) -or
        ($PackageInfo.PSObject.Properties['ArtifactDetails'] -and $null -eq $PackageInfo.ArtifactDetails)) {
        return [pscustomobject]@{
            Status = 'Skipped'
            Reason = 'Package was included only as a co-built validation dependency.'
        }
    }
    if ($PackageInfo.PSObject.Properties['ArtifactDetails'] -and
        $PackageInfo.ArtifactDetails.PSObject.Properties['skipPublishPackage'] -and
        "$($PackageInfo.ArtifactDetails.skipPublishPackage)" -ieq 'true') {
        return [pscustomobject]@{
            Status = 'Skipped'
            Reason = 'Package is excluded from Maven publishing.'
        }
    }

    $artifactId = if ($PackageInfo.PSObject.Properties['ArtifactName'] -and $PackageInfo.ArtifactName) {
        $PackageInfo.ArtifactName
    } else {
        $PackageInfo.Name
    }
    $groupId = $PackageInfo.Group
    $version = if ($PackageInfo.PSObject.Properties['DevVersion'] -and $PackageInfo.DevVersion) {
        $PackageInfo.DevVersion
    } else {
        $PackageInfo.Version
    }
    if ([string]::IsNullOrWhiteSpace($groupId) -or
        [string]::IsNullOrWhiteSpace($artifactId) -or
        [string]::IsNullOrWhiteSpace($version)) {
        throw 'Java PackageInfo must contain nonempty Group, ArtifactName or Name, and Version or DevVersion values.'
    }

    if (-not (Get-Command Get-MavenPackageDetails -ErrorAction SilentlyContinue)) {
        . (Join-Path $PSScriptRoot '../MavenPackaging.ps1')
    }
    $packages = @(Get-MavenPackageDetails -ArtifactDirectory $ArtifactRoot)
    $identityMatches = @($packages | Where-Object {
        $_.GroupID -ceq $groupId -and $_.ArtifactID -ceq $artifactId
    })
    $targetMatches = @($identityMatches | Where-Object { $_.Version -ceq $version })
    $artifact = "${groupId}:${artifactId}:${version}"

    if ($targetMatches.Count -eq 0) {
        if ($identityMatches.Count -gt 0) {
            $actualVersions = @($identityMatches.Version | Sort-Object -Unique) -join ', '
            throw "Local package version mismatch for '$artifact'. Found: $actualVersions."
        }
        throw "Local package '$artifact' was not found under '$ArtifactRoot'."
    }
    if ($targetMatches.Count -gt 1) {
        throw "Local package '$artifact' is ambiguous: $($targetMatches.Count) matching POM files were found."
    }

    $target = $targetMatches[0]
    $pomFiles = @($target.AssociatedArtifacts | Where-Object {
        $_.Type -ceq 'pom' -and [string]::IsNullOrEmpty($_.Classifier)
    })
    $binaryJars = @($target.AssociatedArtifacts | Where-Object {
        $_.Type -ceq 'jar' -and [string]::IsNullOrEmpty($_.Classifier)
    })
    $sourceJars = @($target.AssociatedArtifacts | Where-Object {
        $_.Type -ceq 'jar' -and $_.Classifier -ceq 'sources'
    })
    foreach ($required in @(
        @{ Name = 'POM'; Files = $pomFiles },
        @{ Name = 'binary JAR'; Files = $binaryJars },
        @{ Name = 'sources JAR'; Files = $sourceJars }
    )) {
        if ($required.Files.Count -ne 1) {
            throw "Local package '$artifact' requires exactly one $($required.Name); found $($required.Files.Count)."
        }
    }

    $localPackageDirectory = Join-Path $OutputDirectory 'local-package'
    $mavenHome = Join-Path $OutputDirectory 'maven-home'
    $mavenRepository = Join-Path $MavenHome '.m2/repository'
    $generatorOutput = Join-Path $OutputDirectory 'api'
    foreach ($directory in @($localPackageDirectory, $mavenRepository, $generatorOutput)) {
        $null = New-Item -ItemType Directory -Path $directory -Force
    }

    $packageKeys = @{}
    foreach ($package in $packages) {
        $key = "$($package.GroupID):$($package.ArtifactID):$($package.Version)"
        if ($packageKeys.ContainsKey($key)) {
            throw "Co-built package '$key' is ambiguous under '$ArtifactRoot'."
        }
        $packageKeys[$key] = $true
        Copy-java-MavenPackageToRepository -Package $package -Repository $mavenRepository

        $packagePom = @($package.AssociatedArtifacts | Where-Object {
            $_.Type -ceq 'pom' -and [string]::IsNullOrEmpty($_.Classifier)
        })
        $packageBinary = @($package.AssociatedArtifacts | Where-Object {
            $_.Type -ceq 'jar' -and [string]::IsNullOrEmpty($_.Classifier)
        })
        $packageSources = @($package.AssociatedArtifacts | Where-Object {
            $_.Type -ceq 'jar' -and $_.Classifier -ceq 'sources'
        })
        if ($packagePom.Count -eq 1 -and $packageBinary.Count -eq 1 -and $packageSources.Count -eq 1) {
            $localDirectory = Join-Path $localPackageDirectory $package.GroupID
            $localDirectory = Join-Path $localDirectory $package.ArtifactID
            $null = New-Item -ItemType Directory -Path $localDirectory -Force
            foreach ($file in @($packagePom[0].File, $packageBinary[0].File, $packageSources[0].File)) {
                Copy-Item -LiteralPath $file.FullName -Destination $localDirectory -Force
            }
        }
    }

    $settingsDirectory = Join-Path $MavenHome '.m2'
    $null = New-Item -ItemType Directory -Path $settingsDirectory -Force
    @"
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
  <localRepository>$($mavenRepository -replace '\\', '/')</localRepository>
  <mirrors>
    <mirror>
      <id>central-only</id>
      <mirrorOf>external:*</mirrorOf>
      <url>https://repo.maven.apache.org/maven2</url>
    </mirror>
  </mirrors>
</settings>
"@ | Set-Content -LiteralPath (Join-Path $settingsDirectory 'settings.xml')

    $java2docfxJar = Get-java-Java2DocfxJar
    Write-Host "Calling pinned java2docfx for $artifact from '$localPackageDirectory'."
    $invocation = Invoke-java-Java2Docfx `
        -Java2DocfxJar $java2docfxJar `
        -Artifact $artifact `
        -LocalPackageDirectory $localPackageDirectory `
        -OutputDirectory $generatorOutput `
        -MavenHome $mavenHome
    $logPath = Join-Path $OutputDirectory 'java2docfx.log'
    @($invocation.Output) | Set-Content -LiteralPath $logPath
    @($invocation.Output) | Write-Host

    if ($invocation.ExitCode -ne 0) {
        return [pscustomobject]@{
            Status = 'Failed'
            Reason = "java2docfx exited with code $($invocation.ExitCode). See '$logPath'."
        }
    }

    return [pscustomobject]@{
        Status = 'Passed'
        Reason = ''
    }
}


# Defined in common.ps1 as:
# $ValidateDocsMsPackagesFn = "Validate-${Language}-DocMsPackages"
function Validate-java-DocMsPackages ($PackageInfo, $PackageInfos, $DocValidationImageId) {
    # While eng/common/scripts/Update-DocsMsMetadata.ps1 is still passing a single packageInfo, process as a batch
    if (!$PackageInfos) {
      $PackageInfos = @($PackageInfo)
    }

    # The install-rex-validation-tool.yml will install the java2docfx jar file into the Build.BinariesDirectory
    # which is a DevOps variable for the directory. In PS that variable is BUILD_BINARIESDIRECTORY.
    # The reason why this is necessary is that the command for java2docfx is in the following format:
    # java –jar java2docfx-1.0.0.jar.jar --packagesJsonFile "C\temp\package.json"
    # or
    # java –jar java2docfx-1.0.0.jar --package "<GroupId>:<ArtifactId>:<Version>"
    # which means we need to know where, exactly, because the java command requires the full path
    # to the jar file as an argument
    $java2docfxJar = $null
    if (!$Env:BUILD_BINARIESDIRECTORY) {
      LogError "Env:BUILD_BINARIESDIRECTORY is not set and this is where the java2docfx jar file should be installed."
      return $false
    }
    $java2docfxDir = Join-Path $Env:BUILD_BINARIESDIRECTORY "java2docfx"
    if (!(Test-Path $java2docfxDir)) {
      LogError "There should be a java2docfx directory under Env:BUILD_BINARIESDIRECTORY. Ensure that the /eng/pipelines/templates/steps/install-rex-validation-tool.yml template was run prior to whatever step is running this."
      return $false
    }
    $java2docfxJarLoc = @(Get-ChildItem -Path $java2docfxDir -File -Filter "java2docfx*.jar")
    if (!$java2docfxJarLoc) {
      LogError "The java2docfx jar file should be installed in $java2docfxDir and is not there."
      return $false
    } else {
      # In theory, this shouldn't happen as the install-rex-validation-tool.yml is the only thing
      # that'll ever install the jar
      if ($java2docfxJarLoc.Count -gt 1) {
          Write-Host "There were $($java2docfxJarLoc.Count) java2docfx jar files found in $Build_BinariesDirectory, using the first one"
      }
      $java2docfxJar = $java2docfxJarLoc[0]
      Write-Host "java2docfx jar location=$java2docfxJar"
    }

    $allSuccess = $true
    $originLocation = Get-Location
    foreach ($packageInfo in $PackageInfos) {
      $artifact = "$($packageInfo.Group):$($packageInfo.Name):$($packageInfo.Version)"
      $tempDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "$($packageInfo.Group)-$($packageInfo.Name)-$($packageInfo.Version)"
      New-Item $tempDirectory -ItemType Directory | Out-Null
      # Set the location to the temp directory. The reason being is that it'll effectively be empty, no
      # other jars, no POM files aka nothing Java related to pick up.
      Set-Location $tempDirectory
      try {
        Write-Host "Calling java2docfx for $artifact"
        Write-Host "java -jar ""$java2docfxJar"" -p ""$artifact"" -i"
        # -i ignores *.implementation* packages
        $java2docfxResults = java `
          -jar "$java2docfxJar"`
          -p "$artifact" `
          -i
        # JRS-TODO: The -o option is something I'm currently questioning the behavior of but
        # I can do some initial testing without that option being set
        # -p "$artifact" `
        # -o "$tempDirectory"

        if ($LASTEXITCODE -ne 0) {
          LogWarning "java2docfx failed for $artifact"
          $java2docfxResults | Write-Host
          $allSuccess = $false
        }
      }
      catch {
        LogError "Exception while trying to download: $artifact"
        LogError $_
        LogError $_.ScriptStackTrace
        $allSuccess = $false
      }
      finally {
        # Ensure that the origianl location is restored
        Set-Location $originLocation
        # everything is contained within the temp directory, clean it up every time
        if (Test-Path $tempDirectory) {
          Remove-Item $tempDirectory -Recurse -Force
        }
      }
    }

    return $allSuccess
  }
