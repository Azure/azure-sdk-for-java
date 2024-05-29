# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
