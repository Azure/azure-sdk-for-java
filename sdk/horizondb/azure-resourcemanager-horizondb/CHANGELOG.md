# Release History

## 1.0.0-beta.3 (2026-10-15)

- Azure Resource Manager HorizonDb client library for Java. This package contains Microsoft Azure SDK for HorizonDb Management SDK. Azure Resource Provider API for managing HorizonDB clusters, pools, endpoints, nodes, and firewall rules. Package api-version 2026-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.HorizonDbPoolProperties` was removed

#### `models.HorizonDbReplica` was removed

#### `models.HorizonDbAdministrators` was removed

#### `models.HorizonDbAdministrator$UpdateStages` was removed

#### `models.HorizonDbAdministratorAdd` was removed

#### `models.HorizonDbParameterGroups` was removed

#### `models.HorizonDbReplica$UpdateStages` was removed

#### `models.HorizonDbReplicaProperties` was removed

#### `models.ReplicaRole` was removed

#### `models.HorizonDbReplicaForPatchUpdate` was removed

#### `models.HorizonDbFirewallRule$DefinitionStages` was removed

#### `models.ZonePlacementPolicy` was removed

#### `models.HorizonDbPrivateLinkResource` was removed

#### `models.HorizonDbParameterGroupPropertiesForPatchUpdate` was removed

#### `models.CreateModeCluster` was removed

#### `models.HorizonDbAdministrator$Update` was removed

#### `models.HorizonDbComputeModelType` was removed

#### `models.HorizonDbParameterGroup$UpdateStages` was removed

#### `models.HorizonDbParameterGroup` was removed

#### `models.HorizonDbPools` was removed

#### `models.HorizonDbFirewallRule$Definition` was removed

#### `models.HorizonDbPrivateLinkResources` was removed

#### `models.HorizonDbParameterGroupForPatchUpdate` was removed

#### `models.HorizonDbAdministratorProperties` was removed

#### `models.HorizonDbCluster$Definition` was removed

#### `models.HorizonDbAdministrator$Definition` was removed

#### `models.HorizonDbAdministrator` was removed

#### `models.HorizonDbCluster$Update` was removed

#### `models.HorizonDbReplica$Definition` was removed

#### `models.HorizonDbClusters` was removed

#### `models.HorizonDbPrivateEndpointConnections` was removed

#### `models.HorizonDbPool` was removed

#### `models.CreateModePool` was removed

#### `models.HorizonDbCluster$UpdateStages` was removed

#### `models.ManagedServiceIdentity` was removed

#### `models.HorizonDbFirewallRuleProperties` was removed

#### `models.HorizonDbParameterGroupProperties` was removed

#### `models.ManagedServiceIdentityType` was removed

#### `models.HorizonDbReplicaPropertiesForPatchUpdate` was removed

#### `models.HorizonDbClusterPropertiesForPatchUpdate` was removed

#### `models.HorizonDbComputeModel` was removed

#### `models.HorizonDbAdministratorPropertiesForAdd` was removed

#### `models.HorizonDbReplica$Update` was removed

#### `models.HorizonDbCluster` was removed

#### `models.HorizonDbParameterGroupConnectionProperties` was removed

#### `models.HorizonDbParameterGroup$Definition` was removed

#### `models.HorizonDbClusterProperties` was removed

#### `models.HorizonDbClusterForPatchUpdate` was removed

#### `models.HorizonDbReplicas` was removed

#### `models.HorizonDbFirewallRule$Update` was removed

#### `models.HorizonDbParameterGroup$DefinitionStages` was removed

#### `models.HorizonDbFirewallRule$UpdateStages` was removed

#### `models.HorizonDbCluster$DefinitionStages` was removed

#### `models.HorizonDbClusterMirroring` was removed

#### `models.HorizonDbReplica$DefinitionStages` was removed

#### `models.HorizonDbClusterAuthConfig` was removed

#### `models.HorizonDbAdministrator$DefinitionStages` was removed

#### `models.HorizonDbClusterParameterGroupConnectionProperties` was removed

#### `models.HorizonDbFirewallRule` was removed

#### `models.HorizonDbFirewallRules` was removed

#### `models.HorizonDbParameterGroup$Update` was removed

#### `models.State` was removed

#### `models.Network` was modified

* `publicNetworkAccess()` was removed

#### `HorizonDbManager` was modified

* `horizonDbReplicas()` was removed
* `horizonDbAdministrators()` was removed
* `horizonDbPrivateEndpointConnections()` was removed
* `horizonDbFirewallRules()` was removed
* `horizonDbPools()` was removed
* `horizonDbPrivateLinkResources()` was removed
* `horizonDbParameterGroups()` was removed
* `horizonDbClusters()` was removed

### Features Added

* `models.GeoRedundancyState` was added

* `models.Clusters` was added

* `models.PrivateEndpointConnections` was added

* `models.AuthenticationProperties` was added

* `models.ChangeDataCaptureIdentityActivationState` was added

* `models.MicrosoftEntraAuthenticationForPatchUpdate` was added

* `models.ScheduledMaintenancePropertiesForPatchUpdate` was added

* `models.Pool$UpdateStages` was added

* `models.ClusterCreateMode` was added

* `models.Nodes` was added

* `models.DefaultParameterGroup` was added

* `models.PoolForPatchUpdate` was added

* `models.ParameterGroup` was added

* `models.ChangeDataCaptureConfiguration$UpdateStages` was added

* `models.ChangeDataCaptureProtocol` was added

* `models.MaintenanceType` was added

* `models.ChangeDataCaptureReplicationState` was added

* `models.ChangeDataCaptureDestination$Update` was added

* `models.PoolFeatureCapability` was added

* `models.ClusterState` was added

* `models.ChangeDataCaptureCapabilities` was added

* `models.ChangeDataCaptureDesiredState` was added

* `models.ChangeDataCaptureExtensionStatus` was added

* `models.BackupProperties` was added

* `models.Endpoints` was added

* `models.ChangeDataCaptureConfiguration$Definition` was added

* `models.Pool$Definition` was added

* `models.ChangeDataCaptureDestinationDetails` was added

* `models.PrivateEndpointConnectionResource$Update` was added

* `models.ChangeDataCaptureConfiguration$DefinitionStages` was added

* `models.BackupPropertiesForPatchUpdate` was added

* `models.Cluster$UpdateStages` was added

* `models.LogCaptureConfiguration` was added

* `models.PoolComputeModelForPatchUpdate` was added

* `models.LogCaptureState` was added

* `models.MicrosoftEntraAdministratorAdd` was added

* `models.MaintenanceEvent` was added

* `models.SystemScheduledMaintenanceProperties` was added

* `models.PasswordAuthentications` was added

* `models.PoolCapability` was added

* `models.LogFileDownloadUrl` was added

* `models.ParameterApplyMethod` was added

* `models.Authentication` was added

* `models.HorizonDbClusterDataEncryption` was added

* `models.ChangeDataCaptureCapability` was added

* `models.ClusterCapability` was added

* `models.PrivateEndpointConnectionResource$Definition` was added

* `models.EndpointRole` was added

* `models.ChangeDataCaptureCapabilityPropertiesForPatchUpdate` was added

* `models.ChangeDataCaptureEffectiveState` was added

* `models.PasswordAuthenticationPropertiesForPatchUpdate` was added

* `models.ClusterPropertiesForPatchUpdate` was added

* `models.ChangeDataCaptureCapabilityProperties` was added

* `models.ParameterGroup$DefinitionStages` was added

* `models.ChangeDataCaptureDestination$Definition` was added

* `models.FirewallRule$DefinitionStages` was added

* `models.ParameterGroupProperties` was added

* `models.MicrosoftEntraAdministrator$Definition` was added

* `models.ChangeDataCaptureConfigurationPropertiesForPatchUpdate` was added

* `models.HorizonDbClusterManagedServiceIdentity` was added

* `models.Capabilities` was added

* `models.NodeRole` was added

* `models.DefaultParameterGroups` was added

* `models.ParameterGroup$Definition` was added

* `models.ChangeDataCaptureStatus` was added

* `models.MicrosoftEntraAdministrator$DefinitionStages` was added

* `models.PrivateEndpointConnectionResource$UpdateStages` was added

* `models.Pool$DefinitionStages` was added

* `models.MaintenanceEventProperties` was added

* `models.MicrosoftEntraAuthenticationPropertiesForPatchUpdate` was added

* `models.ChangeDataCaptureCapabilityForPatchUpdate` was added

* `models.ChangeDataCaptureEngineConfigurationState` was added

* `models.Cluster$Update` was added

* `models.MicrosoftEntraAdministratorProperties` was added

* `models.EndpointProperties` was added

* `models.ChangeDataCaptureReplicationArchitecture` was added

* `models.NodeProperties` was added

* `models.PoolHighAvailability` was added

* `models.LogCaptureConfigurationProperties` was added

* `models.ParameterGroup$Update` was added

* `models.PoolHighAvailabilityPatch` was added

* `models.ChangeDataCaptureReplicationStatus` was added

* `models.Pools` was added

* `models.PoolProperties` was added

* `models.ChangeDataCaptureIdentityValidationState` was added

* `models.LogFile` was added

* `models.ParameterValueSource` was added

* `models.ScheduledMaintenanceManagementType` was added

* `models.ParameterGroup$UpdateStages` was added

* `models.LogFiles` was added

* `models.PoolHighAvailabilityMode` was added

* `models.ClusterProperties` was added

* `models.FirewallRules` was added

* `models.ClusterForPatchUpdate` was added

* `models.ParameterGroupSyncStatus` was added

* `models.MicrosoftEntraAdministrator` was added

* `models.ChangeDataCaptureDestinationProperties` was added

* `models.ChangeDataCaptureIdentityAssignmentState` was added

* `models.PoolParameterGroupReference` was added

* `models.PasswordAuthenticationForPatchUpdate` was added

* `models.ChangeDataCaptureConfiguration$Update` was added

* `models.MicrosoftEntraAuthentications` was added

* `models.ReadWritePoolProperties` was added

* `models.HorizonDbClusterEncryptionKeyStatus` was added

* `models.ActivateChangeDataCaptureIdentityRequest` was added

* `models.FirewallRule$Definition` was added

* `models.ChangeDataCaptureDestinationEffectiveState` was added

* `models.PoolParameterGroupReferenceForPatchUpdate` was added

* `models.Pool` was added

* `models.LogCaptureConfigurationPropertiesForPatchUpdate` was added

* `models.ActivateChangeDataCaptureIdentityResult` was added

* `models.ComputeModelCapability` was added

* `models.ChangeDataCaptureConfigurations` was added

* `models.ChangeDataCaptureDestinationPropertiesForPatchUpdate` was added

* `models.Cluster` was added

* `models.Pool$Update` was added

* `models.ParameterGroupConnectionProperties` was added

* `models.ChangeDataCaptureConfiguration` was added

* `models.AiModelManagementStatus` was added

* `models.LogFileProperties` was added

* `models.Node` was added

* `models.ClusterFeatureCapability` was added

* `models.ChangeDataCaptureDestination$DefinitionStages` was added

* `models.ChangeDataCaptureConfigurationForPatchUpdate` was added

* `models.MaintenanceEvents` was added

* `models.PostgreSqlCapabilityVersion` was added

* `models.ComputeGenerationCapability` was added

* `models.PasswordAuthentication` was added

* `models.HorizonDbClusterDataEncryptionForPatchUpdate` was added

* `models.MaintenanceEventRescheduleRequest` was added

* `models.FirewallRuleProperties` was added

* `models.ChangeDataCaptureTopologyBindingStatus` was added

* `models.ChangeDataCaptureTopologyBindingState` was added

* `models.ComputeSizeCapability` was added

* `models.PoolComputeModelType` was added

* `models.LogCaptureConfigurations` was added

* `models.CapabilityProperties` was added

* `models.ChangeDataCaptureConfigurationProperties` was added

* `models.ParameterGroups` was added

* `models.IdentityAssignment$Definition` was added

* `models.MicrosoftEntraAuthenticationProperties` was added

* `models.ChangeDataCaptureDestination$UpdateStages` was added

* `models.Endpoint` was added

* `models.ChangeDataCaptureDestinations` was added

* `models.ChangeDataCaptureDestinationForPatchUpdate` was added

* `models.MicrosoftEntraAdministratorPropertiesForAdd` was added

* `models.NetworkForPatchUpdate` was added

* `models.MaintenanceEventActionResponse` was added

* `models.ComputeProcessorCapability` was added

* `models.LogFileType` was added

* `models.ChangeDataCaptureReadWriteEndpointState` was added

* `models.LogCaptureConfigurationForPatchUpdate` was added

* `models.DefaultParameterGroupProperties` was added

* `models.HorizonDbClusterIdentityType` was added

* `models.PrivateLinkResource` was added

* `models.ParameterGroupForPatchUpdate` was added

* `models.Authentications` was added

* `models.ScheduledMaintenanceProperties` was added

* `models.Cluster$Definition` was added

* `models.IdentityAssignment` was added

* `models.ChangeDataCapturePrerequisiteStatus` was added

* `models.MicrosoftEntraAdministrators` was added

* `models.PoolState` was added

* `models.IdentityAssignments` was added

* `models.FirewallRule` was added

* `models.PasswordAuthenticationProperties` was added

* `models.EndpointScope` was added

* `models.PoolComputeModel` was added

* `models.ChangeDataCaptureDestinationType` was added

* `models.ParameterGroupPropertiesForPatchUpdate` was added

* `models.IdentityAssignment$DefinitionStages` was added

* `models.ChangeDataCaptureIdentityAssignmentProperties` was added

* `models.PostgreSqlVersionCapability` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointConnectionResource$DefinitionStages` was added

