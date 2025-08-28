# Release History

## 1.3.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.3.0-beta.1 (2025-08-07)

- Azure Resource Manager Container Service Fleet client library for Java. This package contains Microsoft Azure SDK for Container Service Fleet Management SDK. Azure Kubernetes Fleet Manager api client. Package api-version 2025-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Fleets` was modified

* `delete(java.lang.String,java.lang.String)` was removed

### Features Added

* `models.GateConfiguration` was added

* `models.UpdateRunGateStatus` was added

* `models.GatePatch` was added

* `models.Timing` was added

* `models.Gates` was added

* `models.GateType` was added

* `models.GatePatchProperties` was added

* `models.GateProvisioningState` was added

* `models.UpdateRunGateTargetProperties` was added

* `models.GateTarget` was added

* `models.Gate` was added

* `models.GateState` was added

#### `models.AutoUpgradeProfile$Update` was modified

* `withLongTermSupport(java.lang.Boolean)` was added
* `withTargetKubernetesVersion(java.lang.String)` was added

#### `ContainerServiceFleetManager` was modified

* `gates()` was added

#### `models.AutoUpgradeProfile$Definition` was modified

* `withTargetKubernetesVersion(java.lang.String)` was added
* `withLongTermSupport(java.lang.Boolean)` was added

#### `models.FleetMember` was modified

* `labels()` was added

#### `models.UpdateStageStatus` was modified

* `afterGates()` was added
* `beforeGates()` was added

#### `models.Fleets` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added

#### `models.UpdateGroupStatus` was modified

* `afterGates()` was added
* `beforeGates()` was added

#### `models.FleetMember$Definition` was modified

* `withLabels(java.util.Map)` was added

#### `models.FleetMemberUpdate` was modified

* `withLabels(java.util.Map)` was added
* `labels()` was added

#### `models.AutoUpgradeProfile` was modified

* `targetKubernetesVersion()` was added
* `longTermSupport()` was added

#### `models.UpdateGroup` was modified

* `beforeGates()` was added
* `withAfterGates(java.util.List)` was added
* `withBeforeGates(java.util.List)` was added
* `afterGates()` was added

#### `models.UpdateStage` was modified

* `afterGates()` was added
* `beforeGates()` was added
* `withAfterGates(java.util.List)` was added
* `withBeforeGates(java.util.List)` was added

#### `models.FleetMember$Update` was modified

* `withLabels(java.util.Map)` was added

## 1.2.0 (2025-04-10)

- Azure Resource Manager Container Service Fleet client library for Java. This package contains Microsoft Azure SDK for Container Service Fleet Management SDK. Azure Kubernetes Fleet Manager api client. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.FleetMemberListResult` was removed

#### `models.UpdateRunListResult` was removed

#### `models.OperationListResult` was removed

#### `models.FleetUpdateStrategyListResult` was removed

#### `models.FleetListResult` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

### Features Added

* `models.UpgradeChannel` was added

* `models.AutoUpgradeProfile$Update` was added

* `models.AutoUpgradeProfile$Definition` was added

* `models.FleetStatus` was added

* `models.AutoUpgradeProfileProvisioningState` was added

* `models.FleetMemberStatus` was added

* `models.AutoUpgradeNodeImageSelectionType` was added

* `models.AutoUpgradeProfile$UpdateStages` was added

* `models.AutoUpgradeProfiles` was added

* `models.AutoUpgradeProfile` was added

* `models.AutoUpgradeNodeImageSelection` was added

* `models.AutoUpgradeProfile$DefinitionStages` was added

* `models.AutoUpgradeProfileOperations` was added

* `models.AutoUpgradeLastTriggerStatus` was added

* `models.GenerateResponse` was added

* `models.AutoUpgradeProfileStatus` was added

#### `models.ApiServerAccessProfile` was modified

* `withSubnetId(java.lang.String)` was added
* `withEnableVnetIntegration(java.lang.Boolean)` was added
* `enableVnetIntegration()` was added
* `subnetId()` was added

#### `ContainerServiceFleetManager` was modified

