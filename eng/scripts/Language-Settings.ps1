$Language = "java"
$LanguageDisplayName = "Java"
$PackageRepository = "Maven"
$packagePattern = "*.pom"
$MetadataUri = "https://raw.githubusercontent.com/Azure/azure-sdk/main/_data/releases/latest/java-packages.csv"
$CampaignTag = Resolve-Path (Join-Path -Path $PSScriptRoot -ChildPath "../repo-docs/ga_tag.html")
$GithubUri = "https://github.com/Azure/azure-sdk-for-java"
$PackageRepositoryUri = "https://repo1.maven.org/maven2"

. "$PSScriptRoot/docs/Docs-ToC.ps1"
. "$PSScriptRoot/docs/Docs-Onboarding.ps1"

# When getting all of the package properties, if Get-AllPackageInfoFromRepo exists
# then it's called instead of Get-PkgPropsForEntireService.
function Get-AllPackageInfoFromRepo([string]$serviceDirectory = $null) {

  $SdkType = $Env:SDKTYPE
  if ($SdkType) {
    Write-Verbose "SdkType env var was set to '$SdkType'"
  } else {
    $SdkType = "client"
    Write-Verbose "SdkType env var was not set, default to 'client'"
  }
  Write-Verbose "Processing SdkType=$SdkType"

  $allPackageProps = @()
  $sdkRoot = Join-Path $RepoRoot "sdk"
  $ymlFiles = @()

  if ($serviceDirectory) {
    $searchPath = Join-Path $sdkRoot $serviceDirectory
    Write-Verbose "searchPath=$searchPath"
    [array]$ymlFiles = Get-ChildItem -Path $searchPath "ci*.yml" | Where-Object { $_.PSIsContainer -eq $false}
  } else {
    # The reason for the exclude folders are POM only releases (nothing is built) or
    # the service directory sits outside of the engineering system
    # 1. boms - BOMs are POM only releases. Also, their versions aren't version controlled.
    # 2. parents - parents are POM only releases which are version controlled
    # 3. resourcemanagerhybrid - intermediate version of resourcemanager that was
    #    a one time release which sits outside of the engineering system
    $excludeFolders = "boms", "resourcemanagerhybrid", "parents"
    [array]$ymlFiles = Get-ChildItem -Path $sdkRoot -Include "ci*.yml" -Recurse -Depth 3 | Where-Object { $_.PSIsContainer -eq $false -and $_.DirectoryName -notmatch ($excludeFolders -join "|") }
  }

  foreach ($ymlFile in $ymlFiles)
  {
    # For each yml file do the following:
    # 1. Load the yml file.
    # 2. Get the Artifacts and AdditionalModules for the yml file.
    # 3. Get the path to the yml
    # 4. From the path in #1, recursively get a list of all pom.xml with depth 1. This
    #    will take care of the cases where the pom.xml is sitting next to the ci.yml, in
    #    the case where the ci.yml is in the sdk/<ServiceDirectory>/<LibraryDirectory>,
    #    as well as the case where the ci.yml is in the sdk/<ServiceDirectory>
    # 5. For each pom file, check and see if its group:artifact matches a name/groupId in
    #    the ci.yml's Artifacts list.
    # 6a. If #5 has a match, create the PackageProp and add it to the list
    # 6b. If #5 doesn't have a match, then skip it. This is the case where it's either
    #     an AdditionalModule or something from another track.
    Write-Verbose "Processing $ymlFile"
    $ymlFileContent = LoadFrom-Yaml $ymlFile
    $YmlFileSdkType = GetValueSafelyFrom-Yaml $ymlFileContent @("extends", "parameters", "SDKType")
    $ymlDir = Split-Path -Path $ymlFile -Parent
    # the default, if not set in the yml file, is client
    if (-not $YmlFileSdkType) {
      $YmlFileSdkType = "client"
    }
    if ($YmlFileSdkType -ne $SdkType) {
      Write-Verbose "SdkType in yml file is '$YmlFileSdkType' which is not '$SdkType', skipping..."
      continue
    }
    # ServiceDirectory
    $serviceDirFromYml = GetValueSafelyFrom-Yaml $ymlFileContent @("extends", "parameters", "ServiceDirectory")
    if (-not $serviceDirFromYml) {
      # Log the error and skip this yml file here if there's no ServiceDirectory entry
      LogWarning "$ymlFile does not have a ServiceDirectory entry, skipping..."
      continue
    }
    else {
      # If the serviceDirectory parameter was passed in, ensure that the ServiceDirectory
      # entry in the ci*.yml file matches the one passed in.
      if ($serviceDirectory) {
        if ($serviceDirectory -ne $serviceDirFromYml) {
          LogWarning "$ymlFile's ServiceDirectory entry does not match the serviceDirectory parameter: '$serviceDirectory'"
        }
      }
      # Check whether or not the yml's serviceDirectory matches the actual path of
      # the yml file relative to the sdkRoot
      # Note: Need to strip off the directory separator character which is based upon the OS
      $computedServiceDirectory = $ymlDir.Replace($sdkRoot + [System.IO.Path]::DirectorySeparatorChar, "")
      # .Replace and -replace have different behaviors. .Replace will not replace a backslash meaning
      # that "foo\bar".Replace("\\","/") would result in foo\bar instead of foo/bar which is why
      # -replace needs to be used
      $computedServiceDirectory = $computedServiceDirectory -replace "\\", "/"
      if ($serviceDirFromYml -ne $computedServiceDirectory) {
        LogWarning "$ymlFile error: ServiceDirectory in the yml file, '$serviceDirFromYml' doesn't match the path relative from the sdkRoot '$computedServiceDirectory'"
      }
    }
    # At this point the SdkType is correct.
    # 1. Create a hash set from the list of artifact for this service directory, for
    #    the SdkType. This is done to ensure that only PackageInfo files are created
    #    for the correct set of Artifacts
    # 2. Save off AdditionalModules to add to the AdditionalValidationPackages entry
    $ArtifactsHashSet = New-Object 'System.Collections.Generic.HashSet[String]'
    $artifacts = GetValueSafelyFrom-Yaml $ymlFileContent @("extends", "parameters", "Artifacts")
    $additionalModules = GetValueSafelyFrom-Yaml $ymlFileContent @("extends", "parameters", "AdditionalModules")
    foreach ($artifact in $artifacts)
    {
      $hashKey = "$($artifact.groupId):$($artifact.name)"
      if (-not $ArtifactsHashSet.Add($hashKey)) {
        LogWarning "ymlFile: $ymlFile contains a duplicate artifact $hashKey"
      }
    }
    # Grab all the pom files in the yml file's directory and subdirectory depth of 1
    # because they're either going to be in the same directory as the yml files or 1
    # level lower. The repository can have sdk/SD1/ci.yml and sdk/SD1/Lib1/ci.yml files
    # each processing more different Artifacts and this is the reason that the ArtifactsHash
    # is necessary to verify.
    # Note: depth of 0 is the current directory, depth of 1 is current and the first level
    # of subdirectories. In the case where we have a ci.yml file for a single library the
    # library's pom file will sit next to yml file. In the case where the ci*.yml file is in
    # the root of the service directory, the pom files should be the immediate subdirectories.
    [array]$pomFiles = Get-ChildItem -Path $ymlDir -Recurse -Depth 1 -File -Filter "pom.xml"
    foreach ($pomFile in $pomFiles) {
      $xmlPomFile = New-Object xml
      $xmlPomFile.Load($pomFile)

      if ($xmlPomFile.project.psobject.properties.name -notcontains "artifactId" -or !$xmlPomFile.project.artifactId) {
        Write-Verbose "$pomFile doesn't have a defined artifactId so skipping this pom."
        continue
      }

      if ($xmlPomFile.project.psobject.properties.name -notcontains "version" -or !$xmlPomFile.project.version) {
        Write-Verbose "$pomFile doesn't have a defined version so skipping this pom."
        continue
      }

      if ($xmlPomFile.project.psobject.properties.name -notcontains "groupid" -or !$xmlPomFile.project.groupId) {
        Write-Verbose "$pomFile doesn't have a defined groupId so skipping this pom."
        continue
      }

      # The case where ArtifactsHashSet won't contain the key from the pom.xml file is when
      # the pom file being processed is one of the intermediate poms in the hierarchy. An
      # example would be sdk/core/pom.xml which just contains the module listings
      $keyFromPom = "$($xmlPomFile.project.groupId):$($xmlPomFile.project.artifactId)"
      if (-not $ArtifactsHashSet.Contains($keyFromPom))
      {
        Write-Verbose "$ymlFile does not contain $($xmlPomFile.project.groupId):$($xmlPomFile.project.artifactId), skipping"
        continue
      }
      # At this point everything is valid
      # 1. Create the packageProps
      # 2. Set the SdkType
      # 3. Set isNewSdk
      # 4. Set the artifactName
      # 5. Set AdditionalValidationPackages to the AdditionalModules from the yml file
      $groupId = $xmlPomFile.project.groupId
      $artifactId = $xmlPomFile.project.artifactId
      $version = $xmlPomFile.project.version
      $pomFileDir = Split-Path -Path $pomFile -Parent
      $pkgProp = [PackageProps]::new($artifactId, $version.ToString(), $pomFileDir, $serviceDirFromYml, $groupId, $artifactId)
      if ($artifactId -match "mgmt" -or $artifactId -match "resourcemanager")
      {
        $pkgProp.SdkType = "mgmt"
      }
      elseif ($artifactId -match "spring")
      {
        $pkgProp.SdkType = "spring"
      }
      else
      {
        $pkgProp.SdkType = "client"
      }
      $pkgProp.IsNewSdk = $False
      if ($groupId) {
        $pkgProp.IsNewSdk = $groupId.StartsWith("com.azure") -or $groupId.StartsWith("io.clientcore")
      }
      $pkgProp.ArtifactName = $artifactId
      if ($additionalModules) {
        # $additionalModules' type is System.Object[] where the objects are are
        # hashtables containing two values, the name and the groupId. Create a
        # list of the AdditionalModules and set the PackageProp.AdditionalValidationPackages
        # to that list
        $additionalModulesList = @()
        foreach ($additionalModule in $additionalModules) {
          $additionalModulesList += "$($additionalModule['groupId']):$($additionalModule['name'])"
        }
        $pkgProp.AdditionalValidationPackages = $additionalModulesList
      }
      $allPackageProps += $pkgProp
    }
  }
  return $allPackageProps
}

