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

    $packageDownloadUrl = 'https://repo1.maven.org/maven2'
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
        Write-Host "java -jar ""$java2docfxJar"" -p ""$artifact"""
        $java2docfxResults = java `
        -jar "$java2docfxJar"`
        -p "$artifact"
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
