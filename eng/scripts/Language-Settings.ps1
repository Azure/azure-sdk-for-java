$Language = "java"
$LanguageDisplayName = "Java"
$PackageRepository = "Maven"
$packagePattern = "*.pom"
$MetadataUri = "https://raw.githubusercontent.com/Azure/azure-sdk/main/_data/releases/latest/java-packages.csv"
$BlobStorageUrl = "https://azuresdkdocs.blob.core.windows.net/%24web?restype=container&comp=list&prefix=java%2F&delimiter=%2F"
$CampaignTag = Resolve-Path (Join-Path -Path $PSScriptRoot -ChildPath "../repo-docs/ga_tag.html")

function Get-java-PackageInfoFromRepo ($pkgPath, $serviceDirectory)
{
  $projectPath = Join-Path $pkgPath "pom.xml"
  if (Test-Path $projectPath)
  {
    $projectData = New-Object -TypeName XML
    $projectData.load($projectPath)

    if ($projectData.project.psobject.properties.name -notcontains "artifactId" -or !$projectData.project.artifactId) {
      Write-Host "$projectPath doesn't have a defined artifactId so skipping this pom."
      return $null
    }

    if ($projectData.project.psobject.properties.name -notcontains "version" -or !$projectData.project.version) {
      Write-Host "$projectPath doesn't have a defined version so skipping this pom."
      return $null
    }

    if ($projectData.project.psobject.properties.name -notcontains "groupid" -or !$projectData.project.groupId) {
      Write-Host "$projectPath doesn't have a defined groupId so skipping this pom."
      return $null
    }

    $projectPkgName = $projectData.project.artifactId
    $pkgVersion = $projectData.project.version
    $pkgGroup = $projectData.project.groupId

    $pkgProp = [PackageProps]::new($projectPkgName, $pkgVersion.ToString(), $pkgPath, $serviceDirectory, $pkgGroup)
    if ($projectPkgName -match "mgmt" -or $projectPkgName -match "resourcemanager")
    {
      $pkgProp.SdkType = "mgmt"
    }
    elseif ($projectPkgName -match "spring")
    {
      $pkgProp.SdkType = "spring"
    }
    else
    {
      $pkgProp.SdkType = "client"
    }
    $pkgProp.IsNewSdk = $False
    if ($pkgGroup) {
      $pkgProp.IsNewSdk = $pkgGroup.StartsWith("com.azure")
    }
    $pkgProp.ArtifactName = $projectPkgName
    return $pkgProp
  }
  return $null
}

# Returns the maven (really sonatype) publish status of a package id and version.
function IsMavenPackageVersionPublished($pkgId, $pkgVersion, $groupId)
{
  try
  {
    $uri = "https://oss.sonatype.org/content/repositories/releases/$groupId/$pkgId/$pkgVersion/$pkgId-$pkgVersion.pom"
    $pomContent = Invoke-RestMethod -MaximumRetryCount 3 -RetryIntervalSec 10 -Method "GET" -uri $uri

    if ($pomContent -ne $null -or $pomContent.Length -eq 0)
    {
      return $true
    }
    else
    {
      return $false
    }
  }
  catch
  {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $statusDescription = $_.Exception.Response.StatusDescription

    # if this is 404ing, then this pkg has never been published before
    if ($statusCode -eq 404) {
      return $false
    }

    Write-Host "VersionCheck to maven for packageId $pkgId failed with statuscode $statusCode"
    Write-Host $statusDescription
    exit(1)
  }
}

