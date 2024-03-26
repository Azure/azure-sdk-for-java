param (
    [hashtable] $DeploymentOutputs
)
function getVariable {
    param (
        $name
    )
    if ($null -ne $DeploymentOutputs -and $DeploymentOutputs.ContainsKey($name)) {
        return $DeploymentOutputs[$name]
    }
    if (test-path "env:\$name") {
        return (Get-Item "env:\$name").Value
    }
    Write-Error "Could not find value for $name"
    exit 1
}

$livetestappsRoot = "$PSScriptRoot/live-test-apps" | Resolve-Path

$webappRoot = "$PSScriptRoot/live-test-apps/identity-test-webapp" | Resolve-Path

$aksRoot = "$PSScriptRoot/live-test-apps/identity-test-container" | Resolve-Path

$vmRoot = "$PSScriptRoot/live-test-apps/identity-test-vm" | Resolve-Path

$webappRootPom = "$webappRoot/pom.xml" | Resolve-Path


$funcAppRoot = "$PSScriptRoot/live-test-apps/identity-test-function" | Resolve-Path
$funcAppPom = "$funcAppRoot/pom.xml" | Resolve-Path

az login --service-principal -u $(getVariable('IDENTITY_CLIENT_ID')) -p $(getVariable('IDENTITY_CLIENT_SECRET')) --tenant $(getVariable('IDENTITY_TENANT_ID'))
az account set --subscription $(getVariable('IDENTITY_SUBSCRIPTION_ID'))



mvn clean install -DskipTests -f $webappRootPom | Write-Host
az webapp deploy --resource-group $(getVariable('IDENTITY_RESOURCE_GROUP')) --name $(getVariable('IDENTITY_WEBAPP_NAME')) --src-path "$webappRoot/target/identity-test-webapp-0.0.1-SNAPSHOT.jar" --type jar


# build function app
mvn clean package "-DfunctionAppName=$(getVariable('IDENTITY_FUNCTION_NAME'))" "-DresourceGroup=$(getVariable('IDENTITY_RESOURCE_GROUP'))" "-DappServicePlanName=$(getVariable('IDENTITY_APPSERVICE_NAME'))" -f $funcAppPom | Write-Host
compress-archive  "$funcAppRoot\target\azure-functions\$(getVariable('IDENTITY_FUNCTION_NAME'))\*" -DestinationPath "$funcAppRoot/target/funcpackage.zip"
az functionapp deployment source config-zip -g $(getVariable('IDENTITY_RESOURCE_GROUP')) -n $(getVariable('IDENTITY_FUNCTION_NAME')) --src "$funcAppRoot/target/funcpackage.zip"

# build aks app
mvn clean package -f "$vmRoot/pom.xml" | Write-Host

# Virtual machine setup
$vmScript = @"
sudo apt update && sudo apt install openjdk-8-jdk -y --no-install-recommends
"@
az vm run-command invoke -n $DeploymentOutputs['IDENTITY_VM_NAME'] -g $DeploymentOutputs['IDENTITY_RESOURCE_GROUP'] --command-id RunShellScript --scripts "$vmScript" | Write-Host

Write-Host "Getting storage account details"
$key=az storage account keys list --account-name $DeploymentOutputs['IDENTITY_STORAGE_NAME_1'] --resource-group $DeploymentOutputs['IDENTITY_RESOURCE_GROUP'] --query "[0].value" --output tsv

Write-Host "Creating storage container"
az storage container create --name "vmcontainer" --account-name $DeploymentOutputs['IDENTITY_STORAGE_NAME_1'] --account-key $key --public-access blob | Write-Host

Write-Host "Uploading file to storage"
az storage blob upload --container-name "vmcontainer" --file "$vmRoot/target/identity-test-vm-1.0-SNAPSHOT-jar-with-dependencies.jar" --name "testfile.jar" --account-name $DeploymentOutputs['IDENTITY_STORAGE_NAME_1'] --account-key $key | Write-Host


if ($IsMacOS -eq $true) {
  # Not supported on MacOS agents
  az logout
  return
}

# build aks app
mvn clean package -f "$aksRoot/pom.xml" | Write-Host

az acr login -n $DeploymentOutputs['IDENTITY_ACR_NAME']
$loginServer = az acr show -n $DeploymentOutputs['IDENTITY_ACR_NAME'] --query loginServer -o tsv

$image = "$loginServer/identity-aks-test-image"
docker build --no-cache -t $image "$aksRoot"
docker push $image


# Attach the ACR to the AKS cluster
Write-Host "Attaching ACR to AKS cluster"
az aks update -n $DeploymentOutputs['IDENTITY_AKS_CLUSTER_NAME'] -g $DeploymentOutputs['IDENTITY_RESOURCE_GROUP'] --attach-acr $DeploymentOutputs['IDENTITY_ACR_NAME']

$MIClientId = $DeploymentOutputs['IDENTITY_USER_DEFINED_IDENTITY_CLIENT_ID']
$MIName = $DeploymentOutputs['IDENTITY_USER_DEFINED_IDENTITY_NAME']
$SaAccountName = 'workload-identity-sa'
$PodName = $DeploymentOutputs['IDENTITY_AKS_POD_NAME']



# Get the aks cluster credentials
Write-Host "Getting AKS credentials"
az aks get-credentials --resource-group $DeploymentOutputs['IDENTITY_RESOURCE_GROUP'] --name $DeploymentOutputs['IDENTITY_AKS_CLUSTER_NAME']

#Get the aks cluster OIDC issuer
Write-Host "Getting AKS OIDC issuer"
$AKS_OIDC_ISSUER = az aks show -n $DeploymentOutputs['IDENTITY_AKS_CLUSTER_NAME'] -g $DeploymentOutputs['IDENTITY_RESOURCE_GROUP'] --query "oidcIssuerProfile.issuerUrl" -otsv

# Create the federated identity
Write-Host "Creating federated identity"
az identity federated-credential create --name $MIName --identity-name $MIName --resource-group $DeploymentOutputs['IDENTITY_RESOURCE_GROUP'] --issuer $AKS_OIDC_ISSUER --subject system:serviceaccount:default:workload-identity-sa

# Build the kubernetes deployment yaml
$kubeConfig = @"
apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    azure.workload.identity/client-id: $MIClientId
  name: $SaAccountName
  namespace: default
---
apiVersion: v1
kind: Pod
metadata:
  name: $PodName
  namespace: default
  labels:
    azure.workload.identity/use: "true"
spec:
  serviceAccountName: $SaAccountName
  containers:
  - name: $PodName
    image: $image
    env:
    - name: AZURE_TEST_MODE
      value: "LIVE"
    - name: IS_RUNNING_IN_IDENTITY_CLUSTER
      value: "true"
    command: ["tail"]
    args: ["-f", "/dev/null"]
    ports:
    - containerPort: 80
  nodeSelector:
    kubernetes.io/os: linux
"@

Set-Content -Path "$livetestappsRoot/kubeconfig.yaml" -Value $kubeConfig
Write-Host "Created kubeconfig.yaml with contents:"
Write-Host $kubeConfig

# Apply the config
kubectl apply -f "$livetestappsRoot/kubeconfig.yaml" --overwrite=true
Write-Host "Applied kubeconfig.yaml"
az logout


