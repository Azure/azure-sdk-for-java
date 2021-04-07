param (
    [hashtable] $DeploymentOutputs,
    [string] $TenantId,
    [string] $TestApplicationId,
    [string] $TestApplicationSecret
)

$version = (Get-Module Az).Version
# Detect if we are running an azure powershell version without the Import-AzContainerRegistryImage command
$minimumVersion = New-Object -TypeName Version -ArgumentList "5.6.0"
if ($version -lt $minimumVersion) {
    Update-Module Az -RequiredVersion "5.7.0" -Force
}

Import-Module Az -Force

Import-AzContainerRegistryImage `
    -ResourceGroupName $DeploymentOutputs['CONTAINERREGISTRY_RESOURCE_GROUP'] `
    -RegistryName $DeploymentOutputs['CONTAINERREGISTRY_REGISTRY_NAME'] `
    -SourceImage 'library/hello-world' -SourceRegistryUri 'registry.hub.docker.com' `
    -TargetTag @('library/hello-world:latest', 'library/hello-world:v1', 'library/hello-world:v2', 'library/hello-world:v3', 'library/hello-world:v4') `
    -Mode 'Force'

Import-AzContainerRegistryImage `
    -ResourceGroupName $DeploymentOutputs['CONTAINERREGISTRY_RESOURCE_GROUP'] `
    -RegistryName $DeploymentOutputs['CONTAINERREGISTRY_REGISTRY_NAME'] `
    -SourceImage 'library/alpine' -SourceRegistryUri 'registry.hub.docker.com' `
    -Mode 'Force'