# Parse out package publishing information given a maven POM file
function Get-java-PackageInfoFromPackageFile ($pkg, $workingDirectory)
{
  [xml]$contentXML = Get-Content $pkg

  $pkgId = $contentXML.project.artifactId
  $docsReadMeName = $pkgId -replace "^azure-" , ""
  $pkgVersion = $contentXML.project.version
  $groupId = if ($contentXML.project.groupId -eq $null) { $contentXML.project.parent.groupId } else { $contentXML.project.groupId }
  $releaseNotes = ""
  $readmeContent = ""

  # if it's a snapshot. return $null (as we don't want to create tags for this, but we also don't want to fail)
  if ($pkgVersion.Contains("SNAPSHOT")) {
    return $null
  }

  $changeLogLoc = @(Get-ChildItem -Path $pkg.DirectoryName -Recurse -Include "$($pkg.Basename)-changelog.md")[0]
  if ($changeLogLoc) {
    $releaseNotes = Get-ChangeLogEntryAsString -ChangeLogLocation $changeLogLoc -VersionString $pkgVersion
  }

  $readmeContentLoc = @(Get-ChildItem -Path $pkg.DirectoryName -Recurse -Include "$($pkg.Basename)-readme.md")[0]
  if ($readmeContentLoc) {
    $readmeContent = Get-Content -Raw $readmeContentLoc
  }

  return New-Object PSObject -Property @{
    PackageId      = $pkgId
    GroupId        = $groupId
    PackageVersion = $pkgVersion
    ReleaseTag     = "$($pkgId)_$($pkgVersion)"
    Deployable     = $forceCreate -or !(IsMavenPackageVersionPublished -pkgId $pkgId -pkgVersion $pkgVersion -groupId $groupId.Replace(".", "/"))
    ReleaseNotes   = $releaseNotes
    ReadmeContent  = $readmeContent
    DocsReadMeName = $docsReadMeName
  }
}

# Stage and Upload Docs to blob Storage
function Publish-java-GithubIODocs ($DocLocation, $PublicArtifactLocation)
{
  $PublishedDocs = Get-ChildItem "$DocLocation" | Where-Object -FilterScript {$_.Name.EndsWith("-javadoc.jar")}
  foreach ($Item in $PublishedDocs)
  {
    $UnjarredDocumentationPath = ""
    try
    {
      $PkgName = $Item.BaseName
      # The jar's unpacking command doesn't allow specifying a target directory
      # and will unjar all of the files in whatever the current directory is.
      # Create a subdirectory to unjar into, set the location, unjar and then
      # set the location back to its original location.
      $UnjarredDocumentationPath = Join-Path -Path $DocLocation -ChildPath $PkgName
      New-Item -ItemType directory -Path "$UnjarredDocumentationPath"
      $CurrentLocation = Get-Location
      Set-Location $UnjarredDocumentationPath
      jar -xf "$($Item.FullName)"
      Set-Location $CurrentLocation

      # If javadocs are produced for a library with source, there will always be an
      # index.html. If this file doesn't exist in the UnjarredDocumentationPath then
      # this is a sourceless library which means there are no javadocs and nothing
      # should be uploaded to blob storage.
      $IndexHtml = Join-Path -Path $UnjarredDocumentationPath -ChildPath "index.html"
      if (!(Test-Path -path $IndexHtml))
      {
        Write-Host "$($PkgName) does not have an index.html file, skipping."
        continue
      }

      # Get the POM file for the artifact we're processing
      $PomFile = $Item.FullName.Substring(0,$Item.FullName.LastIndexOf(("-javadoc.jar"))) + ".pom"
      Write-Host "PomFile $($PomFile)"

      # Pull the version from the POM
      [xml]$PomXml = Get-Content $PomFile
      $Version = $PomXml.project.version
      $ArtifactId = $PomXml.project.artifactId

      # inject the ga tag just before we upload the index to storage.
      $indexContent = Get-Content -Path $IndexHtml -Raw
      $tagContent = Get-Content -Path $CampaignTag -Raw

      $indexContent = $indexContent.Replace("</head>", $tagContent + "</head>")
      Set-Content -Path $IndexHtml -Value $indexContent -NoNewline

      Write-Host "Start Upload for $($PkgName)/$($Version)"
      Write-Host "DocDir $($UnjarredDocumentationPath)"
      Write-Host "PkgName $($ArtifactId)"
      Write-Host "DocVersion $($Version)"
      $releaseTag = RetrieveReleaseTag $PublicArtifactLocation
      Upload-Blobs -DocDir $UnjarredDocumentationPath -PkgName $ArtifactId -DocVersion $Version -ReleaseTag $releaseTag
    }
    Finally
    {
      if (![string]::IsNullOrEmpty($UnjarredDocumentationPath))
      {
        if (Test-Path -Path $UnjarredDocumentationPath)
        {
          Write-Host "Cleaning up $UnjarredDocumentationPath"
          Remove-Item -Recurse -Force $UnjarredDocumentationPath
        }
      }
    }
  }
}

