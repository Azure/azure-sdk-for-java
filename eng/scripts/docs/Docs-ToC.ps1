function Get-java-OnboardedDocsMsPackages($DocRepoLocation) {
    $packageOnboardingFiles = @(
      "$DocRepoLocation/package.json")
  
    $onboardedPackages = @{}
    foreach ($file in $packageOnboardingFiles) {
      $onboardingSpec = ConvertFrom-Json (Get-Content $file -Raw)
      foreach ($spec in $onboardingSpec.packages) {
        $packageName = $spec.packageArtifactId
        $spec = @{
            "packageArtifactId" = $spec.packageArtifactId
            "packageGroupId" = $spec.packageGroupId
            "packageVersion" = $spec.packageVersion
            "packageDownloadUrl" = $spec.packageDownloadUrl

        }
        $onboardedPackages[$packageName] = $spec
      }
    }
  
    return $onboardedPackages
  }
  
  function Get-java-DocsMsTocData($packageMetadata, $docRepoLocation) {
    $packageLevelReadmeName = $packageMetadata.Package.Replace('@azure/', '').Replace('@azure-tools/', '').Replace('azure-', '');
  
    if ($packageMetadata.Package.StartsWith('@azure-rest/')) {
      $packageLevelReadmeName = "$($packageMetadata.Package.Replace('@azure-rest/', ''))-rest"
  
      # TODO: Consider using metadata in doc repo /metadata folder to get
      # DirectoryPath. Do not use DirectoryPath from metadata CSV
    }
  
    $packageTocHeader = $packageMetadata.Package
    if ($clientPackage.DisplayName) {
      $packageTocHeader = $clientPackage.DisplayName
    }
    $output = [PSCustomObject]@{
      PackageLevelReadmeHref = "~/docs-ref-services/{moniker}/$packageLevelReadmeName-readme.md"
      PackageTocHeader       = $packageTocHeader
      TocChildren            = @($clientPackage.Package)
    }
  
    return $output
  }
  
  function Get-java-DocsMsTocChildrenForManagementPackages($packageMetadata, $docRepoLocation) {
    # Download Javadoc
    $artifact = "$($packageMetadata.packageGroupId):$(packageMetadata.packageArtifactId):$(packageMetadata.packageVersion)"
    $packageArtifact = "${artifactNamePrefix}:jar:sources"
    $mvnResults = mvn `
      dependency:copy `
      -Dartifact="$packageArtifact" `
      -DoutputDirectory="$packageDirectory" 
    # Extract

    # Read element-list
    return @($packageMetadata.Package)
  }
  