# Release History

## 1.1.0 (2026-02-26)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package api-version 2026-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ManagedProxyResource` was removed

#### `models.NodeTypeListSkuResult` was removed

#### `models.OperationStatus` was removed

#### `models.NodeTypeListResult` was removed

#### `models.ApplicationTypeResourceList` was removed

#### `models.ServiceResourceList` was removed

#### `models.OperationResults` was removed

#### `models.ApplicationResourceList` was removed

#### `models.OperationResultsGetResponse` was removed

#### `models.ManagedVMSizesResult` was removed

#### `models.ApplicationTypeVersionResourceList` was removed

#### `models.OperationResultsGetHeaders` was removed

#### `models.ManagedClusterListResult` was removed

#### `models.OperationListResult` was removed

#### `models.LongRunningOperationResult` was removed

#### `models.ApplicationTypeResource$DefinitionStages` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.ApplicationResource$DefinitionStages` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.ServicePlacementNonPartiallyPlaceServicePolicy` was modified

* `validate()` was removed

#### `models.RollingUpgradeMonitoringPolicy` was modified

* `validate()` was removed

#### `models.IpConfiguration` was modified

* `validate()` was removed

#### `models.NodeTypeUpdateParameters` was modified

* `validate()` was removed

#### `models.ServiceUpdateParameters` was modified

* `validate()` was removed

#### `models.SettingsParameterDescription` was modified

* `validate()` was removed

#### `models.ServiceEndpoint` was modified

* `validate()` was removed

#### `models.EndpointRangeDescription` was modified

* `validate()` was removed

#### `models.LoadBalancingRule` was modified

* `validate()` was removed

#### `models.ApplicationHealthPolicy` was modified

* `validate()` was removed

#### `models.NamedPartitionScheme` was modified

* `validate()` was removed

#### `models.VaultCertificate` was modified

* `validate()` was removed

#### `models.VmImagePlan` was modified

* `validate()` was removed

#### `models.Partition` was modified

* `validate()` was removed

#### `models.ResourceAzStatus` was modified

* `ResourceAzStatus()` was changed to private access
* `validate()` was removed

#### `models.ApplicationUpgradePolicy` was modified

* `validate()` was removed

#### `models.ManagedClusterUpdateParameters` was modified

* `validate()` was removed

#### `models.ApplicationUserAssignedIdentity` was modified

* `validate()` was removed

#### `models.IpTag` was modified

* `validate()` was removed

#### `models.ApplicationTypeVersionUpdateParameters` was modified

* `validate()` was removed

#### `ServiceFabricManagedClustersManager` was modified

* `operationStatus()` was removed
* `operationResults()` was removed

#### `models.ServiceTypeHealthPolicy` was modified

* `validate()` was removed

#### `models.VaultSecretGroup` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed

#### `models.AzureActiveDirectory` was modified

* `validate()` was removed

#### `models.ScalingMechanism` was modified

* `validate()` was removed

#### `models.AveragePartitionLoadScalingTrigger` was modified

* `validate()` was removed

#### `models.ServiceLoadMetric` was modified

* `validate()` was removed

#### `models.ClientCertificate` was modified

* `validate()` was removed

#### `models.PartitionInstanceCountScaleMechanism` was modified

* `validate()` was removed

#### `models.AvailableOperationDisplay` was modified

* `AvailableOperationDisplay()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApplicationUpdateParameters` was modified

* `validate()` was removed

#### `models.NodeTypeActionParameters` was modified

* `validate()` was removed

#### `models.StatefulServiceProperties` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.ApplicationTypeVersionsCleanupPolicy` was modified

* `validate()` was removed

#### `models.NodeTypeNatConfig` was modified

* `validate()` was removed

#### `models.ServicePlacementInvalidDomainPolicy` was modified

* `validate()` was removed

#### `models.SettingsSectionDescription` was modified

* `validate()` was removed

#### `models.VmManagedIdentity` was modified

