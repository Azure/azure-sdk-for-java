# Release History

## 1.0.0-beta.2 (2021-05-27)

- Azure Resource Manager DelegatedNetwork client library for Java. This package contains Microsoft Azure SDK for DelegatedNetwork Management SDK. DNC web api provides way to create, get and delete dnc controller. Package tag package-2021-03-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ErrorAdditionalInfo` was removed

#### `models.DelegatedSubnet` was modified

* `provisioningState()` was removed
* `resourceGuid()` was removed
* `controllerDetails()` was removed
* `subnetDetails()` was removed

#### `models.Orchestrator$Definition` was modified

* `withPrivateLinkResourceId(java.lang.String)` was removed
* `withClusterRootCA(java.lang.String)` was removed
* `withControllerDetails(models.ControllerDetails)` was removed
* `withOrchestratorAppId(java.lang.String)` was removed
* `withApiServerEndpoint(java.lang.String)` was removed
* `withOrchestratorTenantId(java.lang.String)` was removed

#### `models.Orchestrator` was modified

* `controllerDetails()` was removed
* `provisioningState()` was removed
* `resourceGuid()` was removed
* `privateLinkResourceId()` was removed
* `orchestratorTenantId()` was removed
* `apiServerEndpoint()` was removed
* `orchestratorAppId()` was removed
* `clusterRootCA()` was removed

#### `models.DelegatedController` was modified

* `dncAppId()` was removed
* `dncTenantId()` was removed
* `provisioningState()` was removed
* `resourceGuid()` was removed
* `dncEndpoint()` was removed

#### `models.DelegatedSubnet$Definition` was modified

* `withControllerDetails(models.ControllerDetails)` was removed
* `withSubnetDetails(models.SubnetDetails)` was removed

### New Feature

* `models.DelegatedControllerProperties` was added

* `models.OrchestratorResourceProperties` was added

* `models.DelegatedSubnetProperties` was added

#### `models.DelegatedSubnet` was modified

* `properties()` was added

#### `models.Orchestrator$Definition` was modified

* `withProperties(models.OrchestratorResourceProperties)` was added

#### `models.Orchestrator` was modified

* `properties()` was added

#### `models.DelegatedController` was modified

* `properties()` was added

#### `models.DelegatedSubnet$Definition` was modified

* `withProperties(models.DelegatedSubnetProperties)` was added

## 1.0.0-beta.1 (2021-03-26)

- Azure Resource Manager DelegatedNetwork client library for Java. This package contains Microsoft Azure SDK for DelegatedNetwork Management SDK. DNC web api provides way to create, get and delete dnc controller. Package tag package-2021-03-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