function Get-java-GithubIoDocIndex()
{
  # Update the main.js and docfx.json language content
  UpdateDocIndexFiles -appTitleLang "Java"
  # Fetch out all package metadata from csv file.
  $metadata = Get-CSVMetadata -MetadataUri $MetadataUri
  # Leave the track 2 packages if multiple packages fetched out.
  $clientPackages = $metadata | Where-Object { $_.GroupId -eq 'com.azure' }
  $nonClientPackages = $metadata | Where-Object { $_.GroupId -ne 'com.azure' -and !$clientPackages.Package.Contains($_.Package) }
  $uniquePackages = $clientPackages + $nonClientPackages
  # Get the artifacts name from blob storage
  $artifacts =  Get-BlobStorage-Artifacts -blobStorageUrl $BlobStorageUrl -blobDirectoryRegex "^java/(.*)/$" -blobArtifactsReplacement '$1'
  # Build up the artifact to service name mapping for GithubIo toc.
  $tocContent = Get-TocMapping -metadata $uniquePackages -artifacts $artifacts
  # Generate yml/md toc files and build site.
  GenerateDocfxTocContent -tocContent $tocContent -lang "Java" -campaignId "UA-62780441-42"
}

# a "package.json configures target packages for all the monikers in a Repository, it also has a slightly different
# schema than the moniker-specific json config that is seen in python and js
function Update-java-CIConfig($pkgs, $ciRepo, $locationInDocRepo, $monikerId=$null)
{
  $pkgJsonLoc = (Join-Path -Path $ciRepo -ChildPath $locationInDocRepo)

  if (-not (Test-Path $pkgJsonLoc)) {
    Write-Error "Unable to locate package json at location $pkgJsonLoc, exiting."
    exit(1)
  }

  $allJsonData = Get-Content $pkgJsonLoc | ConvertFrom-Json

  $visibleInCI = @{}

  for ($i=0; $i -lt $allJsonData[$monikerId].packages.Length; $i++) {
    $pkgDef = $allJsonData[$monikerId].packages[$i]
    $visibleInCI[$pkgDef.packageArtifactId] = $i
  }

  foreach ($releasingPkg in $pkgs) {
    if ($visibleInCI.ContainsKey($releasingPkg.PackageId)) {
      $packagesIndex = $visibleInCI[$releasingPkg.PackageId]
      $existingPackageDef = $allJsonData[$monikerId].packages[$packagesIndex]
      $existingPackageDef.packageVersion = $releasingPkg.PackageVersion
    }
    else {
      $newItem = New-Object PSObject -Property @{
        packageDownloadUrl = "https://repo1.maven.org/maven2"
        packageGroupId = $releasingPkg.GroupId
        packageArtifactId = $releasingPkg.PackageId
        packageVersion = $releasingPkg.PackageVersion
        inputPath = @()
        excludePath = @()
      }

      $allJsonData[$monikerId].packages += $newItem
    }
  }

  $jsonContent = $allJsonData | ConvertTo-Json -Depth 10 | % {$_ -replace "(?m)  (?<=^(?:  )*)", "    " }

  Set-Content -Path $pkgJsonLoc -Value $jsonContent
}

$PackageExclusions = @{
  "azure-core-experimental" = "Don't want to include an experimental package.";
}