* `validate()` was removed

#### `models.ClusterUpgradePolicy` was modified

* `validate()` was removed

#### `models.ServiceCorrelation` was modified

* `validate()` was removed

#### `models.AddRemoveIncrementalNamedPartitionScalingMechanism` was modified

* `validate()` was removed

#### `models.ManagedClusterCodeVersionResult` was modified

* `java.lang.String supportExpiryUtc()` -> `java.time.OffsetDateTime supportExpiryUtc()`

#### `models.ServicePlacementRequireDomainDistributionPolicy` was modified

* `validate()` was removed

#### `models.NetworkSecurityRule` was modified

* `validate()` was removed

#### `models.ClusterUpgradeDeltaHealthPolicy` was modified

* `validate()` was removed

#### `models.AdditionalNetworkInterfaceConfiguration` was modified

* `validate()` was removed

#### `models.UniformInt64RangePartitionScheme` was modified

* `validate()` was removed

#### `models.ServicePlacementPolicy` was modified

* `validate()` was removed

#### `models.StatelessServiceProperties` was modified

* `validate()` was removed

#### `models.VmssDataDisk` was modified

* `validate()` was removed

#### `models.VMSize` was modified

* `VMSize()` was changed to private access
* `validate()` was removed

#### `models.ClusterHealthPolicy` was modified

* `validate()` was removed

#### `models.ManagedMaintenanceWindowStatus` was modified

* `lastWindowEndTimeUtc()` was removed
* `lastWindowStartTimeUtc()` was removed
* `lastWindowStatusUpdateAtUtc()` was removed

#### `models.ApplicationTypeResource$Definition` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed

#### `models.ServiceResourcePropertiesBase` was modified

* `validate()` was removed

#### `models.ScalingPolicy` was modified

* `validate()` was removed

#### `models.Subnet` was modified

* `validate()` was removed

#### `models.RuntimeResumeApplicationUpgradeParameters` was modified

* `validate()` was removed

#### `models.ApplicationTypeUpdateParameters` was modified

* `validate()` was removed

#### `models.ClusterMonitoringPolicy` was modified

* `validate()` was removed

#### `models.ScalingTrigger` was modified

* `validate()` was removed

#### `models.ServicePlacementPreferPrimaryDomainPolicy` was modified

* `validate()` was removed

#### `models.ManagedIdentity` was modified

* `validate()` was removed

#### `models.AverageServiceLoadScalingTrigger` was modified

* `validate()` was removed

#### `models.VmssExtension` was modified

* `validate()` was removed

#### `models.FrontendConfiguration` was modified

* `validate()` was removed

#### `models.IpConfigurationPublicIpAddressConfiguration` was modified

* `validate()` was removed

#### `models.NodeTypeSku` was modified

* `validate()` was removed

#### `models.NodeTypeSkuCapacity` was modified

* `NodeTypeSkuCapacity()` was changed to private access
* `validate()` was removed

#### `models.ServicePlacementRequiredDomainPolicy` was modified

* `validate()` was removed

#### `models.ServiceResourceProperties` was modified

* `validate()` was removed

#### `models.ApplicationResource$Definition` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed

#### `models.SingletonPartitionScheme` was modified

* `validate()` was removed

#### `models.NodeTypeSupportedSku` was modified

* `NodeTypeSupportedSku()` was changed to private access
* `validate()` was removed

### Features Added

* `models.RuntimeUpdateApplicationUpgradeParameters` was added

* `models.RestartKind` was added

* `models.ApplicationFetchHealthRequest` was added

* `models.RestartDeployedCodePackageRequest` was added

* `models.RestartReplicaRequest` was added

* `models.RuntimeApplicationHealthPolicy` was added

* `models.VmApplication` was added

* `models.RuntimeRollingUpgradeMode` was added

* `models.RuntimeRollingUpgradeUpdateMonitoringPolicy` was added

* `models.SecurityEncryptionType` was added

