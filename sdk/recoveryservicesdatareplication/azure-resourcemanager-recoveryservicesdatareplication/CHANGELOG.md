# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2025-06-04)

- Azure Resource Manager Recovery Services Data Replication client library for Java. This package contains Microsoft Azure SDK for Recovery Services Data Replication Management SDK. A first party Azure service enabling the data replication. Package api-version 2024-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- First stable release for the azure-resourcemanager-recoveryservicesdatareplication Java SDK.

## 1.0.0-beta.2 (2024-12-03)

- Azure Resource Manager Recovery Services Data Replication client library for Java. This package contains Microsoft Azure SDK for Recovery Services Data Replication Management SDK. A first party Azure service enabling the data replication. Package tag package-2021-02-16-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.DraModelCustomProperties` was modified

* `instanceType()` was added

#### `models.AzStackHciFabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.ProtectedItemModelPropertiesLastTestFailoverJob` was modified

* `scenarioName()` was added
* `state()` was added
* `name()` was added
* `displayName()` was added
* `id()` was added
* `startTime()` was added
* `endTime()` was added

#### `models.VMwareToAzStackHciPolicyModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciEventModelCustomProperties` was modified

* `instanceType()` was added

#### `models.FailoverWorkflowModelCustomProperties` was modified

* `instanceType()` was added
* `affectedObjectDetails()` was added

#### `models.VMwareMigrateFabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.ReplicationExtensionModelCustomProperties` was modified

* `instanceType()` was added

#### `models.FabricModelUpdate` was modified

* `type()` was added
* `id()` was added
* `name()` was added

#### `models.VMwareToAzStackHciProtectedItemCustomProps` was modified

* `instanceType()` was added

#### `models.TestFailoverWorkflowModelCustomProperties` was modified

* `affectedObjectDetails()` was added
* `instanceType()` was added

#### `models.ProtectedItemModelPropertiesCurrentJob` was modified

* `id()` was added
* `state()` was added
* `scenarioName()` was added
* `displayName()` was added
* `name()` was added
* `endTime()` was added
* `startTime()` was added

#### `models.HyperVToAzStackHciRepExtnCustomProps` was modified

* `instanceType()` was added

#### `models.RecoveryPointModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciPlannedFailoverCustomProps` was modified

* `instanceType()` was added

#### `models.PlannedFailoverModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVMigrateFabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciProtectedItemCustomProps` was modified

* `instanceType()` was added

#### `models.VMwareDraModelCustomProperties` was modified

* `instanceType()` was added

#### `models.LastFailedEnableProtectionJob` was modified

* `startTime()` was added
* `endTime()` was added
* `id()` was added
* `displayName()` was added
* `scenarioName()` was added
* `name()` was added
* `state()` was added

#### `models.ProtectedItemModelCustomProperties` was modified

* `instanceType()` was added

#### `models.WorkflowModelCustomProperties` was modified

* `instanceType()` was added

#### `models.HyperVToAzStackHciRecoveryPointCustomProps` was modified

* `instanceType()` was added

#### `models.FabricModelCustomProperties` was modified

* `instanceType()` was added

#### `models.PolicyModelCustomProperties` was modified

* `instanceType()` was added

#### `models.LastFailedPlannedFailoverJob` was modified

* `id()` was added
* `endTime()` was added
* `startTime()` was added
* `scenarioName()` was added
* `displayName()` was added
* `state()` was added
* `name()` was added

#### `models.EventModelCustomProperties` was modified

* `instanceType()` was added

#### `models.VMwareToAzStackHciRepExtnCustomProps` was modified

* `instanceType()` was added

#### `models.VMwareToAzStackHciPlannedFailoverCustomProps` was modified

* `instanceType()` was added

#### `models.VaultModelUpdate` was modified

* `type()` was added
* `id()` was added
* `name()` was added

#### `models.TestFailoverCleanupWorkflowModelCustomProperties` was modified

* `affectedObjectDetails()` was added
* `instanceType()` was added

#### `models.HyperVToAzStackHciPolicyModelCustomProperties` was modified

* `instanceType()` was added

## 1.0.0-beta.1 (2023-10-24)

- Azure Resource Manager Recovery Services Data Replication client library for Java. This package contains Microsoft Azure SDK for Recovery Services Data Replication Management SDK. A first party Azure service enabling the data replication. Package tag package-2021-02-16-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

