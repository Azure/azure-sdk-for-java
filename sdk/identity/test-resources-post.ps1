param (
    [hashtable] $DeploymentOutputs
)

$webappRoot = "$PSScriptRoot/identity-mi-server/" | Resolve-Path
$webappRootPom = "webappRoot/pom.xml" | Resolve-Path
$workingFolder = $webappRoot;
if ($null -ne $Env:AGENT_WORKFOLDER) {
    $workingFolder = $Env:AGENT_WORKFOLDER
}
az login --service-principal -u $DeploymentOutputs['IDENTITY_CLIENT_ID'] -p $DeploymentOutputs['IDENTITY_CLIENT_SECRET'] --tenant $DeploymentOutputs['IDENTITY_TENANT_ID']
az account set --subscription $DeploymentOutputs['IDENTITY_SUBSCRIPTION_ID']
mvn clean package $webappRootPom
az webapp deploy --resource-group $DeploymentOutputs['IDENTITY_RESOURCE_GROUP'] --name $DeploymentOutputs['IDENTITY_WEBAPP_NAME'] --src-path "$workingFolder/target/dentity-mi-server-0.0.1-SNAPSHOT.jar"
Remove-Item -Force -Recurse "$workingFolder/target"
if ($null -eq $Env:AGENT_WORKFOLDER) {
    Remove-Item -Force -Recurse "$webappRoot/%AGENT_WORKFOLDER%"
}
az logout