* `models.RuntimeUpgradeKind` was added

* `models.AutoGeneratedDomainNameLabelScope` was added

* `models.RuntimeServiceTypeHealthPolicy` was added

* `models.RuntimeFailureAction` was added

* `models.ApplicationUpdateParametersProperties` was added

* `models.HealthFilter` was added

#### `models.ManagedCluster$Definition` was modified

* `withEnableOutboundOnlyNodeTypes(java.lang.Boolean)` was added
* `withAllocatedOutboundPorts(java.lang.Integer)` was added
* `withAutoGeneratedDomainNameLabelScope(models.AutoGeneratedDomainNameLabelScope)` was added
* `withVmImage(java.lang.String)` was added
* `withSkipManagedNsgAssignment(java.lang.Boolean)` was added

#### `models.ServiceEndpoint` was modified

* `networkIdentifier()` was added
* `withNetworkIdentifier(java.lang.String)` was added

#### `models.NodeType$Definition` was modified

* `withVmApplications(java.util.List)` was added
* `withEnableResilientEphemeralOsDisk(java.lang.Boolean)` was added
* `withIsOutboundOnly(java.lang.Boolean)` was added
* `withSecurityEncryptionType(models.SecurityEncryptionType)` was added
* `withZoneBalance(java.lang.Boolean)` was added

#### `models.ApplicationResource` was modified

* `restartDeployedCodePackage(models.RestartDeployedCodePackageRequest,com.azure.core.util.Context)` was added
* `updateUpgrade(models.RuntimeUpdateApplicationUpgradeParameters,com.azure.core.util.Context)` was added
* `fetchHealth(models.ApplicationFetchHealthRequest,com.azure.core.util.Context)` was added
* `readUpgrade(com.azure.core.util.Context)` was added
* `updateUpgrade(models.RuntimeUpdateApplicationUpgradeParameters)` was added
* `restartDeployedCodePackage(models.RestartDeployedCodePackageRequest)` was added
* `fetchHealth(models.ApplicationFetchHealthRequest)` was added
* `startRollback(com.azure.core.util.Context)` was added
* `readUpgrade()` was added
* `resumeUpgrade(models.RuntimeResumeApplicationUpgradeParameters,com.azure.core.util.Context)` was added
* `resumeUpgrade(models.RuntimeResumeApplicationUpgradeParameters)` was added
* `startRollback()` was added

#### `models.NodeTypes` was modified

