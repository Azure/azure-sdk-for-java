# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.5 (2022-05-13)

- We’re retiring the Azure Video Analyzer preview service; you're advised to transition your applications off of Video Analyzer by 01 December 2022. This SDK is no longer maintained and won’t work after the service is retired. To learn how to transition off, please refer to: [Transition from Azure Video Analyzer](https://aka.ms/azsdk/videoanalyzer/transitionoffguidance)

- Azure Resource Manager Video Analyzer client library for Java. This package contains Microsoft Azure SDK for Video Analyzer Management SDK. Azure Video Analyzer provides a platform for you to build intelligent video applications that span the edge and the cloud. Package tag package-preview-2021-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.VideoAnalyzersUpdateHeaders` was removed

* `models.VideoAnalyzersCreateOrUpdateHeaders` was removed

* `models.VideoAnalyzersUpdateResponse` was removed

* `models.VideoAnalyzersCreateOrUpdateResponse` was removed

### Features Added

#### `models.PipelineJob` was modified

* `resourceGroupName()` was added

#### `models.AccessPolicyEntity` was modified

* `resourceGroupName()` was added

#### `VideoAnalyzerManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.LivePipeline` was modified

* `resourceGroupName()` was added

#### `models.VideoAnalyzer` was modified

* `resourceGroupName()` was added

#### `VideoAnalyzerManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.VideoEntity` was modified

* `resourceGroupName()` was added

#### `models.EdgeModuleEntity` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `models.PipelineTopology` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.4 (2021-10-27)

- Azure Resource Manager Video Analyzer client library for Java. This package contains Microsoft Azure SDK for Video Analyzer Management SDK. Azure Video Analyzer provides a platform for you to build intelligent video applications that span the edge and the cloud. Package tag package-preview-2021-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.VideoEntity$Update` was modified

* `withMediaInfo(models.VideoMediaInfo)` was added

#### `models.VideoEntity$Definition` was modified

* `withMediaInfo(models.VideoMediaInfo)` was added

#### `models.VideoMediaInfo` was modified

* `withSegmentLength(java.lang.String)` was added

## 1.0.0-beta.3 (2021-10-18)

- Azure Resource Manager Video Analyzer client library for Java. This package contains Microsoft Azure SDK for Video Analyzer Management SDK. Azure Video Analyzer provides a platform for you to build intelligent video applications that span the edge and the cloud. Package tag package-preview-2021-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.VideoStreaming` was removed

* `models.SyncStorageKeysInput` was removed

* `models.VideoAnalyzerProperties` was removed

* `models.VideoAnalyzerPropertiesUpdate` was removed

* `models.VideoStreamingToken` was removed

#### `models.VideoAnalyzers` was modified

* `syncStorageKeysWithResponse(java.lang.String,java.lang.String,models.SyncStorageKeysInput,com.azure.core.util.Context)` was removed
* `syncStorageKeys(java.lang.String,java.lang.String,models.SyncStorageKeysInput)` was removed

#### `models.EdgeModules` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.VideoAnalyzer` was modified

* `syncStorageKeys(models.SyncStorageKeysInput)` was removed
* `systemData()` was removed
* `syncStorageKeysWithResponse(models.SyncStorageKeysInput,com.azure.core.util.Context)` was removed

#### `models.VideoEntity` was modified

* `listStreamingTokenWithResponse(com.azure.core.util.Context)` was removed
* `streaming()` was removed
* `listStreamingToken()` was removed

#### `models.Videos` was modified

* `listStreamingTokenWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listStreamingToken(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.VideoFlags` was modified

* `withIsRecording(boolean)` was removed
* `isRecording()` was removed

#### `models.EdgeModuleEntity` was modified

* `systemData()` was removed

#### `models.UserAssignedManagedIdentity` was modified

* `java.lang.String clientId()` -> `java.util.UUID clientId()`
* `java.lang.String principalId()` -> `java.util.UUID principalId()`

### Features Added

* `models.PipelineJobState` was added

* `models.ProcessorNodeBase` was added

* `models.ParameterDeclaration` was added

* `models.VideoContentToken` was added

* `models.PipelineJob` was added

* `models.VideoSequenceAbsoluteTimeMarkers` was added

* `models.EncoderPresetBase` was added

* `models.PipelineJobCollection` was added

* `models.PrivateEndpointConnectionsCreateOrUpdateResponse` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.LivePipelineOperationStatuses` was added

* `models.PipelineJobUpdate` was added

* `models.VideoAnalyzerOperationResults` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.IotHub` was added

* `models.UnsecuredEndpoint` was added

* `models.NetworkAccessControl` was added

* `models.PrivateLinkResourceListResult` was added

* `models.GroupLevelAccessControl` was added

* `models.PipelineTopology$UpdateStages` was added

* `models.PipelineTopologyUpdate` was added

* `models.EncoderProcessor` was added

* `models.TimeSequenceBase` was added

* `models.PipelineJob$Definition` was added

* `models.CredentialsBase` was added

* `models.LivePipelineUpdate` was added

* `models.EndpointBase` was added

* `models.PipelineJob$DefinitionStages` was added

* `models.VideoPublishingOptions` was added

* `models.PipelineTopologyCollection` was added

* `models.CertificateSource` was added

* `models.PrivateLinkResource` was added

* `models.VideoCreationProperties` was added

* `models.EncoderSystemPreset` was added

