# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-17)

- Azure Resource Manager DevTestLabs client library for Java. This package contains Microsoft Azure SDK for DevTestLabs Management SDK. The DevTest Labs Client. Package tag package-2018-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ScheduleCreationParameter` was modified

* `withLocation(java.lang.String)` was removed

#### `models.GlobalSchedules` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ServiceFabricProperties` was added

* `models.LabVirtualMachineProperties` was added

* `models.ApplicableScheduleProperties` was added

#### `models.NotificationChannel` was modified

* `resourceGroupName()` was added

#### `models.User` was modified

* `resourceGroupName()` was added

#### `models.Secret` was modified

* `resourceGroupName()` was added

#### `models.ServiceRunner` was modified

* `resourceGroupName()` was added

#### `models.CustomImage` was modified

* `resourceGroupName()` was added

#### `models.Formula` was modified

* `resourceGroupName()` was added

#### `DevTestLabsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.LabCost` was modified

* `resourceGroupName()` was added

#### `models.Disk` was modified

* `resourceGroupName()` was added

#### `models.ServiceFabric` was modified

* `resourceGroupName()` was added

#### `models.ArtifactSource` was modified

* `resourceGroupName()` was added

#### `models.DtlEnvironment` was modified

* `resourceGroupName()` was added

#### `models.Schedule` was modified

* `resourceGroupName()` was added

#### `models.Policy` was modified

* `resourceGroupName()` was added

#### `models.Lab` was modified

* `resourceGroupName()` was added

#### `models.GlobalSchedules` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.LabVirtualMachine` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetwork` was modified

* `resourceGroupName()` was added

#### `DevTestLabsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-15)

- Azure Resource Manager DevTestLabs client library for Java. This package contains Microsoft Azure SDK for DevTestLabs Management SDK. The DevTest Labs Client. Package tag package-2018-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
