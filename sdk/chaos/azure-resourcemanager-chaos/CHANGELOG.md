# Release History

## 1.4.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.3.0 (2025-05-15)

- Azure Resource Manager Chaos client library for Java. This package contains Microsoft Azure SDK for Chaos Management SDK. Chaos Management Client. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.TargetListResult` was removed

#### `models.OperationListResult` was removed

#### `models.CapabilityTypeListResult` was removed

#### `models.ExperimentListResult` was removed

#### `models.ExperimentExecutionListResult` was removed

#### `models.ResourceIdentityType` was removed

#### `models.TargetTypeListResult` was removed

#### `models.CapabilityListResult` was removed

#### `models.TargetType` was modified

* `location()` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.ResourceIdentity` was modified

* `withType(models.ResourceIdentityType)` was removed
* `models.ResourceIdentityType type()` -> `models.ManagedServiceIdentityType type()`

#### `models.DelayAction` was modified

* `java.lang.String type()` -> `models.ExperimentActionType type()`

#### `models.ContinuousAction` was modified

* `java.lang.String type()` -> `models.ExperimentActionType type()`

#### `models.CapabilityType` was modified

* `location()` was removed

#### `models.Operations` was modified

* `listAll(com.azure.core.util.Context)` was removed
* `listAll()` was removed

#### `models.ChaosExperimentAction` was modified

* `java.lang.String type()` -> `models.ExperimentActionType type()`

#### `models.DiscreteAction` was modified

* `java.lang.String type()` -> `models.ExperimentActionType type()`

#### `models.ChaosTargetSelector` was modified

* `additionalProperties()` was removed
* `withAdditionalProperties(java.util.Map)` was removed

### Features Added

* `implementation.models.TargetTypeListResult` was added

* `models.ExperimentActionType` was added

* `implementation.models.TargetListResult` was added

* `models.ManagedServiceIdentityType` was added

* `implementation.models.ExperimentListResult` was added

* `implementation.models.OperationListResult` was added

* `implementation.models.CapabilityTypeListResult` was added

* `implementation.models.CapabilityListResult` was added

* `implementation.models.ExperimentExecutionListResult` was added

#### `models.ResourceIdentity` was modified

* `withType(models.ManagedServiceIdentityType)` was added

#### `models.CapabilityType` was modified

* `requiredAzureRoleDefinitionIds()` was added

#### `models.ExperimentExecution` was modified

* `systemData()` was added

#### `models.OperationStatus` was modified

* `resourceId()` was added
* `operations()` was added
* `percentComplete()` was added

#### `models.Operations` was modified

* `list(com.azure.core.util.Context)` was added
* `list()` was added

## 1.2.0 (2024-12-19)

- Azure Resource Manager Chaos client library for Java. This package contains Microsoft Azure SDK for Chaos Management SDK. Chaos Management Client. Package tag package-2024-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.OperationStatus` was modified

* `java.lang.String endTime()` -> `java.time.OffsetDateTime endTime()`
* `java.lang.String startTime()` -> `java.time.OffsetDateTime startTime()`

### Features Added

#### `models.ChaosTargetListSelector` was modified

* `type()` was added

#### `models.DelayAction` was modified

* `type()` was added

#### `models.ChaosTargetQuerySelector` was modified

* `type()` was added

#### `models.ContinuousAction` was modified

* `type()` was added

#### `models.ChaosTargetFilter` was modified

* `type()` was added

#### `models.ChaosTargetSimpleFilter` was modified

* `type()` was added

#### `models.ChaosExperimentAction` was modified

* `type()` was added

#### `models.DiscreteAction` was modified

* `type()` was added

#### `models.ChaosTargetSelector` was modified

* `type()` was added

## 1.1.0 (2024-03-15)

- Azure Resource Manager Chaos client library for Java. This package contains Microsoft Azure SDK for Chaos Management SDK. Chaos Management Client. Package tag package-2024-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ExperimentUpdate` was modified

* `tags()` was added
* `withTags(java.util.Map)` was added

#### `models.Experiment$Update` was modified

* `withTags(java.util.Map)` was added

## 1.0.0 (2023-11-16)