* `models.ComputeProcessorType` was added

* `models.Capability` was added

* `models.ChangeDataCaptureExtensionState` was added

* `models.HorizonDbClusterDataEncryptionType` was added

* `models.PoolPropertiesForPatchUpdate` was added

* `models.MicrosoftEntraAuthentication` was added

* `models.PostgreSqlVersion` was added

* `models.ProvisionedComputeModelCapability` was added

* `models.MaintenanceEventStatus` was added

* `models.FabricDestinationDetails` was added

* `models.ChangeDataCaptureUsageProfile` was added

* `models.ChangeDataCaptureDestination` was added

* `models.Cluster$DefinitionStages` was added

* `models.IdentityValidationStatus` was added

#### `models.Network` was modified

* `withPublicAccess(models.PublicNetworkAccessState)` was added
* `publicAccess()` was added

#### `models.ParameterProperties` was modified

* `defaultValue()` was added
* `valueSource()` was added

#### `HorizonDbManager` was modified

* `changeDataCaptureCapabilities()` was added
* `firewallRules()` was added
* `identityAssignments()` was added
* `passwordAuthentications()` was added
* `privateLinkResources()` was added
* `parameterGroups()` was added
* `capabilities()` was added
* `nodes()` was added
* `defaultParameterGroups()` was added
* `pools()` was added
* `clusters()` was added
* `microsoftEntraAdministrators()` was added
* `privateEndpointConnections()` was added
* `logFiles()` was added
* `changeDataCaptureDestinations()` was added
* `authentications()` was added
* `maintenanceEvents()` was added
* `logCaptureConfigurations()` was added
* `microsoftEntraAuthentications()` was added
* `endpoints()` was added
* `changeDataCaptureConfigurations()` was added

#### `models.PrivateEndpointConnectionResource` was modified

* `resourceGroupName()` was added
* `refresh(com.azure.core.util.Context)` was added
* `refresh()` was added
* `update()` was added

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