* `models.PublicNetworkAccess` was added

* `models.VideoEncoderH264` was added

* `models.PrivateEndpoint` was added

* `models.VideoAnalyzersUpdateHeaders` was added

* `models.VideoAnalyzerOperationStatuses` was added

* `models.PemCertificateList` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.RtspSource` was added

* `models.VideoAnalyzersCreateOrUpdateHeaders` was added

* `models.PipelineJobError` was added

* `models.PipelineJob$UpdateStages` was added

* `models.VideoEncoderBase` was added

* `models.LivePipeline` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.SkuTier` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PipelineTopology$DefinitionStages` was added

* `models.VideoContentUrls` was added

* `models.PipelineTopology$Update` was added

* `models.TunnelBase` was added

* `models.PipelineTopology$Definition` was added

* `models.VideoPreviewImageUrls` was added

* `models.NodeInput` was added

* `models.OperationStatuses` was added

* `models.LivePipelineCollection` was added

* `models.Sku` was added

* `models.PipelineJob$Update` was added

* `models.SinkNodeBase` was added

* `models.EncoderCustomPreset` was added

* `models.VideoArchival` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.SkuName` was added

* `models.LivePipeline$DefinitionStages` was added

* `models.ParameterType` was added

* `models.LivePipeline$Update` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.Kind` was added

* `models.LivePipelines` was added

* `models.PipelineTopologies` was added

* `models.PrivateLinkResources` was added

* `models.PipelineJobOperationStatus` was added

* `models.VideoSink` was added

* `models.LivePipeline$UpdateStages` was added

* `models.NodeBase` was added

* `models.VideoSource` was added

* `models.LivePipelineOperationStatus` was added

* `models.VideoAnalyzersUpdateResponse` was added

* `models.VideoScale` was added

* `models.TlsEndpoint` was added

* `models.VideoAnalyzersCreateOrUpdateResponse` was added

* `models.UsernamePasswordCredentials` was added

* `models.RtspTransport` was added

* `models.OperationResults` was added

* `models.PrivateEndpointConnections` was added

* `models.SourceNodeBase` was added

* `models.SecureIotDeviceRemoteTunnel` was added

* `models.PipelineJobs` was added

* `models.EncoderSystemPresetType` was added

* `models.LivePipeline$Definition` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.TlsValidationOptions` was added

* `models.VideoScaleMode` was added

* `models.PrivateEndpointConnectionsCreateOrUpdateHeaders` was added

* `models.AudioEncoderAac` was added

* `models.ProvisioningState` was added

* `models.AudioEncoderBase` was added

* `models.PipelineTopology` was added

* `models.VideoAnalyzerOperationStatus` was added

* `models.VideoAnalyzerPrivateEndpointConnectionOperationStatus` was added

* `models.PipelineJobOperationStatuses` was added

* `models.LivePipelineState` was added

* `models.ParameterDefinition` was added

#### `VideoAnalyzerManager` was modified

* `livePipelineOperationStatuses()` was added
* `operationResults()` was added
* `videoAnalyzerOperationStatuses()` was added
* `pipelineTopologies()` was added
* `videoAnalyzerOperationResults()` was added
* `pipelineJobOperationStatuses()` was added
* `livePipelines()` was added
* `privateLinkResources()` was added
* `privateEndpointConnections()` was added
* `operationStatuses()` was added
* `pipelineJobs()` was added

#### `models.VideoEntity$Update` was modified

* `withArchival(models.VideoArchival)` was added

#### `models.VideoEntity$Definition` was modified

* `withArchival(models.VideoArchival)` was added

#### `models.EdgeModules` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.VideoAnalyzer` was modified

* `provisioningState()` was added
* `iotHubs()` was added
* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added
* `networkAccessControl()` was added

#### `VideoAnalyzerManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.VideoEntity` was modified

* `contentUrls()` was added
* `listContentToken()` was added
* `archival()` was added
* `listContentTokenWithResponse(com.azure.core.util.Context)` was added

#### `models.Videos` was modified

* `listContentToken(java.lang.String,java.lang.String,java.lang.String)` was added
* `listContentTokenWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VideoFlags` was modified

* `isInUse()` was added
* `withIsInUse(boolean)` was added

#### `models.VideoAnalyzerUpdate` was modified

* `networkAccessControl()` was added
* `withIotHubs(java.util.List)` was added
* `privateEndpointConnections()` was added
* `withNetworkAccessControl(models.NetworkAccessControl)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `publicNetworkAccess()` was added
* `iotHubs()` was added
* `provisioningState()` was added

#### `models.VideoAnalyzer$Definition` was modified

* `withIotHubs(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withNetworkAccessControl(models.NetworkAccessControl)` was added

#### `models.VideoAnalyzer$Update` was modified

* `withNetworkAccessControl(models.NetworkAccessControl)` was added
* `withIotHubs(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

## 1.0.0-beta.2 (2021-05-25)

- Azure Resource Manager Video Analyzer client library for Java. This package contains Microsoft Azure SDK for Video Analyzer Management SDK. Azure Video Analyzer provides a platform for you to build intelligent video applications that span the edge and the cloud. Package tag package-2021-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2021-04-30)

- Azure Resource Manager VideoAnalyzer client library for Java. This package contains Microsoft Azure SDK for VideoAnalyzer Management SDK. Azure Video Analyzer ARM Client. Package tag package-2021-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
