<#
.SYNOPSIS
Sets the dev version for the SDKs and updates the repository with the new version.

.DESCRIPTION
Sets the dev version for the SDKs and updates the repository with the new version.

The dev version is based on the date and the build number. The dev version is then published to the Azure Artifacts feed.

.PARAMETER BuildNumber
The build number to use for the dev version.

.PARAMETER SdkType
The type of SDK to set the dev version for.
#>

param(
  [Parameter(Mandatory = $true)]
  [string]$BuildNumber,

  [Parameter(Mandatory = $true)]
  [string]$SdkType
)

$artifacts = $env:ARTIFACTSJSON | ConvertFrom-Json
python3 --version

# Append dev package version suffix for each artifact
foreach ($artifact in $artifacts) {
    python3 "$PSScriptRoot/../../versioning/set_versions.py" --build-type $SDKType --build-qualifier "alpha.$BuildNumber" --artifact-id $artifact.name --group-id $artifact.groupId
}

# Set zero-dev-version for packages
python3 "$PSScriptRoot/../../versioning/set_versions.py" --set-dev-zero-version --build-type $SDKType --build-qualifier "alpha.$BuildNumber"

# Apply version settings to repository
python3 "$PSScriptRoot/../../versioning/update_versions.py" --update-type library --build-type $SDKType --sr
