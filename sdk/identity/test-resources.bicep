@description('The client OID to grant access to test resources.')
param testApplicationOid string

@minLength(6)
@maxLength(50)
@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resource. By default, this is the same as the resource group.')
param location string = resourceGroup().location

param sshPubKey string

param adminUserName string = 'azureuser'

@minLength(5)
@maxLength(50)
@description('Provide a globally unique name of the Azure Container Registry')
param acrName string = 'acr${uniqueString(resourceGroup().id)}'

param latestAksVersion string

//See https://docs.microsoft.com/en-us/azure/role-based-access-control/built-in-roles
var blobContributor = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'ba92f5b4-2d11-453d-a403-e96b0029c9fe') //Storage Blob Data Contributor
var websiteContributor = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'de139f84-1756-47ae-9be6-808fbbe84772') //Website Contributor
// cluster parameters
var kubernetesVersion = latestAksVersion

resource usermgdid 'Microsoft.ManagedIdentity/userAssignedIdentities@2018-11-30' = {
  name: baseName
  location: location
}

resource blobRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  scope: sa
  name: guid(resourceGroup().id, blobContributor)
  properties: {
    principalId: web.identity.principalId
    roleDefinitionId: blobContributor
    principalType: 'ServicePrincipal'
  }
}

resource blobRoleFunc 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  scope: sa
  name: guid(resourceGroup().id, blobContributor, 'azfunc')
  properties: {
    principalId: azfunc.identity.principalId
    roleDefinitionId: blobContributor
    principalType: 'ServicePrincipal'
  }
}

resource blobRole2 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  scope: sa2
  name: guid(resourceGroup().id, blobContributor, usermgdid.id)
  properties: {
    principalId: usermgdid.properties.principalId
    roleDefinitionId: blobContributor
    principalType: 'ServicePrincipal'
  }
}

resource webRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  scope: web
  name: guid(resourceGroup().id, websiteContributor)
  properties: {
    principalId: testApplicationOid
    roleDefinitionId: websiteContributor
    principalType: 'ServicePrincipal'
  }
}

resource sa 'Microsoft.Storage/storageAccounts@2021-08-01' = {
  name: baseName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
  }
}

resource sa2 'Microsoft.Storage/storageAccounts@2021-08-01' = {
  name: '${baseName}2'
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
  }
}

resource farm 'Microsoft.Web/serverfarms@2021-03-01' = {
  name: '${baseName}_farm'
  location: location
  sku: {
    name: 'B1'
    tier: 'Basic'
    size: 'B1'
    family: 'B'
    capacity: 1
  }
  kind: 'linux'
  properties: {
    reserved: true
  }
}

resource web 'Microsoft.Web/sites@2021-03-01' = {
  name: '${baseName}-webapp'
  location: location
  kind: 'app'
  identity: {
    type: 'SystemAssigned, UserAssigned'
    userAssignedIdentities: {
      '${usermgdid.id}' : { }
    }
  }
  properties: {
    enabled: true
    serverFarmId: farm.id
    httpsOnly: true
    keyVaultReferenceIdentity: 'SystemAssigned'
    siteConfig: {
      linuxFxVersion: 'JAVA|17-java17'
      http20Enabled: true
      minTlsVersion: '1.2'
      appSettings: [
        {
          name: 'AZURE_REGIONAL_AUTHORITY_NAME'
          value: 'eastus'
        }
        {
          name: 'IDENTITY_STORAGE_NAME_1'
          value: sa.name
        }
        {
          name: 'IDENTITY_STORAGE_NAME_2'
          value: sa2.name
        }
        {
          name: 'IDENTITY_USER_DEFINED_IDENTITY'
          value: usermgdid.id
        }
        {
          name: 'SCM_DO_BUILD_DURING_DEPLOYMENT'
          value: 'true'
        }
      ]
    }
  }
}

resource azfunc 'Microsoft.Web/sites@2021-03-01' = {
  name: '${baseName}func'
  location: location
  kind: 'functionapp'
  identity: {
    type: 'SystemAssigned, UserAssigned'
    userAssignedIdentities: {
      '${usermgdid.id}' : { }
    }
  }
  properties: {
    enabled: true
    serverFarmId: farm.id
    httpsOnly: true
    keyVaultReferenceIdentity: 'SystemAssigned'
    siteConfig: {
      alwaysOn: true
      minTlsVersion: '1.2'
      appSettings: [
        {
          name: 'IDENTITY_STORAGE_NAME_1'
          value: sa.name
        }
        {
          name: 'IDENTITY_STORAGE_NAME_2'
          value: sa2.name
        }
        {
          name: 'IDENTITY_USER_DEFINED_IDENTITY'
          value: usermgdid.id
        }
        {
          name: 'AzureWebJobsStorage'
          value: 'DefaultEndpointsProtocol=https;AccountName=${sa.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${sa.listKeys().keys[0].value}'
        }
        {
          name: 'WEBSITE_CONTENTAZUREFILECONNECTIONSTRING'
          value: 'DefaultEndpointsProtocol=https;AccountName=${sa.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${sa.listKeys().keys[0].value}'
        }
        {
          name: 'WEBSITE_CONTENTSHARE'
          value: toLower('${baseName}-func')
        }
        {
          name: 'FUNCTIONS_EXTENSION_VERSION'
          value: '~4'
        }
        {
          name: 'FUNCTIONS_WORKER_RUNTIME'
          value: 'java'
        }
      ]
    }
  }
}