# Get-java-AdditionalValidationPackagesFromPackageSet is the implementation of the
# $AdditionalValidationPackagesFromPackageSetFn which is used
function Get-java-AdditionalValidationPackagesFromPackageSet {
  param(
    [Parameter(Mandatory=$true)]
    $LocatedPackages,
    [Parameter(Mandatory=$true)]
    $diffObj,
    [Parameter(Mandatory=$true)]
    $AllPkgProps
  )
  $uniqueResultSet = @()

  # this section will identify the list of packages that we should treat as
  # "directly" changed for a given service level change. While that doesn't
  # directly change a package within the service, I do believe we should directly include all
  # packages WITHIN that service. This is because the service level file changes are likely to
  # have an impact on the packages within that service.
  $changedServices = @()
  $targetedFiles = $diffObj.ChangedFiles
  if ($diff.DeletedFiles) {
    if (-not $targetedFiles) {
      $targetedFiles = @()
    }
    $targetedFiles += $diff.DeletedFiles
  }

  # The targetedFiles needs to filter out anything in the ExcludePaths
  # otherwise it'll end up processing things below that it shouldn't be.
  foreach ($excludePath in $diffObj.ExcludePaths) {
    $targetedFiles = $targetedFiles | Where-Object { -not $_.StartsWith($excludePath) }
  }

  if ($targetedFiles) {
    foreach($file in $targetedFiles) {
      $pathComponents = $file -split "/"
      # Handle changes in the root of any sdk/<ServiceDirectory>. Unfortunately, changes
      # in the root service directory require any and all libraries in that service directory,
      # include those in a <ServiceDirectory>/<LibraryDirectory> to get added to the changed
      # services.
      if ($pathComponents.Length -eq 3 -and $pathComponents[0] -eq "sdk") {
        $changedServices += $pathComponents[1]
      }

      # For anything in the root of the sdk directory, or the repository root, just run template
      if (($pathComponents.Length -eq 2 -and $pathComponents[0] -eq "sdk") -or
          ($pathComponents.Length -eq 1)) {
        $changedServices += "template"
      }
    }
    # dedupe the changedServices list
    $changedServices = $changedServices | Get-Unique
    foreach ($changedService in $changedServices) {
      # Because Java has libraries at the sdk/<ServiceDirectory> and sdk/<ServiceDirectory>/<Library>
      # directories, the additional package lookup needs to for ci*.yml files where the ServiceDirectory
      # equals the $changedService as well as ServiceDirectories that starts $changedService/, note the
      # trailing slash is necessary. For example, if PR changes the ServiceDirectory foo and there
      # exist ci.yml files with the service directories "foo/bar" and "foobar", we only want to match
      # foo and foo/bar, not foobar hence the -eq $changedService and StartsWith("$changedService/")
      $additionalPackages = $AllPkgProps | Where-Object { $_.ServiceDirectory -eq $changedService -or $_.ServiceDirectory.StartsWith("$changedService/")}
      foreach ($pkg in $additionalPackages) {
        if ($uniqueResultSet -notcontains $pkg -and $LocatedPackages -notcontains $pkg) {
          # IncludedForValidation means that it's package that was indirectly included because it
          # wasn't directly changed. For example, if someone changes a file in the root of sdk/core
          # we add all of the core libraries that do not have direct changes as indirect packages.
          $pkg.IncludedForValidation = $true
          $uniqueResultSet += $pkg
        }
      }
    }
  }

  Write-Host "Returning additional packages for validation: $($uniqueResultSet.Count)"
  foreach ($pkg in $uniqueResultSet) {
    Write-Host "  - $($pkg.Name)"
  }

  return $uniqueResultSet
}

