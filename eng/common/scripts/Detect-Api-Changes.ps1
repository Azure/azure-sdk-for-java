[CmdletBinding()]
Param (
  [Parameter(Mandatory=$True)]
  [string] $ArtifactPath,
  [Parameter(Mandatory=$True)]
  [string] $PullRequestNumber,
  [Parameter(Mandatory=$True)]
  [string] $BuildId,
  [Parameter(Mandatory=$True)]
  [string] $CommitSha,
  [Parameter(Mandatory=$True)]
  [string] $ArtifactList,
  [string] $ArtifactName = "packages",
  [string] $APIViewUri = "http://apiviewstaging.azurewebsites.net/PullRequest/DetectApiChanges"
)

# Submit API review request and return status whether current revision is approved or pending or failed to create review
function Submit-Request($filePath)
{
    $queryParam = "artifactName=$ArtifactName&buildId=$BuildId&filePath=$filePath&commitSha=$CommitSha&language=$Language&pullRequestNumber=$PullRequestNumber"    
    $uri= "$($APIViewUri)?$($queryParam)"
    Write-Host "Request URI: $uri"
    try
    {
        $Response = Invoke-WebRequest -Method 'GET' -Uri $uri
        $StatusCode = $Response.StatusCode
    }
    catch
    {
        Write-Host "Exception details: $($_.Exception.Response)"
        $StatusCode = $_.Exception.Response.StatusCode
    }

    return $StatusCode
}

function Shoud-Process-Package($pkgPath, $packageName)
{
    $pkg = Split-Path -Leaf $pkgPath
    $configFileDir = Join-Path -Path $ArtifactPath "PackageInfo"
    $pkgPropPath = Join-Path -Path $configFileDir "$packageName.json"
    if (-Not (Test-Path $pkgPropPath))
    {
        Write-Host " Package property file path $($pkgPropPath) is invalid."
        return $False
    }
    # Get package info from json file created before updating version to daily dev
    $pkgInfo = Get-Content $pkgPropPath | ConvertFrom-Json
    Write-Host "SDK Type: $($pkgInfo.SdkType)"
    return ($pkgInfo.SdkType -eq "client" -and $pkgInfo.IsNewSdk)
}

function Log-Input-Params()
{
    Write-Host "Artifact Path: $($ArtifactPath)"
    Write-Host "Artifact Name: $($ArtifactName)"
    Write-Host "PullRequest Number: $($PullRequestNumber)"
    Write-Host "BuildId: $($BuildId)"
    Write-Host "Language: $($Language)"
    Write-Host "Commit SHA: $($CommitSha)"
    Write-Host "ArtifactJson: $($ArtifactJson)"
}

. (Join-Path $PSScriptRoot common.ps1)
Log-Input-Params
$ArtifactPath = Join-Path $ArtifactPath $ArtifactName
$artifacts = $ArtifactList.Split(",")
foreach ($artifactName in $artifacts)
{
    Write-Host "Processing $($artifactName)"
    $packages = @{}
    if ($FindArtifactForApiReviewFn -and (Test-Path "Function:$FindArtifactForApiReviewFn"))
    {
        $packages = &$FindArtifactForApiReviewFn $ArtifactPath $artifactName
    }
    else
    {
        Write-Host "The function for 'FindArtifactForApiReviewFn' was not found.`
        Make sure it is present in eng/scripts/Language-Settings.ps1 and referenced in eng/common/scripts/common.ps1.`
        See https://github.com/Azure/azure-sdk-tools/blob/main/doc/common/common_engsys.md#code-structure"
        exit(1)
    }


    if ($packages)
    {
        $pkgPath = $packages.Values[0]
        if (Shoud-Process-Package -pkgPath $pkgPath -packageName $artifactName)
        {
            Write-Host "Submitting API Review for package $($pkg)"
            $filePath = $pkgPath.Replace($ArtifactPath , "").Replace("\", "/")
            Submit-Request -filePath $filePath
        }
    }
    else
    {
        Write-Host "No package is found in artifact path to submit review request"
    }
}