* `redeploy(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters)` was added
* `deallocate(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `redeploy(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `deallocate(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters)` was added

#### `models.ApplicationUpdateParameters` was modified

* `withProperties(models.ApplicationUpdateParametersProperties)` was added
* `properties()` was added

#### `models.Services` was modified

* `restartReplica(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.RestartReplicaRequest)` was added
* `restartReplica(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.RestartReplicaRequest,com.azure.core.util.Context)` was added

#### `models.Applications` was modified

* `restartDeployedCodePackage(java.lang.String,java.lang.String,java.lang.String,models.RestartDeployedCodePackageRequest)` was added
* `updateUpgrade(java.lang.String,java.lang.String,java.lang.String,models.RuntimeUpdateApplicationUpgradeParameters)` was added
* `fetchHealth(java.lang.String,java.lang.String,java.lang.String,models.ApplicationFetchHealthRequest,com.azure.core.util.Context)` was added
* `restartDeployedCodePackage(java.lang.String,java.lang.String,java.lang.String,models.RestartDeployedCodePackageRequest,com.azure.core.util.Context)` was added
* `fetchHealth(java.lang.String,java.lang.String,java.lang.String,models.ApplicationFetchHealthRequest)` was added
* `updateUpgrade(java.lang.String,java.lang.String,java.lang.String,models.RuntimeUpdateApplicationUpgradeParameters,com.azure.core.util.Context)` was added

#### `models.NodeType` was modified

* `redeploy(models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `isOutboundOnly()` was added
* `zoneBalance()` was added
* `enableResilientEphemeralOsDisk()` was added
* `deallocate(models.NodeTypeActionParameters)` was added
* `securityEncryptionType()` was added
* `start(models.NodeTypeActionParameters)` was added
* `start(models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `redeploy(models.NodeTypeActionParameters)` was added
* `vmApplications()` was added
* `deallocate(models.NodeTypeActionParameters,com.azure.core.util.Context)` was added

#### `models.ManagedMaintenanceWindowStatus` was modified

* `lastWindowStatusUpdateAtUTC()` was added
* `lastWindowEndTimeUTC()` was added
* `lastWindowStartTimeUTC()` was added

#### `models.ApplicationTypeResource$Definition` was modified

* `withExistingManagedCluster(java.lang.String,java.lang.String)` was added

#### `models.ServiceResource` was modified

* `restartReplica(models.RestartReplicaRequest,com.azure.core.util.Context)` was added
* `restartReplica(models.RestartReplicaRequest)` was added

#### `models.ApplicationResource$Update` was modified

* `withProperties(models.ApplicationUpdateParametersProperties)` was added

#### `models.ManagedCluster` was modified

* `allocatedOutboundPorts()` was added
* `autoGeneratedDomainNameLabelScope()` was added
* `vmImage()` was added
* `enableOutboundOnlyNodeTypes()` was added
* `skipManagedNsgAssignment()` was added

#### `models.ApplicationResource$Definition` was modified

* `withExistingManagedCluster(java.lang.String,java.lang.String)` was added

## 1.1.0-beta.3 (2025-11-10)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package api-version 2025-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.RuntimeUpdateApplicationUpgradeParameters` was added

* `models.RestartKind` was added

* `models.ApplicationFetchHealthRequest` was added

* `models.RestartDeployedCodePackageRequest` was added

* `models.RestartReplicaRequest` was added

* `models.RuntimeApplicationHealthPolicy` was added

* `models.RuntimeRollingUpgradeMode` was added

* `models.RuntimeRollingUpgradeUpdateMonitoringPolicy` was added

* `models.RuntimeUpgradeKind` was added

* `models.RuntimeServiceTypeHealthPolicy` was added

* `models.RuntimeFailureAction` was added

* `models.ApplicationUpdateParametersProperties` was added

* `models.HealthFilter` was added

#### `models.ApplicationResource` was modified

* `updateUpgrade(models.RuntimeUpdateApplicationUpgradeParameters)` was added
* `restartDeployedCodePackage(models.RestartDeployedCodePackageRequest)` was added
* `fetchHealth(models.ApplicationFetchHealthRequest)` was added
* `updateUpgrade(models.RuntimeUpdateApplicationUpgradeParameters,com.azure.core.util.Context)` was added
* `restartDeployedCodePackage(models.RestartDeployedCodePackageRequest,com.azure.core.util.Context)` was added
* `fetchHealth(models.ApplicationFetchHealthRequest,com.azure.core.util.Context)` was added

#### `models.ApplicationUpdateParameters` was modified

* `withProperties(models.ApplicationUpdateParametersProperties)` was added
* `properties()` was added

#### `models.Services` was modified

* `restartReplica(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.RestartReplicaRequest,com.azure.core.util.Context)` was added
* `restartReplica(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.RestartReplicaRequest)` was added

#### `models.Applications` was modified

* `fetchHealth(java.lang.String,java.lang.String,java.lang.String,models.ApplicationFetchHealthRequest)` was added
* `updateUpgrade(java.lang.String,java.lang.String,java.lang.String,models.RuntimeUpdateApplicationUpgradeParameters)` was added
* `updateUpgrade(java.lang.String,java.lang.String,java.lang.String,models.RuntimeUpdateApplicationUpgradeParameters,com.azure.core.util.Context)` was added
* `restartDeployedCodePackage(java.lang.String,java.lang.String,java.lang.String,models.RestartDeployedCodePackageRequest)` was added
* `restartDeployedCodePackage(java.lang.String,java.lang.String,java.lang.String,models.RestartDeployedCodePackageRequest,com.azure.core.util.Context)` was added
* `fetchHealth(java.lang.String,java.lang.String,java.lang.String,models.ApplicationFetchHealthRequest,com.azure.core.util.Context)` was added

#### `models.ServiceResource` was modified

* `restartReplica(models.RestartReplicaRequest)` was added
* `restartReplica(models.RestartReplicaRequest,com.azure.core.util.Context)` was added

#### `models.ApplicationResource$Update` was modified

* `withProperties(models.ApplicationUpdateParametersProperties)` was added

## 1.1.0-beta.2 (2025-08-11)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package api-version 2025-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ManagedCluster$Definition` was modified

* `withEnableOutboundOnlyNodeTypes(java.lang.Boolean)` was added

#### `models.ServiceEndpoint` was modified

* `networkIdentifier()` was added
* `withNetworkIdentifier(java.lang.String)` was added

#### `models.NodeType$Definition` was modified

* `withIsOutboundOnly(java.lang.Boolean)` was added

#### `models.NodeType` was modified

* `isOutboundOnly()` was added

#### `models.ManagedCluster` was modified

* `enableOutboundOnlyNodeTypes()` was added

## 1.1.0-beta.1 (2025-06-21)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package api-version 2025-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ManagedProxyResource` was removed

#### `models.NodeTypeListSkuResult` was removed

#### `models.OperationStatus` was removed

#### `models.NodeTypeListResult` was removed

#### `models.ApplicationTypeResourceList` was removed

#### `models.ServiceResourceList` was removed

#### `models.OperationResults` was removed

#### `models.ApplicationResourceList` was removed

#### `models.OperationResultsGetResponse` was removed

#### `models.ManagedVMSizesResult` was removed

#### `models.ApplicationTypeVersionResourceList` was removed

#### `models.OperationResultsGetHeaders` was removed

#### `models.ManagedClusterListResult` was removed

#### `models.OperationListResult` was removed

#### `models.LongRunningOperationResult` was removed

#### `models.ApplicationTypeResource$DefinitionStages` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.ApplicationResource$DefinitionStages` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed in stage 1

#### `ServiceFabricManagedClustersManager` was modified

* `operationStatus()` was removed
* `operationResults()` was removed

#### `models.AvailableOperationDisplay` was modified

* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed

#### `models.ManagedClusterCodeVersionResult` was modified

* `java.lang.String supportExpiryUtc()` -> `java.time.OffsetDateTime supportExpiryUtc()`

#### `models.ManagedMaintenanceWindowStatus` was modified

* `lastWindowStartTimeUtc()` was removed
* `lastWindowStatusUpdateAtUtc()` was removed
* `lastWindowEndTimeUtc()` was removed

#### `models.ApplicationTypeResource$Definition` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed

#### `models.ApplicationResource$Definition` was modified

* `withExistingManagedcluster(java.lang.String,java.lang.String)` was removed

### Features Added

* `implementation.models.NodeTypeListSkuResult` was added

* `implementation.models.ApplicationTypeVersionResourceList` was added

* `models.SfmcOperationStatus` was added

* `models.FaultSimulationContentWrapper` was added

* `implementation.models.NodeTypeListResult` was added

* `implementation.models.ApplicationResourceList` was added

* `models.FaultSimulationStatus` was added

* `models.ZoneFaultSimulationContent` was added

* `implementation.models.FaultSimulationListResult` was added

* `implementation.models.OperationListResult` was added

* `models.FaultSimulationDetails` was added

* `models.VmApplication` was added

* `models.FaultSimulation` was added

* `implementation.models.ManagedVMSizesResult` was added

* `models.SecurityEncryptionType` was added

* `models.NodeTypeFaultSimulation` was added

* `models.AutoGeneratedDomainNameLabelScope` was added

* `implementation.models.ApplicationTypeResourceList` was added

* `implementation.models.ServiceResourceList` was added

* `models.FaultKind` was added

* `models.FaultSimulationContent` was added

* `models.FaultSimulationIdContent` was added

* `implementation.models.ManagedClusterListResult` was added

* `models.FaultSimulationConstraints` was added

#### `models.ManagedCluster$Definition` was modified

* `withAllocatedOutboundPorts(java.lang.Integer)` was added
* `withVmImage(java.lang.String)` was added
* `withAutoGeneratedDomainNameLabelScope(models.AutoGeneratedDomainNameLabelScope)` was added

#### `models.NodeType$Definition` was modified

* `withSecurityEncryptionType(models.SecurityEncryptionType)` was added
* `withZoneBalance(java.lang.Boolean)` was added
* `withVmApplications(java.util.List)` was added

#### `models.ApplicationResource` was modified

* `startRollback()` was added
* `readUpgrade()` was added
* `resumeUpgrade(models.RuntimeResumeApplicationUpgradeParameters)` was added
* `startRollback(com.azure.core.util.Context)` was added
* `readUpgrade(com.azure.core.util.Context)` was added
* `resumeUpgrade(models.RuntimeResumeApplicationUpgradeParameters,com.azure.core.util.Context)` was added

#### `models.NodeTypes` was modified

* `start(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `deallocate(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `getFaultSimulation(java.lang.String,java.lang.String,java.lang.String,models.FaultSimulationIdContent)` was added
* `redeploy(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters)` was added
* `stopFaultSimulation(java.lang.String,java.lang.String,java.lang.String,models.FaultSimulationIdContent)` was added
* `startFaultSimulation(java.lang.String,java.lang.String,java.lang.String,models.FaultSimulationContentWrapper)` was added
* `stopFaultSimulation(java.lang.String,java.lang.String,java.lang.String,models.FaultSimulationIdContent,com.azure.core.util.Context)` was added
* `getFaultSimulationWithResponse(java.lang.String,java.lang.String,java.lang.String,models.FaultSimulationIdContent,com.azure.core.util.Context)` was added
* `deallocate(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters)` was added
* `redeploy(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,models.NodeTypeActionParameters)` was added
* `startFaultSimulation(java.lang.String,java.lang.String,java.lang.String,models.FaultSimulationContentWrapper,com.azure.core.util.Context)` was added
* `listFaultSimulation(java.lang.String,java.lang.String,java.lang.String)` was added
* `listFaultSimulation(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ManagedClusters` was modified

* `listFaultSimulation(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `startFaultSimulation(java.lang.String,java.lang.String,models.FaultSimulationContentWrapper)` was added
* `listFaultSimulation(java.lang.String,java.lang.String)` was added
* `stopFaultSimulation(java.lang.String,java.lang.String,models.FaultSimulationIdContent,com.azure.core.util.Context)` was added
* `startFaultSimulation(java.lang.String,java.lang.String,models.FaultSimulationContentWrapper,com.azure.core.util.Context)` was added
* `getFaultSimulationWithResponse(java.lang.String,java.lang.String,models.FaultSimulationIdContent,com.azure.core.util.Context)` was added
* `getFaultSimulation(java.lang.String,java.lang.String,models.FaultSimulationIdContent)` was added
* `stopFaultSimulation(java.lang.String,java.lang.String,models.FaultSimulationIdContent)` was added

#### `models.NodeType` was modified

* `start(models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `getFaultSimulationWithResponse(models.FaultSimulationIdContent,com.azure.core.util.Context)` was added
* `listFaultSimulation()` was added
* `redeploy(models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `deallocate(models.NodeTypeActionParameters)` was added
* `startFaultSimulation(models.FaultSimulationContentWrapper,com.azure.core.util.Context)` was added
* `securityEncryptionType()` was added
* `listFaultSimulation(com.azure.core.util.Context)` was added
* `startFaultSimulation(models.FaultSimulationContentWrapper)` was added
* `zoneBalance()` was added
* `deallocate(models.NodeTypeActionParameters,com.azure.core.util.Context)` was added
* `start(models.NodeTypeActionParameters)` was added
* `redeploy(models.NodeTypeActionParameters)` was added
* `stopFaultSimulation(models.FaultSimulationIdContent)` was added
* `vmApplications()` was added
* `getFaultSimulation(models.FaultSimulationIdContent)` was added
* `stopFaultSimulation(models.FaultSimulationIdContent,com.azure.core.util.Context)` was added

#### `models.ManagedMaintenanceWindowStatus` was modified

* `lastWindowEndTimeUTC()` was added
* `lastWindowStatusUpdateAtUTC()` was added
* `lastWindowStartTimeUTC()` was added

#### `models.ApplicationTypeResource$Definition` was modified

* `withExistingManagedCluster(java.lang.String,java.lang.String)` was added

#### `models.ManagedCluster` was modified

* `stopFaultSimulation(models.FaultSimulationIdContent)` was added
* `stopFaultSimulation(models.FaultSimulationIdContent,com.azure.core.util.Context)` was added
* `startFaultSimulation(models.FaultSimulationContentWrapper,com.azure.core.util.Context)` was added
* `autoGeneratedDomainNameLabelScope()` was added
* `allocatedOutboundPorts()` was added
* `listFaultSimulation()` was added
* `getFaultSimulationWithResponse(models.FaultSimulationIdContent,com.azure.core.util.Context)` was added
* `getFaultSimulation(models.FaultSimulationIdContent)` was added
* `listFaultSimulation(com.azure.core.util.Context)` was added
* `vmImage()` was added
* `startFaultSimulation(models.FaultSimulationContentWrapper)` was added

#### `models.ApplicationResource$Definition` was modified

* `withExistingManagedCluster(java.lang.String,java.lang.String)` was added

## 1.0.0 (2024-12-25)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager Service Fabric Managed Clusters client library for Java.

## 1.0.0-beta.3 (2024-12-16)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package tag package-2024-09-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ManagedCluster$Definition` was modified

* `withCustomFqdn(java.lang.String)` was removed

#### `models.StatefulServiceProperties` was modified

* `provisioningState()` was removed

#### `models.StatelessServiceProperties` was modified

* `provisioningState()` was removed

#### `models.ManagedCluster` was modified

* `customFqdn()` was removed

### Features Added

#### `models.ManagedCluster$Definition` was modified

* `withAllocatedOutboundPorts(java.lang.Integer)` was added

#### `models.ManagedCluster` was modified

* `allocatedOutboundPorts()` was added

## 1.0.0-beta.2 (2024-10-18)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package tag package-2024-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.VmApplication` was added

* `models.AutoGeneratedDomainNameLabelScope` was added

#### `models.ManagedCluster$Definition` was modified

* `withCustomFqdn(java.lang.String)` was added
* `withAutoGeneratedDomainNameLabelScope(models.AutoGeneratedDomainNameLabelScope)` was added

#### `models.NodeType$Definition` was modified

* `withVmApplications(java.util.List)` was added

#### `models.NodeType` was modified

* `vmApplications()` was added

#### `models.ManagedCluster` was modified

* `autoGeneratedDomainNameLabelScope()` was added
* `customFqdn()` was added

## 1.0.0-beta.1 (2024-07-31)

- Azure Resource Manager Service Fabric Managed Clusters client library for Java. This package contains Microsoft Azure SDK for Service Fabric Managed Clusters Management SDK. Service Fabric Managed Clusters Management Client. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
- Azure Resource Manager ServiceFabricManagedClusters client library for Java. This package contains Microsoft Azure SDK for ServiceFabricManagedClusters Management SDK. Service Fabric Managed Clusters Management Client. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-servicefabricmanagedclusters Java SDK.

