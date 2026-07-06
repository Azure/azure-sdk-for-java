# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2026-06-23)

- Azure Resource Manager RedHatOpenShift client library for Java. This package contains Microsoft Azure SDK for RedHatOpenShift Management SDK. Rest API for Azure Red Hat OpenShift 4. Package api-version 2025-07-25. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SyncIdentityProviders` was removed

#### `models.SyncSet$Update` was removed

#### `models.MachinePoolList` was removed

#### `models.MachinePools` was removed

#### `models.SyncIdentityProvider$DefinitionStages` was removed

#### `models.Secret$UpdateStages` was removed

#### `models.SyncSet$UpdateStages` was removed

#### `models.SyncIdentityProviderUpdate` was removed

#### `models.MachinePool$Update` was removed

#### `models.SyncSet` was removed

#### `models.SyncSetList` was removed

#### `models.MachinePool$UpdateStages` was removed

#### `models.Secret` was removed

#### `models.OperationList` was removed

#### `models.SecretList` was removed

#### `models.SyncIdentityProviderList` was removed

#### `models.Secrets` was removed

#### `models.OpenShiftClusterList` was removed

#### `models.SyncIdentityProvider$UpdateStages` was removed

#### `models.SyncSets` was removed

#### `models.MachinePool$Definition` was removed

#### `models.Secret$Update` was removed

#### `models.Secret$DefinitionStages` was removed

#### `models.SyncIdentityProvider$Update` was removed

#### `models.SyncSetUpdate` was removed

#### `models.OpenShiftVersionList` was removed

#### `models.SyncSet$DefinitionStages` was removed

#### `models.MachinePool` was removed

#### `models.MachinePoolUpdate` was removed

#### `models.Secret$Definition` was removed

#### `models.SecretUpdate` was removed

#### `models.SyncSet$Definition` was removed

#### `models.MachinePool$DefinitionStages` was removed

#### `models.SyncIdentityProvider$Definition` was removed

#### `models.SyncIdentityProvider` was removed

#### `models.WorkerProfile` was modified

* `validate()` was removed

#### `models.MasterProfile` was modified

* `validate()` was removed

#### `models.NetworkProfile` was modified

* `validate()` was removed

#### `models.ServicePrincipalProfile` was modified

* `validate()` was removed

#### `models.ManagedOutboundIPs` was modified

* `validate()` was removed

#### `models.EffectiveOutboundIp` was modified

* `EffectiveOutboundIp()` was changed to private access
* `withId(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiServerProfile` was modified

* `validate()` was removed

#### `models.IngressProfile` was modified

* `validate()` was removed

#### `RedHatOpenShiftManager` was modified

* `machinePools()` was removed
* `fluent.AzureRedHatOpenShiftClient serviceClient()` -> `fluent.RedHatOpenShiftManagementClient serviceClient()`
* `syncSets()` was removed
* `syncIdentityProviders()` was removed
* `secrets()` was removed

#### `models.ClusterProfile` was modified

* `validate()` was removed

#### `models.OpenShiftClusterUpdate` was modified

* `systemData()` was removed
* `validate()` was removed

#### `models.LoadBalancerProfile` was modified

* `validate()` was removed

#### `models.Display` was modified

* `Display()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `validate()` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.ConsoleProfile` was modified

* `validate()` was removed

### Features Added

* `models.PlatformWorkloadIdentityRoleSetOperations` was added

* `models.PlatformWorkloadIdentityProfile` was added

* `models.PlatformWorkloadIdentityRoleSets` was added

* `models.UserAssignedIdentity` was added

* `models.PlatformWorkloadIdentityRole` was added

* `models.ManagedServiceIdentityType` was added

* `models.PlatformWorkloadIdentity` was added

* `models.ManagedServiceIdentity` was added

* `models.PlatformWorkloadIdentityRoleSet` was added

#### `models.OpenShiftCluster$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withPlatformWorkloadIdentityProfile(models.PlatformWorkloadIdentityProfile)` was added

#### `models.OpenShiftCluster$Update` was modified

* `withPlatformWorkloadIdentityProfile(models.PlatformWorkloadIdentityProfile)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.OpenShiftVersions` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added

#### `models.OpenShiftCluster` was modified

* `platformWorkloadIdentityProfile()` was added
* `identity()` was added

#### `RedHatOpenShiftManager` was modified

* `platformWorkloadIdentityRoleSets()` was added
* `platformWorkloadIdentityRoleSetOperations()` was added

#### `models.ClusterProfile` was modified

* `oidcIssuer()` was added

#### `models.OpenShiftClusterUpdate` was modified

* `withPlatformWorkloadIdentityProfile(models.PlatformWorkloadIdentityProfile)` was added
* `identity()` was added
* `platformWorkloadIdentityProfile()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

## 1.0.0-beta.1 (2024-08-21)

- Azure Resource Manager Red Hat Open Shift client library for Java. This package contains Microsoft Azure SDK for Red Hat Open Shift Management SDK. Rest API for Azure Red Hat OpenShift 4. Package tag package-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-redhatopenshift Java SDK.