# Returns the maven (really sonatype) publish status of a package id and version.
function IsMavenPackageVersionPublished($pkgId, $pkgVersion, $groupId)
{
  # oss.sonatype.org seems to have started returning 403 for our agents. Based on https://central.sonatype.org/faq/403-error-central it is likely
  # because some agent is trying to query the directory too frequently. So we will attempt to query the raw maven repo itself.
  # $uri = "https://oss.sonatype.org/content/repositories/releases/$($groupId.Replace('.', '/'))/$pkgId/$pkgVersion/$pkgId-$pkgVersion.pom"
  $uri = "https://repo1.maven.org/maven2/$($groupId.Replace('.', '/'))/$pkgId/$pkgVersion/$pkgId-$pkgVersion.pom"

  $attempt = 1
  while ($attempt -le 3)
  {
    try
    {
      if ($attempt -gt 1) {
        Start-Sleep -Seconds ([Math]::Pow(2, $attempt))
      }

      Write-Host "Checking published package at $uri"
      $response = Invoke-WebRequest -Method "GET" -uri $uri -SkipHttpErrorCheck

      if ($response.BaseResponse.IsSuccessStatusCode)
      {
        return $true
      }

      $statusCode = $response.StatusCode

      if ($statusCode -eq 404)
      {
        return $false
      }

      Write-Host "Http request for maven package $groupId`:$pkgId`:$pkgVersion failed attempt $attempt with status code $statusCode"
    }
    catch
    {
      Write-Host "Http request for maven package $groupId`:$pkgId`:$pkgVersion failed attempt $attempt with exception $($_.Exception.Message)"
    }

    $attempt += 1
  }

  throw "Http request for maven package $groupId`:$pkgId`:$pkgVersion failed after 3 attempts"
}

