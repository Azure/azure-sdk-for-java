# Release History

## 1.0.0-beta.2 (2024-09-06)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NodeGroupSpec` was removed

* `models.NodeKind` was removed

#### `models.MongoClusterProperties` was modified

* `withAdministratorLogin(java.lang.String)` was removed
* `administratorLoginPassword()` was removed
* `administratorLogin()` was removed
* `earliestRestoreTime()` was removed
* `nodeGroupSpecs()` was removed
* `withAdministratorLoginPassword(java.lang.String)` was removed
* `withNodeGroupSpecs(java.util.List)` was removed

#### `models.MongoClusterUpdateProperties` was modified

* `withAdministratorLoginPassword(java.lang.String)` was removed
* `withNodeGroupSpecs(java.util.List)` was removed
* `administratorLogin()` was removed
* `withAdministratorLogin(java.lang.String)` was removed
* `administratorLoginPassword()` was removed
* `nodeGroupSpecs()` was removed

### Features Added

* `models.BackupProperties` was added

* `models.HighAvailabilityMode` was added

* `models.PromoteMode` was added

* `models.StorageProperties` was added

* `models.PromoteOption` was added

* `models.Replicas` was added

* `models.ShardingProperties` was added

* `models.ReplicationProperties` was added

* `implementation.models.ReplicaListResult` was added

* `models.ReplicationRole` was added

* `models.MongoClusterReplicaParameters` was added

* `models.ReplicationState` was added

* `models.Replica` was added

* `models.PromoteReplicaRequest` was added

* `models.AdministratorProperties` was added

* `models.PreviewFeature` was added

* `models.ComputeProperties` was added

* `models.HighAvailabilityProperties` was added

#### `models.MongoClusters` was modified

* `promote(java.lang.String,java.lang.String,models.PromoteReplicaRequest,com.azure.core.util.Context)` was added
* `promote(java.lang.String,java.lang.String,models.PromoteReplicaRequest)` was added

#### `MongoClusterManager` was modified

* `replicas()` was added

#### `models.MongoClusterProperties` was modified

* `withCompute(models.ComputeProperties)` was added
* `replica()` was added
* `withReplicaParameters(models.MongoClusterReplicaParameters)` was added
* `withAdministrator(models.AdministratorProperties)` was added
* `highAvailability()` was added
* `withPreviewFeatures(java.util.List)` was added
* `storage()` was added
* `administrator()` was added
* `sharding()` was added
* `withStorage(models.StorageProperties)` was added
* `withSharding(models.ShardingProperties)` was added
* `infrastructureVersion()` was added
* `previewFeatures()` was added
* `backup()` was added
* `replicaParameters()` was added
* `compute()` was added
* `withHighAvailability(models.HighAvailabilityProperties)` was added
* `withBackup(models.BackupProperties)` was added

#### `models.MongoCluster` was modified

* `promote(models.PromoteReplicaRequest)` was added
* `promote(models.PromoteReplicaRequest,com.azure.core.util.Context)` was added

#### `models.ConnectionString` was modified

* `name()` was added

#### `models.MongoClusterUpdateProperties` was modified

* `compute()` was added
* `storage()` was added
* `withHighAvailability(models.HighAvailabilityProperties)` was added
* `withBackup(models.BackupProperties)` was added
* `withCompute(models.ComputeProperties)` was added
* `withPreviewFeatures(java.util.List)` was added
* `previewFeatures()` was added
* `backup()` was added
* `sharding()` was added
* `highAvailability()` was added
* `administrator()` was added
* `withStorage(models.StorageProperties)` was added
* `withAdministrator(models.AdministratorProperties)` was added
* `withSharding(models.ShardingProperties)` was added

## 1.0.0-beta.1 (2024-07-01)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-mongocluster Java SDK. 