resource acrResource 'Microsoft.ContainerRegistry/registries@2023-01-01-preview' = {
  name: acrName
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    adminUserEnabled: true
  }
}

resource newCluster 'Microsoft.ContainerService/managedClusters@2023-06-01' = {
  name: baseName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    kubernetesVersion: kubernetesVersion
    enableRBAC: true
    dnsPrefix: 'identitytest'
    agentPoolProfiles: [
      {
        name: 'agentpool'
        count: 1
        vmSize: 'Standard_D2s_v3'
        osDiskSizeGB: 128
        osDiskType: 'Managed'
        kubeletDiskType: 'OS'
        type: 'VirtualMachineScaleSets'
        enableAutoScaling: false
        orchestratorVersion: kubernetesVersion
        mode: 'System'
        osType: 'Linux'
        osSKU: 'Ubuntu'
      }
    ]
    linuxProfile: {
      adminUsername: adminUserName
      ssh: {
        publicKeys: [
          {
            keyData: sshPubKey
          }
        ]
      }
    }
    oidcIssuerProfile: {
      enabled: true
    }
    securityProfile: {
      workloadIdentity: {
        enabled: true
      }
    }
  }
}


resource vnet 'Microsoft.Network/virtualNetworks@2021-02-01' = {
  name: '${baseName}vnet'
  location: location
  properties: {
    addressSpace: {
      addressPrefixes: [
        '10.0.0.0/16'
      ]
    }
    subnets: [
      {
        name: '${baseName}subnet'
        properties: {
          addressPrefix: '10.0.0.0/24'
        }
      }
    ]
  }
}

resource networkInterface 'Microsoft.Network/networkInterfaces@2021-02-01' = {
  name: '${baseName}NIC'
  location: location
  properties: {
    ipConfigurations: [
      {
        name: 'myIPConfig'
        properties: {
          privateIPAllocationMethod: 'Dynamic'
          subnet: {
            id: vnet.properties.subnets[0].id
          }
        }
      }
    ]
  }
}

resource virtualMachine 'Microsoft.Compute/virtualMachines@2020-06-01' = {
  name: '${baseName}vm'
  location: location
  identity: {
    type: 'SystemAssigned, UserAssigned'
    userAssignedIdentities: {
      '${usermgdid.id}' : { }
    }
  }
  properties: {
    hardwareProfile: {
      vmSize: 'Standard_DS1_v2'
    }
    osProfile: {
      computerName: '${baseName}vm'
      adminUsername: adminUserName
      linuxConfiguration: {
        disablePasswordAuthentication: true
        ssh: {
          publicKeys: [
            {
              path: '/home/${adminUserName}/.ssh/authorized_keys'
              keyData: sshPubKey
            }
          ]
        }
      }
    }
    storageProfile: {
      imageReference: {
        publisher: 'Canonical'
        offer: '0001-com-ubuntu-server-jammy'
        sku: '22_04-lts-gen2'
        version: 'latest'
      }
      osDisk: {
          createOption: 'FromImage'
      }
    }
    networkProfile: {
      networkInterfaces: [{
          id: networkInterface.id
      }]
    }
  }
}

output IDENTITY_WEBAPP_NAME string = web.name
output IDENTITY_USER_DEFINED_IDENTITY string = usermgdid.id
output IDENTITY_USER_DEFINED_IDENTITY_CLIENT_ID string = usermgdid.properties.clientId
output IDENTITY_USER_DEFINED_IDENTITY_NAME string = usermgdid.name
output IDENTITY_AKS_CLUSTER_NAME string = newCluster.name
output IDENTITY_AKS_POD_NAME string = 'java-test-app'
output IDENTITY_STORAGE_NAME_1 string = sa.name
output IDENTITY_STORAGE_NAME_2 string = sa2.name
output IDENTITY_FUNCTION_NAME string = azfunc.name
output IDENTITY_APPSERVICE_NAME string = farm.name
output IDENTITY_FUNC_URL string = azfunc.properties.defaultHostName
output IDENTITY_ACR_NAME string = acrResource.name
output IDENTITY_ACR_LOGIN_SERVER string = acrResource.properties.loginServer
output IDENTITY_VM_NAME string = virtualMachine.name
