# Release History

## 1.1.0-beta.2 (2025-10-08)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. Package api-version 2025-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MongoClusterRestoreParameters` was modified

* `validate()` was removed

#### `models.KeyEncryptionKeyIdentity` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `validate()` was removed

#### `models.DatabaseRole` was modified

* `validate()` was removed

#### `models.IdentityProvider` was modified

* `validate()` was removed

#### `models.ShardingProperties` was modified

* `validate()` was removed

#### `models.EntraIdentityProviderProperties` was modified

* `validate()` was removed

#### `models.MongoClusterReplicaParameters` was modified

* `validate()` was removed

#### `models.MongoClusterProperties` was modified

* `validate()` was removed

#### `models.PromoteReplicaRequest` was modified

* `validate()` was removed

#### `models.AdministratorProperties` was modified

* `validate()` was removed

#### `models.UserProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.MongoClusterUpdate` was modified

* `validate()` was removed

#### `models.ComputeProperties` was modified

* `validate()` was removed

#### `models.HighAvailabilityProperties` was modified

* `validate()` was removed

#### `models.BackupProperties` was modified

* `validate()` was removed

#### `models.CustomerManagedKeyEncryptionProperties` was modified

* `validate()` was removed

#### `models.StorageProperties` was modified

* `withIops(java.lang.Long)` was removed
* `withThroughput(java.lang.Long)` was removed
* `iops()` was removed
* `throughput()` was removed
* `validate()` was removed

#### `models.EntraIdentityProvider` was modified

* `validate()` was removed

#### `models.FirewallRuleProperties` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnection` was modified

* `validate()` was removed

#### `models.ReplicationProperties` was modified

* `validate()` was removed

#### `models.EncryptionProperties` was modified

* `validate()` was removed

#### `models.AuthConfigProperties` was modified

* `validate()` was removed

#### `models.DataApiProperties` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed

#### `models.ConnectionString` was modified

* `validate()` was removed

#### `models.PrivateLinkResourceProperties` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnectionProperties` was modified

* `validate()` was removed

#### `models.MongoClusterUpdateProperties` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

### Features Added

#### `models.MongoClusterUpdateProperties` was modified

* `encryption()` was added
* `withEncryption(models.EncryptionProperties)` was added

## 1.1.0-beta.1 (2025-07-23)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. Package api-version 2025-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.KeyEncryptionKeyIdentity` was added

* `models.ManagedServiceIdentity` was added

* `models.DatabaseRole` was added

* `models.IdentityProvider` was added

* `models.User` was added

* `models.ManagedServiceIdentityType` was added

* `models.EntraIdentityProviderProperties` was added

* `models.User$UpdateStages` was added

* `models.StorageType` was added

* `models.UserProperties` was added

* `models.CustomerManagedKeyEncryptionProperties` was added

* `models.EntraPrincipalType` was added

* `models.EntraIdentityProvider` was added

* `models.DataApiMode` was added

* `models.KeyEncryptionKeyIdentityType` was added

* `models.User$DefinitionStages` was added

* `models.UserRole` was added

* `models.User$Definition` was added

* `models.EncryptionProperties` was added

* `models.AuthenticationMode` was added

* `models.Users` was added

* `models.IdentityProviderType` was added

* `models.AuthConfigProperties` was added

* `models.DataApiProperties` was added

* `models.UserAssignedIdentity` was added

* `models.User$Update` was added

#### `models.MongoCluster$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MongoClusterProperties` was modified

* `encryption()` was added
* `withAuthConfig(models.AuthConfigProperties)` was added
* `withEncryption(models.EncryptionProperties)` was added
* `dataApi()` was added
* `withDataApi(models.DataApiProperties)` was added
* `authConfig()` was added

#### `models.MongoClusterUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

#### `models.StorageProperties` was modified

* `withThroughput(java.lang.Long)` was added
* `iops()` was added
* `withIops(java.lang.Long)` was added
* `type()` was added
* `throughput()` was added
* `withType(models.StorageType)` was added

#### `MongoClusterManager` was modified

* `users()` was added

#### `models.MongoCluster` was modified

* `identity()` was added

#### `models.MongoCluster$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MongoClusterUpdateProperties` was modified