* `autoUpgradeProfiles()` was added
* `autoUpgradeProfileOperations()` was added

#### `models.FleetMember` was modified

* `status()` was added

#### `models.Fleet` was modified

* `status()` was added

#### `models.NodeImageSelection` was modified

* `withCustomNodeImageVersions(java.util.List)` was added
* `customNodeImageVersions()` was added

#### `models.UpdateRun` was modified

* `autoUpgradeProfileId()` was added

## 1.2.0-beta.1 (2024-10-17)

- Azure Resource Manager ContainerServiceFleet client library for Java. This package contains Microsoft Azure SDK for ContainerServiceFleet Management SDK. Azure Kubernetes Fleet Manager Client. Package tag package-2024-05-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.UpgradeChannel` was added

* `models.AutoUpgradeProfile$Update` was added

* `models.AutoUpgradeProfile$Definition` was added

* `models.AutoUpgradeProfileProvisioningState` was added

* `models.AutoUpgradeNodeImageSelectionType` was added

* `models.AutoUpgradeProfileListResult` was added

* `models.AutoUpgradeProfile$UpdateStages` was added

* `models.AutoUpgradeProfiles` was added

* `models.AutoUpgradeProfile` was added

* `models.AutoUpgradeNodeImageSelection` was added

* `models.AutoUpgradeProfile$DefinitionStages` was added

#### `models.ApiServerAccessProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `enableVnetIntegration()` was added
* `withEnableVnetIntegration(java.lang.Boolean)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `subnetId()` was added
* `withSubnetId(java.lang.String)` was added

#### `models.FleetMemberListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WaitStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `ContainerServiceFleetManager` was modified

* `autoUpgradeProfiles()` was added

#### `models.AgentProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateRunListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateStageStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedServiceIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateGroupStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FleetHubProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateRunStrategy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NodeImageSelection` was modified

* `customNodeImageVersions()` was added
* `withCustomNodeImageVersions(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NodeImageSelectionStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FleetMemberUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FleetUpdateStrategyListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FleetPatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateGroup` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedClusterUpgradeSpec` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FleetListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateStage` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkipProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MemberUpdateStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FleetCredentialResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedClusterUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkipTarget` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NodeImageVersion` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateRunStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.1.0 (2024-05-17)