# Parse out package publishing information given a maven POM file
function Get-java-PackageInfoFromPackageFile ($pkg, $workingDirectory)
{
  [xml]$contentXML = Get-Content $pkg

  $pkgId = $contentXML.project.artifactId
  $docsReadMeName = $pkgId -replace "^azure-" , ""
  $pkgVersion = $contentXML.project.version
  $groupId = if ($null -eq $contentXML.project.groupId) { $contentXML.project.parent.groupId } else { $contentXML.project.groupId }
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

# Defined in common.ps1 as:
# $GetDocsMsDevLanguageSpecificPackageInfoFn = "Get-${Language}-DocsMsDevLanguageSpecificPackageInfo"
function Get-java-DocsMsDevLanguageSpecificPackageInfo($packageInfo, $packageSourceOverride) {
  # If the default namespace isn't in the package info then it needs to be added
  # Can't check if (!$packageInfo.Namespaces) in strict mode because Namespaces won't exist
  # at all.
  if (!($packageInfo | Get-Member Namespaces)) {
    $version = $packageInfo.Version
    # If the dev version is set, use that
    if ($packageInfo.DevVersion) {
      $version = $packageInfo.DevVersion
    }
    $namespaces = Fetch-Namespaces-From-Javadoc $packageInfo.ArtifactName $packageInfo.Group $version
    # If there are namespaces found from the javadoc.jar then add them to the packageInfo which
    # will later update the metadata json file in the docs repository. If there aren't any namespaces
    # then don't add the namespaces member with an empty list. The reason being is that the
    # UpdateDocsMsMetadataForPackage function will merge the packageInfo json and the metadata json
    # files by taking any properties in the metadata json file that aren't in the packageInfo and add
    # them from the metadata. This allows us to set the namespaces for things that can't be figured out
    # through the javadoc, like track 1 libraries whose javadoc.jar files don't contain anything, in
    # the metadata json files.
    if ($namespaces.Count -gt 0) {
      Write-Host "Get-java-DocsMsDevLanguageSpecificPackageInfo:adding namespaces property with the following namespaces:"
      $namespaces | Write-Host
      $packageInfo | Add-Member -Type NoteProperty -Name "Namespaces" -Value $namespaces
    } else {
      Write-Host "Get-java-DocsMsDevLanguageSpecificPackageInfo: no namespaces to add"
    }
  }
  return $packageInfo
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
  $clientPackages = $metadata | Where-Object { $_.GroupId -eq 'com.azure' -or $_.GroupId -eq 'com.azure.v2' }
  $nonClientPackages = $metadata | Where-Object { $_.GroupId -ne 'com.azure' -and $_.GroupId -ne 'com.azure.v2' -and !$clientPackages.Package.Contains($_.Package) }
  $uniquePackages = $clientPackages + $nonClientPackages
  # Get the artifacts name from blob storage
  $artifacts =  Get-BlobStorage-Artifacts `
    -blobDirectoryRegex "^java/(.*)/$" `
    -blobArtifactsReplacement '$1' `
    -storageAccountName 'azuresdkdocs' `
    -storageContainerName '$web' `
    -storagePrefix 'java/'

  # Build up the artifact to service name mapping for GithubIo toc.
  $tocContent = Get-TocMapping -metadata $uniquePackages -artifacts $artifacts
  # Generate yml/md toc files and build site.
  GenerateDocfxTocContent -tocContent $tocContent -lang "Java" -campaignId "UA-62780441-42"
}

# function is used to filter packages to submit to API view tool
function Find-java-Artifacts-For-Apireview($artifactDir, $pkgName)
{
  # skip spark packages
  if ($pkgName.Contains("-spark")) {
    return $null
  }
  # skip azure-cosmos-test package because it needs to be released
  if ($pkgName.Contains("azure-cosmos-test")) {
    return $null
  }

  # Find all source jar files in given artifact directory
  # Filter for package in "com.azure*" groupId.
  $artifactPath = Join-Path $artifactDir "com.azure*" $pkgName
  Write-Host "Checking for source jar in artifact path $($artifactPath)"
  $files = @(Get-ChildItem -Recurse "${artifactPath}" | Where-Object -FilterScript {$_.Name.EndsWith("sources.jar")})
  # And filter for packages in "io.clientcore*" groupId.
  # (Is there a way to pass more information here to know the explicit groupId?)
  $artifactPath = Join-Path $artifactDir "io.clientcore*" $pkgName
  $files += @(Get-ChildItem -Recurse "${artifactPath}" | Where-Object -FilterScript {$_.Name.EndsWith("sources.jar")})
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

function SetPackageVersion ($PackageName, $Version, $ServiceDirectory, $ReleaseDate, $ReplaceLatestEntryTitle=$true, $BuildType = "client", $GroupId = "com.azure", $PackageProperties)
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
  $fullLibraryName = $GroupId + ":" + $PackageName
  python "$EngDir/versioning/set_versions.py" --new-version $Version --artifact-id $PackageName --group-id $GroupId
  # -ll option says "only update README and CHANGELOG entries for libraries that are on the list"
  python "$EngDir/versioning/update_versions.py" --library-list $fullLibraryName
  & "$EngCommonScriptsDir/Update-ChangeLog.ps1" -Version $Version -ServiceDirectory $ServiceDirectory -PackageName $PackageName `
  -Unreleased $False -ReplaceLatestEntryTitle $ReplaceLatestEntryTitle -ReleaseDate $ReleaseDate
}

function GetExistingPackageVersions ($PackageName, $GroupId=$null)
{
  try {
    $Uri = 'https://search.maven.org/solrsearch/select?q=g:"' + $GroupId + '"+AND+a:"' + $PackageName +'"&core=gav&rows=20&wt=json'
    $response = (Invoke-RestMethod -Method GET -Uri $Uri).response
    if($response.numFound -ne 0)
    {
      $existingVersion = @($response.docs.v)
      if ($existingVersion.Count -gt 0)
      {
        [Array]::Reverse($existingVersion)
        return $existingVersion
      }
    }
    return $null
  }
  catch {
    LogError "Failed to retrieve package versions for ${PackageName}. $($_.Exception.Message)"
    return $null
  }
}

# Defined in common.ps1
# $GetDocsMsMetadataForPackageFn = "Get-${Language}-DocsMsMetadataForPackage"
function Get-java-DocsMsMetadataForPackage($PackageInfo) {
  $readmeName = $PackageInfo.Name.ToLower()
  Write-Host "Docs.ms Readme name: $($readmeName)"

  # Readme names (which are used in the URL) should not include redundant terms
  # when viewed in URL form. For example:
  # https://review.docs.microsoft.com/en-us/java/api/overview/azure/storage-blob-readme
  # Note how the end of the URL doesn't look like:
  # ".../azure/azure-storage-blobs-readme"

  # This logic eliminates a preceding "azure-" in the readme filename.
  # "azure-storage-blobs" -> "storage-blobs"
  if ($readmeName.StartsWith('azure-')) {
    $readmeName = $readmeName.Substring(6)
  }

  New-Object PSObject -Property @{
    DocsMsReadMeName = $readmeName
    LatestReadMeLocation  = 'docs-ref-services/latest'
    PreviewReadMeLocation = 'docs-ref-services/preview'
    LegacyReadMeLocation  = 'docs-ref-services/legacy'
    Suffix = ''
  }
}

function Get-java-EmitterName() {
  return "@azure-tools/typespec-java"
}

function Get-java-EmitterAdditionalOptions([string]$projectDirectory) {
  return "--option @azure-tools/typespec-java.emitter-output-dir=$projectDirectory/"
}

function Get-java-DirectoriesForGeneration() {
    $sdkDirectories = Get-ChildItem -Path "$RepoRoot/sdk" -Directory | Get-ChildItem -Directory

    return $sdkDirectories | Where-Object {
        (Test-Path -Path "$_/tsp-location.yaml") -or
        (Test-Path -Path "$_/swagger/Update-Codegeneration.ps1")
    }
}

function Update-java-GeneratedSdks([string]$PackageDirectoriesFile) {
  $packageDirectories = Get-Content $PackageDirectoriesFile | ConvertFrom-Json

  foreach ($directory in $packageDirectories) {
    Push-Location $RepoRoot
    try {
        $tspLocationFile = Get-Item -Path "sdk/$directory/tsp-location.yaml" -ErrorAction SilentlyContinue
        $updateScript = Get-Item -Path "sdk/$directory/swagger/Update-CodeGeneration.ps1" -ErrorAction SilentlyContinue

        if ($tspLocationFile) {
            Write-Host "Found tsp-location.yaml in $directory, using TypeSpec to generate projects"
            ./eng/common/scripts/TypeSpec-Project-Sync.ps1 "sdk/$directory"
            ./eng/common/scripts/TypeSpec-Project-Generate.ps1 "sdk/$directory"
        } elseif ($updateScript) {
            Write-Host "Using $updateScript to generate projects"
            & $updateScript.FullName
        } else {
            Write-Host "No tsp-location.yaml or swagger/Update-Codegeneration.ps1 found in $directory, skipping"
        }
    }
    finally {
      Pop-Location
    }
  }
}

function Get-java-ApiviewStatusCheckRequirement($packageInfo) {
  if ($packageInfo.IsNewSdk -and ($packageInfo.SdkType -eq "client" -or $packageInfo.SdkType -eq "spring")) {
    return $true
  }
  return $false
}