* `authConfig()` was added
* `dataApi()` was added
* `withAuthConfig(models.AuthConfigProperties)` was added
* `withDataApi(models.DataApiProperties)` was added

## 1.0.0 (2024-09-25)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NodeGroupSpec` was removed

* `models.NodeKind` was removed

#### `MongoClusterManager` was modified

* `fluent.DocumentDBClient serviceClient()` -> `fluent.MongoClusterManagementClient serviceClient()`

#### `models.MongoClusterProperties` was modified

* `earliestRestoreTime()` was removed
* `withAdministratorLoginPassword(java.lang.String)` was removed
* `withNodeGroupSpecs(java.util.List)` was removed
* `administratorLogin()` was removed
* `nodeGroupSpecs()` was removed
* `withAdministratorLogin(java.lang.String)` was removed
* `administratorLoginPassword()` was removed

#### `models.MongoClusterUpdateProperties` was modified

* `administratorLogin()` was removed
* `administratorLoginPassword()` was removed
* `nodeGroupSpecs()` was removed
* `withNodeGroupSpecs(java.util.List)` was removed
* `withAdministratorLoginPassword(java.lang.String)` was removed
* `withAdministratorLogin(java.lang.String)` was removed

### Features Added

* `models.BackupProperties` was added

* `models.HighAvailabilityMode` was added

* `models.StorageProperties` was added

* `models.ShardingProperties` was added

* `models.AdministratorProperties` was added

* `models.ComputeProperties` was added

* `models.HighAvailabilityProperties` was added

#### `models.MongoClusterProperties` was modified

* `highAvailability()` was added
* `withCompute(models.ComputeProperties)` was added
* `withAdministrator(models.AdministratorProperties)` was added
* `withHighAvailability(models.HighAvailabilityProperties)` was added
* `storage()` was added
* `compute()` was added
* `sharding()` was added
* `administrator()` was added
* `withSharding(models.ShardingProperties)` was added
* `withStorage(models.StorageProperties)` was added
* `withBackup(models.BackupProperties)` was added
* `backup()` was added

#### `models.ConnectionString` was modified

* `name()` was added

#### `models.MongoClusterUpdateProperties` was modified

* `backup()` was added
* `withCompute(models.ComputeProperties)` was added
* `compute()` was added
* `administrator()` was added
* `withHighAvailability(models.HighAvailabilityProperties)` was added
* `highAvailability()` was added
* `withStorage(models.StorageProperties)` was added
* `withBackup(models.BackupProperties)` was added
* `sharding()` was added
* `storage()` was added
* `withAdministrator(models.AdministratorProperties)` was added
* `withSharding(models.ShardingProperties)` was added

## 1.0.0-beta.2 (2024-09-24)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.PromoteMode` was added

* `models.PromoteOption` was added

* `models.Replicas` was added

* `models.ReplicationProperties` was added

* `implementation.models.ReplicaListResult` was added

* `models.ReplicationRole` was added

* `models.MongoClusterReplicaParameters` was added

* `models.ReplicationState` was added

* `models.Replica` was added

* `models.PromoteReplicaRequest` was added

* `models.PreviewFeature` was added

#### `models.MongoClusters` was modified

* `promote(java.lang.String,java.lang.String,models.PromoteReplicaRequest)` was added
* `promote(java.lang.String,java.lang.String,models.PromoteReplicaRequest,com.azure.core.util.Context)` was added

#### `MongoClusterManager` was modified

* `replicas()` was added

#### `models.MongoClusterProperties` was modified

* `withReplicaParameters(models.MongoClusterReplicaParameters)` was added
* `replicaParameters()` was added
* `replica()` was added
* `withPreviewFeatures(java.util.List)` was added
* `infrastructureVersion()` was added
* `previewFeatures()` was added

#### `models.MongoCluster` was modified

* `promote(models.PromoteReplicaRequest)` was added
* `promote(models.PromoteReplicaRequest,com.azure.core.util.Context)` was added

#### `models.MongoClusterUpdateProperties` was modified

* `previewFeatures()` was added
* `withPreviewFeatures(java.util.List)` was added

## 1.0.0-beta.1 (2024-07-01)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-mongocluster Java SDK. 