- Azure Resource Manager Chaos client library for Java. This package contains Microsoft Azure SDK for Chaos Management SDK. Chaos Management Client. Package tag package-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ExperimentStatusListResult` was removed

* `models.Selector` was removed

* `models.SimpleFilter` was removed

* `models.Action` was removed

* `models.ExperimentCancelOperationResult` was removed

* `models.ExperimentExecutionDetailsListResult` was removed

* `models.Filter` was removed

* `models.Branch` was removed

* `models.QuerySelector` was removed

* `models.SimpleFilterParameters` was removed

* `models.Step` was removed

* `models.ExperimentStatus` was removed

* `models.ListSelector` was removed

* `models.ExperimentStartOperationResult` was removed

#### `models.ExperimentExecutionDetails` was modified

* `stopDateTime()` was removed
* `experimentId()` was removed
* `lastActionDateTime()` was removed
* `createdDateTime()` was removed
* `startDateTime()` was removed

#### `models.DelayAction` was modified

* `models.Action withName(java.lang.String)` -> `models.ChaosExperimentAction withName(java.lang.String)`

#### `models.ContinuousAction` was modified

* `models.Action withName(java.lang.String)` -> `models.ChaosExperimentAction withName(java.lang.String)`

#### `models.Experiments` was modified

* `getExecutionDetailsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listAllStatuses(java.lang.String,java.lang.String)` was removed
* `models.ExperimentCancelOperationResult cancel(java.lang.String,java.lang.String)` -> `void cancel(java.lang.String,java.lang.String)`
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listExecutionDetails(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `cancelWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getExecutionDetails(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listAllStatuses(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listExecutionDetails(java.lang.String,java.lang.String)` was removed
* `models.ExperimentStartOperationResult start(java.lang.String,java.lang.String)` -> `void start(java.lang.String,java.lang.String)`
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `getStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `startWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getStatus(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.Experiment$Definition` was modified

* `withStartOnCreation(java.lang.Boolean)` was removed

#### `models.Experiment` was modified

* `startWithResponse(com.azure.core.util.Context)` was removed
* `models.ExperimentStartOperationResult start()` -> `void start()`
* `models.ExperimentCancelOperationResult cancel()` -> `void cancel()`
* `cancelWithResponse(com.azure.core.util.Context)` was removed
* `startOnCreation()` was removed

#### `models.DiscreteAction` was modified

* `models.Action withName(java.lang.String)` -> `models.ChaosExperimentAction withName(java.lang.String)`

### Features Added

* `models.ChaosTargetListSelector` was added

* `models.ChaosTargetSimpleFilterParameters` was added

* `models.ChaosTargetQuerySelector` was added

* `models.ExperimentExecutionListResult` was added

* `models.ExperimentExecution` was added

* `models.ChaosExperimentBranch` was added

* `models.ChaosTargetFilter` was added

* `models.ChaosTargetSimpleFilter` was added

* `models.OperationStatus` was added

* `models.ChaosExperimentStep` was added

* `models.ProvisioningState` was added

* `models.ChaosExperimentAction` was added

* `models.ChaosTargetSelector` was added

* `models.OperationStatuses` was added

#### `models.ExperimentExecutionDetails` was modified

* `stoppedAt()` was added
* `startedAt()` was added
* `lastActionAt()` was added

#### `models.Experiments` was modified

* `executionDetails(java.lang.String,java.lang.String,java.lang.String)` was added
* `executionDetailsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getExecution(java.lang.String,java.lang.String,java.lang.String)` was added
* `listAllExecutions(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `cancel(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getExecutionWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listAllExecutions(java.lang.String,java.lang.String)` was added

#### `models.Experiment` was modified

* `provisioningState()` was added
* `start(com.azure.core.util.Context)` was added
* `cancel(com.azure.core.util.Context)` was added

#### `ChaosManager` was modified

* `operationStatuses()` was added

## 1.0.0-beta.1 (2023-07-27)

- Azure Resource Manager Chaos client library for Java. This package contains Microsoft Azure SDK for Chaos Management SDK. Chaos Management Client. Package tag package-2023-04-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
