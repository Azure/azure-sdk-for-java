# Release History

## 1.0.0-beta.2 (2024-09-18)

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

* `infrastructureVersion()` was added
* `withPreviewFeatures(java.util.List)` was added
* `previewFeatures()` was added
* `replicaParameters()` was added
* `replica()` was added
* `withReplicaParameters(models.MongoClusterReplicaParameters)` was added

#### `models.MongoCluster` was modified

* `promote(models.PromoteReplicaRequest,com.azure.core.util.Context)` was added
* `promote(models.PromoteReplicaRequest)` was added

#### `models.MongoClusterUpdateProperties` was modified

* `previewFeatures()` was added
* `withPreviewFeatures(java.util.List)` was added

## 1.0.0-beta.1 (2024-07-01)

- Azure Resource Manager Mongo Cluster client library for Java. This package contains Microsoft Azure SDK for Mongo Cluster Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure Cosmos DB for MongoDB vCore resources including clusters and firewall rules. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-mongocluster Java SDK. 