# Validates if the package will succeed in the CI build by validating the
# existence of a com folder in the unzipped source package
function SourcePackageHasComFolder($artifactNamePrefix, $packageDirectory) {
  try
  {
    $packageArtifact = "${artifactNamePrefix}:jar:sources"
    $mvnResults = mvn `
      dependency:copy `
      -Dartifact="$packageArtifact" `
      -DoutputDirectory="$packageDirectory"

    if ($LASTEXITCODE) {
      LogWarning "Could not download source artifact: $packageArtifact"
      $mvnResults | Write-Host
      return $false
    }

    $sourcesJarPath = (Get-ChildItem -File -Path $packageDirectory -Filter "*-sources.jar")[0]
    $sourcesExtractPath = Join-Path $packageDirectory "sources"
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($sourcesJarPath, $sourcesExtractPath)

    if (!(Test-Path "$sourcesExtractPath\com")) {
      LogWarning "Could not locate 'com' folder extracting $packageArtifact"
      return $false
    }
  }
  catch
  {
    LogError "Exception while updating checking if package can be documented: $($package.packageGroupId):$($package.packageArtifactId)"
    LogError $_
    LogError $_.ScriptStackTrace
    return $false
  }

  return $true
}

function PackageDependenciesResolve($artifactNamePrefix, $packageDirectory) {

  $pomArtifactName = "${artifactNamePrefix}:pom"
  $artifactDownloadOutput = mvn `
    dependency:copy `
    -Dartifact="$pomArtifactName" `
    -DoutputDirectory="$packageDirectory"

  if ($LASTEXITCODE) {
    LogWarning "Could not download pom artifact: $pomArtifactName"
    $artifactDownloadOutput | Write-Host
    return $false
  }

  $downloadedPomPath = (Get-ChildItem -File -Path $packageDirectory -Filter '*.pom')[0]

  # -P '!azure-mgmt-sdk-test-jar' excludes the unpublished test jar from
  # dependencies
  $copyDependencyOutput = mvn `
    -f $downloadedPomPath `
    dependency:copy-dependencies `
    -P '!azure-mgmt-sdk-test-jar' `
    -DoutputDirectory="$packageDirectory"

  if ($LASTEXITCODE) {
    LogWarning "Could not resolve dependencies for: $pomArtifactName"
    $copyDependencyOutput | Write-Host
    return $false
  }

  return $true
}

function ValidatePackage($groupId, $artifactId, $version, $workingDirectory) {
  $artifactNamePrefix = "${groupId}:${artifactId}:${version}"

  $packageDirectory = Join-Path `
    $workingDirectory `
    "${groupId}__${artifactId}__${version}"
  New-Item -ItemType Directory -Path $packageDirectory -Force | Out-Null

  return (SourcePackageHasComFolder $artifactNamePrefix $packageDirectory) `
    -and (PackageDependenciesResolve $artifactNamePrefix $packageDirectory)
}

function Update-java-DocsMsPackages($DocsRepoLocation, $DocsMetadata) {
  Write-Host "Excluded packages:"
  foreach ($excludedPackage in $PackageExclusions.Keys) {
    Write-Host "  $excludedPackage - $($PackageExclusions[$excludedPackage])"
  }

  # Also exclude 'spring' packages
  # https://github.com/Azure/azure-sdk-for-java/issues/23087
  $FilteredMetadata = $DocsMetadata.Where({ !($PackageExclusions.ContainsKey($_.Package) -or $_.Type -eq 'spring') })

  UpdateDocsMsPackages `
    (Join-Path $DocsRepoLocation 'package.json') `
    'preview' `
    $FilteredMetadata

  UpdateDocsMsPackages `
    (Join-Path $DocsRepoLocation 'package.json') `
    'latest' `
    $FilteredMetadata
}

function UpdateDocsMsPackages($DocConfigFile, $Mode, $DocsMetadata) {
  $packageConfig = Get-Content $DocConfigFile -Raw | ConvertFrom-Json

  $workingDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ([System.IO.Path]::GetRandomFileName())
  New-Item -ItemType Directory -Force -Path $workingDirectory | Out-Null

  $packageOutputPath = 'docs-ref-autogen'
  if ($Mode -eq 'preview') {
    $packageOutputPath = 'preview/docs-ref-autogen'
  }
  $targetPackageList = $packageConfig.Where({ $_.output_path -eq $packageOutputPath})
  if ($targetPackageList.Length -eq 0) {
    LogError "Unable to find package config for $packageOutputPath in $DocConfigFile"
    exit 1
  } elseif ($targetPackageList.Length -gt 1) {
    LogError "Found multiple package configs for $packageOutputPath in $DocConfigFile"
    exit 1
  }

  $targetPackageList = $targetPackageList[0]

  $outputPackages = @()
  foreach ($package in $targetPackageList.packages) {
    $packageGroupId = $package.packageGroupId
    $packageName = $package.packageArtifactId

    $matchingPublishedPackageArray = $DocsMetadata.Where({
      $_.Package -eq $packageName -and $_.GroupId -eq $packageGroupId
    })

    # If this package does not match any published packages keep it in the list.
    # This handles packages which are not tracked in metadata but still need to
    # be built in Docs CI.
    if ($matchingPublishedPackageArray.Count -eq 0) {
      Write-Host "Keep non-tracked package: $packageName"
      $outputPackages += $package
      continue
    }

    if ($matchingPublishedPackageArray.Count -gt 1) {
      LogWarning "Found more than one matching published package in metadata for $packageName; only updating first entry"
    }
    $matchingPublishedPackage = $matchingPublishedPackageArray[0]

    if ($Mode -eq 'preview' -and !$matchingPublishedPackage.VersionPreview.Trim()) {
      # If we are in preview mode and the package does not have a superseding
      # preview version, remove the package from the list.
      Write-Host "Remove superseded preview package: $packageName"
      continue
    }

    if ($Mode -eq 'latest' -and !$matchingPublishedPackage.VersionGA.Trim()) {
      LogWarning "Metadata is missing GA version for GA package $packageName. Keeping existing package."
      $outputPackages += $package
      continue
    }

    $packageVersion = $($matchingPublishedPackage.VersionGA)
    if ($Mode -eq 'preview') {
      if (!$matchingPublishedPackage.VersionPreview.Trim()) {
        LogWarning "Metadata is missing preview version for preview package $packageName. Keeping existing package."
        $outputPackages += $package
        continue
      }
      $packageVersion = $matchingPublishedPackage.VersionPreview
    }

    # If upgrading the package, run basic sanity checks against the package
    if ($package.packageVersion -ne $packageVersion) {
      Write-Host "Validating new version detected for $packageName ($packageVersion)"
      $validatePackageResult = ValidatePackage $package.packageGroupId $package.packageArtifactId $packageVersion $workingDirectory

      if (!$validatePackageResult) {
        LogWarning "Package is not valid: $packageName. Keeping old version."
        $outputPackages += $package
        continue
      }

      $package.packageVersion = $packageVersion
    }

    Write-Host "Keeping tracked package: $packageName."
    $outputPackages += $package
  }

  $outputPackagesHash = @{}
  foreach ($package in $outputPackages) {
    $outputPackagesHash["$($package.packageGroupId):$($package.packageArtifactId)"] = $true
  }

  $remainingPackages = @()
  if ($Mode -eq 'preview') {
    $remainingPackages = $DocsMetadata.Where({
      $_.VersionPreview.Trim() -and !$outputPackagesHash.ContainsKey("$($package.packageGroupId):$($package.packageArtifactId)")
    })
  } else {
    $remainingPackages = $DocsMetadata.Where({
      $_.VersionGA.Trim() -and !$outputPackagesHash.ContainsKey("$($package.packageGroupId):$($package.packageArtifactId)")
    })
  }

  # Add packages that exist in the metadata but are not onboarded in docs config
  foreach ($package in $remainingPackages) {
    $packageName = $package.Package
    $packageGroupId = $package.GroupId
    $packageVersion = $package.VersionGA
    if ($Mode -eq 'preview') {
      $packageVersion = $package.VersionPreview
    }

    Write-Host "Validating new package $($packageGroupId):$($packageName):$($packageVersion)"
    $validatePackageResult = ValidatePackage ValidatePackage $package.packageGroupId $package.packageArtifactId $packageVersion $workingDirectory
    if (!$validatePackageResult) {
      LogWarning "Package is not valid: ${packageGroupId}:$packageName. Cannot onboard."
      continue
    }

    Write-Host "Add new package from metadata: ${packageGroupId}:$packageName"
    $package = [ordered]@{
      packageArtifactId = $packageArtifactId
      packageGroupId = $packageGroupId
      packageVersion = $packageVersion
      packageDownloadUrl = "https://repo1.maven.org/maven2"
    }

    $outputPackages += $package
  }

  $targetPackageList.packages = $outputPackages

  # It is assumed that there is a matching config from above when the number of
  # matching $targetPackageList is 1
  foreach ($config in $packageConfig) {
    if ($config.output_path -eq $packageOutputPath) {
      $config = $targetPackageList
      break
    }
  }

  $outputJson = ConvertTo-Json $packageConfig -Depth 100
  Set-Content -Path $DocConfigFile -Value $outputJson
  Write-Host "Onboarding configuration written to: $DocConfigFile"
}

# function is used to filter packages to submit to API view tool
function Find-java-Artifacts-For-Apireview($artifactDir, $pkgName)
{
  # Find all source jar files in given artifact directory
  # Filter for package in "com.azure*" groupid.
  $artifactPath = Join-Path $artifactDir "com.azure*" $pkgName
  Write-Host "Checking for source jar in artifact path $($artifactPath)"
  $files = Get-ChildItem -Recurse "${artifactPath}" | Where-Object -FilterScript {$_.Name.EndsWith("sources.jar")}
  if (!$files)
  {
    Write-Host "$($artifactPath) does not have any package"
    return $null
  }
  elseif($files.Count -ne 1)
  {
    Write-Host "$($artifactPath) should contain only one (1) published source jar package"
    Write-Host "No of Packages $($files.Count)"
    return $null
  }

  $packages = @{
    $files[0].Name = $files[0].FullName
  }

  return $packages
}

function SetPackageVersion ($PackageName, $Version, $ServiceDirectory, $ReleaseDate, $BuildType = "client", $GroupId = "com.azure", $PackageProperties)
{
  if ($PackageProperties)
  {
    $GroupId = $PackageProperties.Group
    if ($PackageProperties.SdkType -eq "client")
    {
      if ($PackageProperties.IsNewSDK) {
        $BuildType = "client"
      }
      else {
        $BuildType = "data"
      }
    }
  }

  if($null -eq $ReleaseDate)
  {
    $ReleaseDate = Get-Date -Format "yyyy-MM-dd"
  }
  python "$EngDir/versioning/set_versions.py" --build-type $BuildType --new-version $Version --ai $PackageName --gi $GroupId
  python "$EngDir/versioning/update_versions.py" --update-type library --build-type $BuildType --sr
  & "$EngCommonScriptsDir/Update-ChangeLog.ps1" -Version $Version -ServiceDirectory $ServiceDirectory -PackageName $PackageName `
  -Unreleased $False -ReplaceLatestEntryTitle $True -ReleaseDate $ReleaseDate
}

function GetExistingPackageVersions ($PackageName, $GroupId=$null)
{
  try {
    $Uri = 'https://search.maven.org/solrsearch/select?q=g:"' + $GroupId + '"+AND+a:"' + $PackageName +'"&core=gav&rows=20&wt=json'
    $existingVersion = Invoke-RestMethod -Method GET -Uri $Uri
    $existingVersion = $existingVersion.response.docs.v
    [Array]::Reverse($existingVersion)
    return $existingVersion
  }
  catch {
    LogError "Failed to retrieve package versions. `n$_"
    return $null
  }
}