- Azure Resource Manager ContainerServiceFleet client library for Java. This package contains Microsoft Azure SDK for ContainerServiceFleet Management SDK. Azure Kubernetes Fleet Manager Client. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ApiServerAccessProfile` was added

* `models.AgentProfile` was added

* `models.FleetHubProfile` was added

* `models.SkipProperties` was added

* `models.TargetType` was added

* `models.SkipTarget` was added

#### `models.Fleet` was modified

* `hubProfile()` was added

#### `models.UpdateRuns` was modified

* `skip(java.lang.String,java.lang.String,java.lang.String,models.SkipProperties)` was added
* `skip(java.lang.String,java.lang.String,java.lang.String,models.SkipProperties,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Fleet$Definition` was modified

* `withHubProfile(models.FleetHubProfile)` was added

#### `models.UpdateRun` was modified

* `skip(models.SkipProperties)` was added
* `skip(models.SkipProperties,java.lang.String,com.azure.core.util.Context)` was added

## 1.1.0-beta.1 (2024-04-11)

- Azure Resource Manager ContainerServiceFleet client library for Java. This package contains Microsoft Azure SDK for ContainerServiceFleet Management SDK. Azure Kubernetes Fleet Manager Client. Package tag package-2024-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ApiServerAccessProfile` was added

* `models.AgentProfile` was added

* `models.FleetHubProfile` was added

* `models.SkipProperties` was added

* `models.TargetType` was added

* `models.SkipTarget` was added

#### `models.Fleet` was modified

* `hubProfile()` was added

#### `models.UpdateRuns` was modified

* `skip(java.lang.String,java.lang.String,java.lang.String,models.SkipProperties,java.lang.String,com.azure.core.util.Context)` was added
* `skip(java.lang.String,java.lang.String,java.lang.String,models.SkipProperties)` was added

#### `models.Fleet$Definition` was modified

* `withHubProfile(models.FleetHubProfile)` was added

#### `models.UpdateRun` was modified

* `skip(models.SkipProperties,java.lang.String,com.azure.core.util.Context)` was added
* `skip(models.SkipProperties)` was added

## 1.0.0 (2023-10-27)

- Azure Resource Manager ContainerServiceFleet client library for Java. This package contains Microsoft Azure SDK for ContainerServiceFleet Management SDK. Azure Kubernetes Fleet Manager Client. Package tag package-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ApiServerAccessProfile` was removed

* `models.AgentProfile` was removed

* `models.FleetHubProfile` was removed

#### `models.Fleet` was modified

* `hubProfile()` was removed

#### `models.Fleet$Definition` was modified

* `withHubProfile(models.FleetHubProfile)` was removed

## 1.0.0-beta.3 (2023-10-23)

- Azure Resource Manager ContainerServiceFleet client library for Java. This package contains Microsoft Azure SDK for ContainerServiceFleet Management SDK. Azure Kubernetes Fleet Manager Client. Package tag package-2023-08-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.FleetUpdateStrategy$DefinitionStages` was added

* `models.FleetUpdateStrategies` was added

* `models.FleetUpdateStrategy` was added

* `models.FleetUpdateStrategy$UpdateStages` was added

* `models.FleetUpdateStrategyProvisioningState` was added

* `models.FleetUpdateStrategyListResult` was added

* `models.FleetUpdateStrategy$Definition` was added

* `models.FleetUpdateStrategy$Update` was added

#### `ContainerServiceFleetManager` was modified

* `fleetUpdateStrategies()` was added

#### `models.AgentProfile` was modified

* `vmSize()` was added
* `withVmSize(java.lang.String)` was added

#### `models.FleetHubProfile` was modified

* `portalFqdn()` was added

#### `models.UpdateRun$Definition` was modified

* `withUpdateStrategyId(java.lang.String)` was added

#### `models.UpdateRun$Update` was modified

* `withUpdateStrategyId(java.lang.String)` was added

#### `models.UpdateRun` was modified

* `updateStrategyId()` was added
* `systemData()` was added

## 1.0.0-beta.2 (2023-09-14)

- Azure Resource Manager ContainerServiceFleet client library for Java. This package contains Microsoft Azure SDK for ContainerServiceFleet Management SDK. Azure Kubernetes Fleet Manager Client. Package tag package-2023-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.UpdateRun` was modified

* `systemData()` was removed

### Features Added

* `models.ApiServerAccessProfile` was added

* `models.UserAssignedIdentity` was added

* `models.AgentProfile` was added

* `models.ManagedServiceIdentity` was added

* `models.NodeImageSelection` was added

* `models.NodeImageSelectionStatus` was added

* `models.NodeImageVersion` was added

* `models.ManagedServiceIdentityType` was added

* `models.NodeImageSelectionType` was added

#### `models.FleetPatch` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

#### `models.Fleet` was modified

* `identity()` was added

#### `models.Fleet$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.FleetHubProfile` was modified

* `agentProfile()` was added
* `apiServerAccessProfile()` was added
* `withAgentProfile(models.AgentProfile)` was added
* `withApiServerAccessProfile(models.ApiServerAccessProfile)` was added

#### `models.MemberUpdateStatus` was modified

* `message()` was added

#### `models.ManagedClusterUpdate` was modified

* `withNodeImageSelection(models.NodeImageSelection)` was added
* `nodeImageSelection()` was added

#### `models.Fleet$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.UpdateRunStatus` was modified

* `nodeImageSelection()` was added

## 1.0.0-beta.1 (2023-06-21)

- Azure Resource Manager ContainerServiceFleet client library for Java. This package contains Microsoft Azure SDK for ContainerServiceFleet Management SDK. Azure Kubernetes Fleet Manager Client. Package tag package-2023-03-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
