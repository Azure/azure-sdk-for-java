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
  $uri = "https://oss.sonatype.org/content/repositories/releases/$($groupId.Replace('.', '/'))/$pkgId/$pkgVersion/$pkgId-$pkgVersion.pom"

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

      Write-Host "Http request for maven package $groupId`:$pkgId`:$pkgVersion failed attempt $attempt with statuscode $statusCode"
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
    $namespaces = Fetch-Namespaces-From-Javadoc $packageInfo.Name $packageInfo.Group $version
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
  $clientPackages = $metadata | Where-Object { $_.GroupId -eq 'com.azure' }
  $nonClientPackages = $metadata | Where-Object { $_.GroupId -ne 'com.azure' -and !$clientPackages.Package.Contains($_.Package) }
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
  # skip azure-cosmos-test package because it needs to be releaesd
  if ($pkgName.Contains("azure-cosmos-test")) {
    return $null
  }

  # Find all source jar files in given artifact directory
  # Filter for package in "com.azure*" groupid.
  $artifactPath = Join-Path $artifactDir "com.azure*" $pkgName
  Write-Host "Checking for source jar in artifact path $($artifactPath)"
  $files = @(Get-ChildItem -Recurse "${artifactPath}" | Where-Object -FilterScript {$_.Name.EndsWith("sources.jar")})
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
  python "$EngDir/versioning/set_versions.py" --build-type $BuildType --new-version $Version --ai $PackageName --gi $GroupId
  # -ll option says "only update README and CHANGELOG entries for libraries that are on the list"
  python "$EngDir/versioning/update_versions.py" --update-type library --build-type $BuildType --ll $fullLibraryName
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
      $existingVersion = $response.docs.v
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

  # This logic eliminates a preceeding "azure-" in the readme filename.
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
            Write-Host "Found tsp-location.yaml in $directory, using typespec to generate projects"
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

