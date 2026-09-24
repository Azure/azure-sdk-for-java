# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-08-20)

- Azure Resource Manager HorizonDb client library for Java. This package contains Microsoft Azure SDK for HorizonDb Management SDK. Azure Resource Provider API for managing HorizonDB clusters, pools, replicas, and firewall rules. Package api-version 2026-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PrivateEndpointConnectionUpdate` was removed

#### `models.PrivateEndpointConnection` was removed

#### `models.OptionalPropertiesUpdateableProperties` was removed

#### `models.HorizonDbPrivateEndpointConnections` was modified

* `update(java.lang.String,java.lang.String,models.PrivateEndpointConnectionUpdate,com.azure.core.util.Context)` was removed
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `update(java.lang.String,java.lang.String,models.PrivateEndpointConnectionUpdate)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.HorizonDbAdministrators` was added

* `models.HorizonDbAdministrator$UpdateStages` was added

* `models.ManagedServiceIdentity` was added

* `models.HorizonDbAdministratorAdd` was added

* `models.PrincipalTypes` was added

* `models.ManagedServiceIdentityType` was added

* `models.HorizonDbComputeModel` was added

* `models.HorizonDbAdministrator$Update` was added

* `models.HorizonDbAdministratorPropertiesForAdd` was added

* `models.AuthenticationState` was added

* `models.HorizonDbComputeModelType` was added

* `models.UserAssignedIdentity` was added

* `models.HorizonDbAdministratorProperties` was added

* `models.HorizonDbAdministrator$Definition` was added

* `models.HorizonDbClusterMirroring` was added

* `models.HorizonDbAdministrator` was added

* `models.HorizonDbClusterAuthConfig` was added

* `models.HorizonDbAdministrator$DefinitionStages` was added

#### `models.HorizonDbCluster$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.HorizonDbClusters` was modified

* `start(java.lang.String,java.lang.String)` was added
* `restart(java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String)` was added
* `restart(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.HorizonDbPrivateEndpointConnections` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionResourceInner,com.azure.core.util.Context)` was added
* `updateStatus(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionResourceInner)` was added

#### `models.HorizonDbClusterPropertiesForPatchUpdate` was modified

* `withMirroring(models.HorizonDbClusterMirroring)` was added
* `withAuthConfig(models.HorizonDbClusterAuthConfig)` was added
* `withComputeModel(models.HorizonDbComputeModel)` was added
* `mirroring()` was added
* `authConfig()` was added
* `computeModel()` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `withPrivateLinkServiceConnectionState(models.PrivateLinkServiceConnectionState)` was added
* `withPrivateEndpoint(models.PrivateEndpoint)` was added

#### `models.HorizonDbCluster` was modified

* `stop()` was added
* `start()` was added
* `identity()` was added
* `stop(com.azure.core.util.Context)` was added
* `start(com.azure.core.util.Context)` was added
* `restart()` was added
* `restart(com.azure.core.util.Context)` was added

#### `models.HorizonDbClusterProperties` was modified

* `mirroring()` was added
* `computeModel()` was added
* `authConfig()` was added
* `withMirroring(models.HorizonDbClusterMirroring)` was added
* `withComputeModel(models.HorizonDbComputeModel)` was added
* `withAuthConfig(models.HorizonDbClusterAuthConfig)` was added

#### `models.HorizonDbClusterForPatchUpdate` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.HorizonDbCluster$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `HorizonDbManager` was modified

* `horizonDbAdministrators()` was added

#### `models.State` was modified

* `SUCCEEDED` was added
* `UPGRADING` was added

## 1.0.0-beta.1 (2026-03-30)

- Azure Resource Manager HorizonDb client library for Java. This package contains Microsoft Azure SDK for HorizonDb Management SDK. Azure Resource Provider API for managing HorizonDb clusters, pools, replicas, and firewall rules. Package api-version 2026-01-20-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-horizondb Java SDK.
