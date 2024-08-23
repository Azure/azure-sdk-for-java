# Release History

## 1.1.0 (2024-08-22)

- Azure Resource Manager Machine Learning client library for Java. This package contains Microsoft Azure SDK for Machine Learning Management SDK. These APIs allow end users to operate on Azure Machine Learning Workspace resources. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AmlOperation` was removed

* `models.AmlOperationListResult` was removed

* `models.AmlOperationDisplay` was removed

#### `models.CodeVersions` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ComputeStartStopSchedule` was modified

* `models.RecurrenceTrigger recurrence()` -> `models.Recurrence recurrence()`
* `models.TriggerType triggerType()` -> `models.ComputeTriggerType triggerType()`
* `withRecurrence(models.RecurrenceTrigger)` was removed
* `withTriggerType(models.TriggerType)` was removed
* `models.CronTrigger cron()` -> `models.Cron cron()`
* `withCron(models.CronTrigger)` was removed

#### `models.Workspaces` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PrivateEndpoint` was modified

* `subnetArmId()` was removed

#### `models.Jobs` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListViewType,com.azure.core.util.Context)` was removed

### Features Added

* `models.SystemCreatedStorageAccount` was added

* `models.Registry$Definition` was added

* `models.NodesValueType` was added

* `models.MonitoringFeatureFilterType` was added

* `models.DataAvailabilityStatus` was added

* `models.FeaturesetVersionBackfillResponse` was added

* `models.LakeHouseArtifact` was added

* `models.OperationListResult` was added

* `models.ServerlessEndpoint$Update` was added

* `models.NumericalDataDriftMetric` was added

* `models.ServerlessEndpoint` was added

* `models.NumericalPredictionDriftMetric` was added

* `models.WorkspaceConnectionAccessKey` was added

* `models.DestinationAsset` was added

* `models.ManagedIdentityCredential` was added

* `models.ServerlessComputeSettings` was added

* `models.FeaturestoreEntityVersions` was added

* `models.MonitoringFeatureFilterBase` was added

* `models.OutboundRuleBasicResource$Update` was added

* `models.DataDriftMonitoringSignal` was added

* `models.MonitorNotificationSettings` was added

* `models.WorkspaceHubConfig` was added

* `models.FeaturesetVersionProperties` was added

* `models.MaterializationComputeResource` was added

* `models.FeaturestoreEntityVersion$DefinitionStages` was added

* `models.ManagedComputeIdentity` was added

* `models.AcrDetails` was added

* `models.CategoricalPredictionDriftMetric` was added

* `models.MarketplaceSubscription` was added

* `models.TmpfsOptions` was added

* `models.DockerCredential` was added

* `models.Registry` was added

* `models.CategoricalPredictionDriftMetricThreshold` was added

* `models.FeaturesetContainer$Definition` was added

* `models.CustomMetricThreshold` was added

* `models.MarketplaceSubscriptionProperties` was added

* `models.SparkJobScalaEntry` was added

* `models.FeaturesetVersion$Definition` was added

* `models.FeatureDataType` was added

* `models.MarketplaceSubscriptions` was added

* `models.SparkJobEntry` was added

* `models.AssetProvisioningState` was added

* `models.EncryptionUpdateProperties` was added

* `models.CategoricalDataDriftMetric` was added

* `models.ServerlessEndpointTrackedResourceArmPaginatedResult` was added

* `models.Image` was added

* `models.MonitorComputeConfigurationBase` was added

* `models.MarketplaceSubscription$Definition` was added

* `models.FeaturestoreEntityContainerResourceArmPaginatedResult` was added

* `models.WorkspaceConnectionApiKey` was added

* `models.Collection` was added

* `models.PendingUploadCredentialType` was added

* `models.OperationDisplay` was added

* `models.PrivateEndpointResource` was added

* `models.SystemCreatedAcrAccount` was added

* `models.MonitorDefinition` was added

* `models.RegistryComponentVersions` was added

* `models.MarketplacePlan` was added

* `models.ManagedNetworkSettingsRules` was added

* `models.OneLakeDatastore` was added

* `models.ServerlessEndpoint$DefinitionStages` was added

* `models.FeatureImportanceSettings` was added

* `models.FeatureResourceArmPaginatedResult` was added

* `models.FeaturesetSpecification` was added

* `models.ManagedNetworkProvisions` was added

* `models.AzureDevOpsWebhook` was added

* `models.MonitoringSignalType` was added

* `models.EnvironmentVariableType` was added

* `models.DataDriftMetricThresholdBase` was added

* `models.MonitoringSignalBase` was added

* `models.Docker` was added

* `models.NumericalPredictionDriftMetricThreshold` was added

* `models.ArmResourceId` was added

* `models.RegistryEnvironmentContainers` was added

* `models.ServicePrincipalAuthTypeWorkspaceConnectionProperties` was added

* `models.Origin` was added

* `models.PrivateEndpointOutboundRule` was added

* `models.MarketplaceSubscriptionResourceArmPaginatedResult` was added

* `models.DataCollectionMode` was added

* `models.Nodes` was added

* `models.BindOptions` was added

* `models.RegistryCodeContainers` was added

* `models.AllFeatures` was added

* `models.RegistryEnvironmentVersions` was added

* `models.DataReferenceCredential` was added

* `models.PredictionDriftMonitoringSignal` was added

* `models.OAuth2AuthTypeWorkspaceConnectionProperties` was added

* `models.FeatureProperties` was added

* `models.FeaturesetVersions` was added

* `models.FeaturesetVersion$Update` was added

* `models.VolumeDefinitionType` was added

* `models.Operation` was added

* `models.DataQualityMonitoringSignal` was added

* `models.RegistryPartialManagedServiceIdentity` was added

* `models.TopNFeaturesByAttribution` was added

* `models.Registries` was added

* `models.FeaturestoreEntityVersion$Definition` was added

* `models.MarketplaceSubscription$DefinitionStages` was added

* `models.Feature` was added

* `models.RuleAction` was added

* `models.ContentSafetyStatus` was added

* `models.ConnectionGroup` was added

* `models.RegistryPrivateEndpointConnection` was added

* `models.GetBlobReferenceSasRequestDto` was added

* `models.BatchDeploymentConfigurationType` was added

* `models.StorageAccountDetails` was added

* `models.RegistryRegionArmDetails` was added

* `models.EncryptionKeyVaultUpdateProperties` was added

* `models.FeaturesetVersion$DefinitionStages` was added

* `models.MonitorComputeType` was added

* `models.StaticInputData` was added

* `models.SparkJobEntryType` was added

* `models.UserCreatedAcrAccount` was added

* `models.ServerlessEndpoints` was added

* `models.VolumeOptions` was added

* `models.FeaturestoreEntityVersionResourceArmPaginatedResult` was added

* `models.RegistryCodeVersions` was added

* `models.FeatureImportanceMode` was added

* `models.MarketplaceSubscriptionProvisioningState` was added

* `models.MonitoringTarget` was added

* `models.QueueSettings` was added

* `models.RegistryDataContainers` was added

* `models.FeatureAttributionMetricThreshold` was added

* `models.Protocol` was added

* `models.ActionType` was added

* `models.ComputeRecurrenceSchedule` was added

* `models.Registry$Update` was added

* `models.MarketplaceSubscriptionStatus` was added

* `models.MonitorServerlessSparkCompute` was added

* `models.ApiKeyAuthWorkspaceConnectionProperties` was added

* `models.AnonymousAccessCredential` was added

* `models.ManagedNetworkSettings` was added

* `models.MarketplaceSubscription$Update` was added

* `models.MaterializationSettings` was added

* `models.Recurrence` was added

* `models.Registry$UpdateStages` was added

* `models.OutboundRuleListResult` was added

* `models.WorkspaceConnectionServicePrincipal` was added

* `models.Webhook` was added

* `models.DataReferenceCredentialType` was added

* `models.RegistryDataReferences` was added

* `models.PrivateEndpointDestination` was added

* `models.MarketplaceSubscription$UpdateStages` was added

* `models.OneLakeArtifact` was added

* `models.FeaturestoreEntityContainer$UpdateStages` was added

* `models.ServerlessEndpointProperties` was added

* `models.ImageMetadata` was added

* `models.CustomKeysWorkspaceConnectionProperties` was added

* `models.FeaturestoreEntityContainer` was added

* `models.CustomKeys` was added

* `models.FeatureStoreSettings` was added

* `models.WebhookType` was added

* `models.AccountKeyAuthTypeWorkspaceConnectionProperties` was added

* `models.ManagedNetworkProvisionOptions` was added

* `models.SparkJob` was added

* `models.ServerlessEndpointState` was added

* `models.WorkspacePropertiesUpdateParameters` was added

* `models.ComputeRuntimeDto` was added

* `models.FeatureWindow` was added

* `models.Cron` was added

* `models.RuleCategory` was added

* `models.FeatureSubset` was added

* `models.PendingUploadRequestDto` was added

* `models.RuleStatus` was added

* `models.RegistryComponentContainers` was added

* `models.DataCollector` was added

* `models.WorkspaceConnectionAccountKey` was added

* `models.MonitoringInputDataBase` was added

* `models.Features` was added

* `models.AccessKeyAuthTypeWorkspaceConnectionProperties` was added

* `models.CategoricalDataQualityMetricThreshold` was added

* `models.MaterializationStoreType` was added

* `models.CustomService` was added

* `models.AadAuthTypeWorkspaceConnectionProperties` was added

* `models.BlobReferenceForConsumptionDto` was added

* `models.VolumeDefinition` was added

* `models.ModelTaskType` was added

* `models.FeaturestoreEntityVersion` was added

* `models.FeaturestoreEntityContainer$Update` was added

* `models.ServerlessInferenceEndpoint` was added

* `models.FeaturesetVersionBackfillRequest` was added

* `models.ServerlessEndpoint$Definition` was added

* `models.PendingUploadResponseDto` was added

* `models.RegistryDataVersions` was added

* `models.OutboundRule` was added

* `models.ModelSettings` was added

* `models.OneLakeArtifactType` was added

* `models.Endpoint` was added

* `models.MonitoringThreshold` was added

* `models.MonitoringInputDataType` was added

* `models.RollingInputData` was added

* `models.GetBlobReferenceSasResponseDto` was added

* `models.Registry$DefinitionStages` was added

* `models.FeaturesetVersion$UpdateStages` was added

* `models.FeaturestoreEntityContainers` was added

* `models.ComputeRecurrenceFrequency` was added

* `models.IndexColumn` was added

* `models.CategoricalDataDriftMetricThreshold` was added

* `models.NumericalDataQualityMetricThreshold` was added

* `models.FeaturestoreEntityContainer$DefinitionStages` was added

* `models.FeaturesetContainers` was added

* `models.PredictionDriftMetricThresholdBase` was added

* `models.JobTier` was added

* `models.GetBlobReferenceForConsumptionDto` was added

* `models.FeaturestoreEntityVersion$Update` was added

* `models.MonitorEmailNotificationSettings` was added

* `models.SasCredential` was added

* `models.NotificationSetting` was added

* `models.BatchDeploymentConfiguration` was added

* `models.SasCredentialDto` was added

* `models.ServiceTagOutboundRule` was added

* `models.IsolationMode` was added

* `models.UserCreatedStorageAccount` was added

* `models.MonitoringFeatureDataType` was added

* `models.ServerlessEndpoint$UpdateStages` was added

* `models.OutboundRuleBasicResource$UpdateStages` was added

* `models.EnvironmentVariable` was added

* `models.WorkspaceConnectionOAuth2` was added

* `models.PartialRegistryPartialTrackedResource` was added

* `models.PartialMinimalTrackedResourceWithSkuAndIdentity` was added

* `models.FeaturesetContainer$Update` was added

* `models.RegistryTrackedResourceArmPaginatedResult` was added

* `models.FeaturestoreEntityContainerProperties` was added

* `models.FeaturestoreEntityVersionProperties` was added

* `models.RuleType` was added

* `models.NumericalDataDriftMetricThreshold` was added

* `models.ComputeTriggerType` was added

* `models.EndpointServiceConnectionStatus` was added

* `models.FeaturesetContainer$DefinitionStages` was added

* `models.OutboundRuleBasicResource$Definition` was added

* `models.EmailNotificationEnableType` was added

* `models.ServiceTagDestination` was added

* `models.RequestLogging` was added

* `models.FeaturestoreEntityContainer$Definition` was added

* `models.ManagedNetworkStatus` was added

* `models.FeaturesetContainerProperties` was added

* `models.FeaturesetContainer$UpdateStages` was added

* `models.MonitoringNotificationType` was added

* `models.RollingRateType` was added

* `models.CustomMonitoringSignal` was added

* `models.CreateMonitorAction` was added

* `models.ComputeWeekDay` was added

* `models.DataQualityMetricThresholdBase` was added

* `models.OutboundRuleBasicResource$DefinitionStages` was added

* `models.ServerlessInferenceEndpointAuthMode` was added

* `models.RegistryModelVersions` was added

* `models.SparkResourceConfiguration` was added

* `models.SparkJobPythonEntry` was added

* `models.FeaturesetContainerResourceArmPaginatedResult` was added

* `models.FeaturesetVersionResourceArmPaginatedResult` was added

* `models.MonitorComputeIdentityType` was added

* `models.OutboundRuleBasicResource` was added

* `models.AmlTokenComputeIdentity` was added

* `models.FeatureAttributionDriftMonitoringSignal` was added

* `models.NumericalDataQualityMetric` was added

* `models.FeaturesetVersion` was added

* `models.FeaturesetContainer` was added

* `models.RegistryModelContainers` was added

* `models.FeaturestoreEntityVersion$UpdateStages` was added

* `models.AllNodes` was added

* `models.MonitorComputeIdentityBase` was added

* `models.ContentSafety` was added

* `models.FeatureAttributionMetric` was added

* `models.CategoricalDataQualityMetric` was added

* `models.BatchPipelineComponentDeploymentConfiguration` was added

* `models.PendingUploadType` was added

* `models.ManagedNetworkProvisionStatus` was added

* `models.PendingUploadCredentialDto` was added

* `models.RegistryPrivateLinkServiceConnectionState` was added

* `models.FixedInputData` was added

* `models.ImageType` was added

* `models.AzureDatastore` was added

* `models.FqdnOutboundRule` was added

#### `models.DiagnoseRequestProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NlpVerticalLimitSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoScaleProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NoneDatastoreCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `credentialsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FqdnEndpointsProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EstimatedVMPrice` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScaleSettingsInformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceConnectionPropertiesV2` was modified

* `withExpiryTime(java.time.OffsetDateTime)` was added
* `withSharedUserList(java.util.List)` was added
* `expiryTime()` was added
* `group()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withIsSharedToAll(java.lang.Boolean)` was added
* `authType()` was added
* `metadata()` was added
* `isSharedToAll()` was added
* `sharedUserList()` was added
* `createdByWorkspaceArmId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withMetadata(java.util.Map)` was added

#### `models.BanditPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `policyType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobResourceConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageClassificationMultilabel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added

#### `models.BuildContext` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CronTrigger` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `triggerType()` was added

#### `models.MLFlowModelJobOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `jobOutputType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBlobDatastore` was modified

* `datastoreType()` was added
* `subscriptionId()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withResourceGroup(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withSubscriptionId(java.lang.String)` was added
* `isDefault()` was added
* `resourceGroup()` was added

#### `models.AssetContainer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodeVersions` was modified

* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset)` was added
* `createOrGetStartPendingUpload(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PendingUploadRequestDto)` was added
* `createOrGetStartPendingUploadWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PendingUploadRequestDto,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset,com.azure.core.util.Context)` was added

#### `models.KubernetesProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageModelSettingsObjectDetection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabricksComputeSecretsProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmlToken` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `identityType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabricksSchema` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SweepJobLimits` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `jobLimitsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SynapseSpark` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `provisioningState()` was added
* `modifiedOn()` was added
* `provisioningErrors()` was added
* `isAttachedCompute()` was added
* `createdOn()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `computeType()` was added

#### `models.ScheduleProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceSchema` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FqdnEndpoints` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DefaultScaleSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `scaleType()` was added

#### `models.ImageSweepSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SystemService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OnlineRequestSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataVersionBaseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `dataType()` was added

#### `models.DataVersionBase` was modified

* `publish(models.DestinationAsset)` was added
* `publish(models.DestinationAsset,com.azure.core.util.Context)` was added

#### `models.CodeConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageInstanceSegmentation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `taskType()` was added

#### `models.ForecastHorizon` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `mode()` was added

#### `models.ComponentVersion` was modified

* `publish(models.DestinationAsset,com.azure.core.util.Context)` was added
* `publish(models.DestinationAsset)` was added

#### `models.BatchDeploymentProperties` was modified

* `deploymentConfiguration()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDeploymentConfiguration(models.BatchDeploymentConfiguration)` was added

#### `models.Aks` was modified

* `provisioningErrors()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `computeType()` was added
* `createdOn()` was added
* `isAttachedCompute()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `provisioningState()` was added
* `modifiedOn()` was added

#### `models.PartialMinimalTrackedResourceWithSku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TargetRollingWindowSize` was modified

* `mode()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataLakeAnalytics` was modified

* `isAttachedCompute()` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `computeType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `createdOn()` was added
* `modifiedOn()` was added
* `provisioningErrors()` was added

#### `models.WorkspaceConnectionPropertiesV2BasicResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoTargetRollingWindowSize` was modified

* `mode()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataVersionBaseResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClassificationTrainingSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Workspace$Update` was modified

* `withManagedNetwork(fluent.models.ManagedNetworkSettingsInner)` was added
* `withServerlessComputeSettings(models.ServerlessComputeSettings)` was added
* `withV1LegacyMode(java.lang.Boolean)` was added
* `withEncryption(models.EncryptionUpdateProperties)` was added
* `withFeatureStoreSettings(models.FeatureStoreSettings)` was added
* `withEnableDataIsolation(java.lang.Boolean)` was added

#### `models.PartialBatchDeployment` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataContainerResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeStartStopSchedule` was modified

* `withTriggerType(models.ComputeTriggerType)` was added
* `withRecurrence(models.Recurrence)` was added
* `withCron(models.Cron)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceName` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineSecrets` was modified

* `computeType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoPauseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceCreatedBy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModelVersionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withStage(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added
* `stage()` was added

#### `models.BatchEndpointProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `scoringUri()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `swaggerUri()` was added

#### `models.OnlineDeploymentProperties` was modified

* `endpointComputeType()` was added
* `dataCollector()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDataCollector(models.DataCollector)` was added

#### `models.TruncationSelectionPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `policyType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageClassificationBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BatchRetrySettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineSshCredentials` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceConnectionUsernamePassword` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withSecurityToken(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `securityToken()` was added

#### `models.WorkspaceConnectionPersonalAccessToken` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaBaseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CodeContainerProperties` was modified

* `latestVersion()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `nextVersion()` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmlComputeNodeInformation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModelContainerProperties` was modified

* `latestVersion()` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `nextVersion()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TableVerticalFeaturizationSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EarlyTerminationPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `policyType()` was added

#### `models.AutoNCrossValidations` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `mode()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InferenceContainerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobLimits` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `jobLimitsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TargetLags` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `mode()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BatchEndpointDefaults` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageModelDistributionSettingsClassification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptsToExecute` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDataLakeGen1Datastore` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `isDefault()` was added
* `resourceGroup()` was added
* `withSubscriptionId(java.lang.String)` was added
* `datastoreType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `subscriptionId()` was added
* `withResourceGroup(java.lang.String)` was added

#### `models.DatastoreSecrets` was modified

* `secretsType()` was added

#### `models.Workspaces` was modified

* `delete(java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.Kubernetes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `computeType()` was added
* `provisioningErrors()` was added
* `modifiedOn()` was added
* `isAttachedCompute()` was added
* `createdOn()` was added

#### `models.ManagedOnlineDeployment` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `endpointComputeType()` was added
* `provisioningState()` was added
* `withDataCollector(models.DataCollector)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataLakeAnalyticsSchema` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NodeStateCounts` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentVersionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withStage(java.lang.String)` was added
* `stage()` was added

#### `models.ModelVersionResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssignedUser` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentContainerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `latestVersion()` was added
* `nextVersion()` was added
* `provisioningState()` was added

#### `models.AksSchemaProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetJobOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomModelJobInput` was modified

* `jobInputType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CodeVersionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added

#### `models.AmlComputeNodesInformation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Objective` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UriFolderDataVersion` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `dataType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Workspace$Definition` was modified

* `withServerlessComputeSettings(models.ServerlessComputeSettings)` was added
* `withFeatureStoreSettings(models.FeatureStoreSettings)` was added
* `withManagedNetwork(fluent.models.ManagedNetworkSettingsInner)` was added
* `withHubResourceId(java.lang.String)` was added
* `withEnableDataIsolation(java.lang.Boolean)` was added
* `withWorkspaceHubConfig(models.WorkspaceHubConfig)` was added
* `withAssociatedWorkspaces(java.util.List)` was added
* `withKind(java.lang.String)` was added

#### `models.DataPathAssetReference` was modified

* `referenceType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SynapseSparkProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StackEnsembleSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceDataDisk` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ListWorkspaceQuotas` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TargetUtilizationScaleSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `scaleType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceApplication` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResource` was modified

* `name()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PipelineJob` was modified

* `withNotificationSetting(models.NotificationSetting)` was added
* `status()` was added
* `jobType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Password` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ColumnTransformer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrainingSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RecurrenceSchedule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PartialMinimalTrackedResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceConnectionManagedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomTargetLags` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `mode()` was added

#### `models.DistributionConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `distributionType()` was added

#### `models.PaginatedComputeResourcesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Seasonality` was modified

* `mode()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HDInsightProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Classification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added

#### `models.VirtualMachine` was modified

* `isAttachedCompute()` was added
* `provisioningState()` was added
* `modifiedOn()` was added
* `createdOn()` was added
* `computeType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningErrors()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegressionTrainingSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateWorkspaceQuotas` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmlCompute` was modified

* `isAttachedCompute()` was added
* `computeType()` was added
* `modifiedOn()` was added
* `provisioningErrors()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `createdOn()` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDataLakeGen2Datastore` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `isDefault()` was added
* `datastoreType()` was added
* `subscriptionId()` was added
* `withSubscriptionId(java.lang.String)` was added
* `resourceGroup()` was added
* `withResourceGroup(java.lang.String)` was added

#### `models.ImageModelSettingsClassification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatastoreResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountKeyDatastoreCredentials` was modified

* `credentialsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerResourceRequirements` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeInstanceConnectivityEndpoints` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Databricks` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningErrors()` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `modifiedOn()` was added
* `createdOn()` was added
* `computeType()` was added
* `isAttachedCompute()` was added

#### `models.ListAmlUserFeatureResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UriFileDataVersion` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `dataType()` was added

#### `models.UriFileJobInput` was modified

* `jobInputType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeInstance` was modified

* `provisioningErrors()` was added
* `provisioningState()` was added
* `modifiedOn()` was added
* `isAttachedCompute()` was added
* `createdOn()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `computeType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuCapacity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceConnections` was modified

* `listSecretsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSecrets(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ComponentContainerProperties` was modified

* `latestVersion()` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `nextVersion()` was added

#### `models.Workspace` was modified

* `serverlessComputeSettings()` was added
* `featureStoreSettings()` was added
* `workspaceHubConfig()` was added
* `enableDataIsolation()` was added
* `hubResourceId()` was added
* `kind()` was added
* `managedNetwork()` was added
* `associatedWorkspaces()` was added

#### `models.ComponentContainerResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeResourceSchema` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmlComputeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Route` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceContainer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionKeyVaultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NlpVertical` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UriFileJobOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `jobOutputType()` was added

#### `models.LiteralJobInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `jobInputType()` was added

#### `models.CodeVersion` was modified

* `publish(models.DestinationAsset,com.azure.core.util.Context)` was added
* `createOrGetStartPendingUpload(models.PendingUploadRequestDto)` was added
* `createOrGetStartPendingUploadWithResponse(models.PendingUploadRequestDto,com.azure.core.util.Context)` was added
* `publish(models.DestinationAsset)` was added

#### `models.TableVerticalLimitSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UriFolderJobInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `jobInputType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrialComponent` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeSecrets` was modified

* `computeType()` was added

#### `models.SkuResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MedianStoppingPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `policyType()` was added

#### `models.NoneAuthTypeWorkspaceConnectionProperties` was modified

* `withMetadata(java.util.Map)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `group()` was added
* `authType()` was added
* `withIsSharedToAll(java.lang.Boolean)` was added
* `withSharedUserList(java.util.List)` was added
* `createdByWorkspaceArmId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withExpiryTime(java.time.OffsetDateTime)` was added

#### `models.NotebookPreparationError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScaleSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobBaseResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PartialManagedServiceIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Regression` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `MachineLearningManager` was modified

* `registryModelVersions()` was added
* `registryDataVersions()` was added
* `serverlessEndpoints()` was added
* `registryCodeContainers()` was added
* `featuresetContainers()` was added
* `managedNetworkProvisions()` was added
* `featurestoreEntityVersions()` was added
* `registryComponentContainers()` was added
* `registryComponentVersions()` was added
* `marketplaceSubscriptions()` was added
* `registryDataReferences()` was added
* `registryModelContainers()` was added
* `managedNetworkSettingsRules()` was added
* `registries()` was added
* `registryEnvironmentVersions()` was added
* `registryCodeVersions()` was added
* `registryDataContainers()` was added
* `features()` was added
* `featuresetVersions()` was added
* `featurestoreEntityContainers()` was added
* `registryEnvironmentContainers()` was added

#### `models.CodeContainerResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduleActionBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `actionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ListUsagesResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MLTableData` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `dataType()` was added

#### `models.Forecasting` was modified

* `taskType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CertificateDatastoreSecrets` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `secretsType()` was added

#### `models.CertificateDatastoreCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `credentialsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineSchemaProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UsernamePasswordAuthTypeWorkspaceConnectionProperties` was modified

* `withIsSharedToAll(java.lang.Boolean)` was added
* `withExpiryTime(java.time.OffsetDateTime)` was added
* `withMetadata(java.util.Map)` was added
* `withSharedUserList(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `createdByWorkspaceArmId()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `group()` was added
* `authType()` was added

#### `models.ServiceManagedResourcesSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GridSamplingAlgorithm` was modified

* `samplingAlgorithmType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComponentVersionResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IdentityForCmk` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureFileDatastore` was modified

* `withSubscriptionId(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withResourceGroup(java.lang.String)` was added
* `isDefault()` was added
* `subscriptionId()` was added
* `datastoreType()` was added
* `resourceGroup()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceUpdateParameters` was modified

* `serverlessComputeSettings()` was added
* `encryption()` was added
* `withManagedNetwork(fluent.models.ManagedNetworkSettingsInner)` was added
* `withV1LegacyMode(java.lang.Boolean)` was added
* `v1LegacyMode()` was added
* `featureStoreSettings()` was added
* `managedNetwork()` was added
* `withEnableDataIsolation(java.lang.Boolean)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withServerlessComputeSettings(models.ServerlessComputeSettings)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withFeatureStoreSettings(models.FeatureStoreSettings)` was added
* `enableDataIsolation()` was added
* `withEncryption(models.EncryptionUpdateProperties)` was added

#### `models.NlpVerticalFeaturizationSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineImage` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UriFolderJobOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `jobOutputType()` was added

#### `models.Compute` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `computeType()` was added

#### `models.OutputPathAssetReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `referenceType()` was added

#### `models.EnvironmentVersionResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerBase` was modified

* `triggerType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InstanceTypeSchemaResources` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScriptReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MLTableJobOutput` was modified

* `jobOutputType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoMLVertical` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Mpi` was modified

* `distributionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnoseResponseResultValue` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SasDatastoreCredentials` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `credentialsType()` was added

#### `models.AksNetworkingConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineSecretsSchema` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchEndpointTrackedResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabricksComputeSecrets` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `computeType()` was added

#### `models.OnlineDeploymentTrackedResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageModelDistributionSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageVertical` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnoseResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TritonModelJobOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `jobOutputType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceConnectionSharedAccessSignature` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatastoreCredentials` was modified

* `credentialsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KubernetesSchema` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ProbeSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedServiceIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceConnectionPropertiesV2BasicResource` was modified

* `listSecretsWithResponse(com.azure.core.util.Context)` was added
* `listSecrets()` was added

#### `models.EstimatedVMPrices` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TextClassificationMultilabel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added

#### `models.ComponentVersions` was modified

* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset)` was added
* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset,com.azure.core.util.Context)` was added

#### `models.CosmosDbSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IdAssetReference` was modified

* `referenceType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AksComputeSecrets` was modified

* `computeType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PersonalComputeInstanceSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceProperties` was modified

* `featureStoreSettings()` was added
* `managedNetwork()` was added
* `hubResourceId()` was added
* `associatedWorkspaces()` was added
* `serverlessComputeSettings()` was added
* `enableDataIsolation()` was added
* `workspaceHubConfig()` was added

#### `models.CommandJobLimits` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `jobLimitsType()` was added

#### `models.VirtualMachineSize` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageClassification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added

#### `models.DatabricksProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SasDatastoreSecrets` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `secretsType()` was added

#### `models.SharedPrivateLinkResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AksSchema` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TritonModelJobInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `jobInputType()` was added

#### `models.AssetJobInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerResourceSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomModelJobOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `jobOutputType()` was added

#### `models.FlavorData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatastoreProperties` was modified

* `datastoreType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BayesianSamplingAlgorithm` was modified

* `samplingAlgorithmType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Jobs` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListViewType,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DeploymentResourceConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TableVertical` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomNCrossValidations` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `mode()` was added

#### `models.DataVersions` was modified

* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset,com.azure.core.util.Context)` was added
* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset)` was added

#### `models.ComputeInstanceEnvironmentInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuSetting` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceSshSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegenerateEndpointKeysRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TensorFlow` was modified

* `distributionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SasAuthTypeWorkspaceConnectionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withExpiryTime(java.time.OffsetDateTime)` was added
* `withSharedUserList(java.util.List)` was added
* `withMetadata(java.util.Map)` was added
* `group()` was added
* `withIsSharedToAll(java.lang.Boolean)` was added
* `createdByWorkspaceArmId()` was added
* `authType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageObjectDetectionBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeploymentLogsRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TextNer` was modified

* `taskType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FqdnEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentVersions` was modified

* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset,com.azure.core.util.Context)` was added
* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset)` was added

#### `models.MLTableJobInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `jobInputType()` was added

#### `models.AutoForecastHorizon` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `mode()` was added

#### `models.WorkspaceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommandJob` was modified

* `withNotificationSetting(models.NotificationSetting)` was added
* `withQueueSettings(models.QueueSettings)` was added
* `jobType()` was added
* `queueSettings()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `status()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeInstanceVersion` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IdentityConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `identityType()` was added

#### `models.ManagedIdentityAuthTypeWorkspaceConnectionProperties` was modified

* `withExpiryTime(java.time.OffsetDateTime)` was added
* `createdByWorkspaceArmId()` was added
* `authType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `group()` was added
* `withSharedUserList(java.util.List)` was added
* `withIsSharedToAll(java.lang.Boolean)` was added
* `withMetadata(java.util.Map)` was added

#### `models.RegistryListCredentialsResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FeaturizationSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServicePrincipalDatastoreCredentials` was modified

* `credentialsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModelContainerResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SslConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnoseWorkspaceParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EndpointDeploymentPropertiesBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFactory` was modified

* `computeType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `createdOn()` was added
* `provisioningErrors()` was added
* `isAttachedCompute()` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `modifiedOn()` was added

#### `models.CustomTargetRollingWindowSize` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `mode()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageObjectDetection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `taskType()` was added

#### `models.ModelVersions` was modified

* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset,com.azure.core.util.Context)` was added
* `publish(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DestinationAsset)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InstanceTypeSchema` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmlComputeSchema` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `identityType()` was added

#### `models.ImageModelSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAccountCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `jobOutputType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodeVersionResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HDInsightSchema` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EndpointScheduleAction` was modified

* `actionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `identityType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SetupScripts` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MLFlowModelJobInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `jobInputType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomForecastHorizon` was modified

* `mode()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AssetReferenceBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `referenceType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceProperties` was modified

* `osImageMetadata()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withCustomServices(java.util.List)` was added
* `withSchedules(models.ComputeSchedules)` was added
* `customServices()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModelVersion` was modified

* `publish(models.DestinationAsset)` was added
* `publish(models.DestinationAsset,com.azure.core.util.Context)` was added

#### `models.UsageName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BatchDeploymentTrackedResourceArmPaginatedResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SweepJob` was modified

* `withNotificationSetting(models.NotificationSetting)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `status()` was added
* `queueSettings()` was added
* `jobType()` was added
* `withQueueSettings(models.QueueSettings)` was added

#### `models.AksComputeSecretsProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutoSeasonality` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `mode()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NCrossValidations` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `mode()` was added

#### `models.OnlineEndpointTrackedResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageModelDistributionSettingsObjectDetection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeInstanceDataMount` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ForecastingTrainingSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TextClassification` was modified

* `taskType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PartialSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoTargetLags` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `mode()` was added

#### `models.ImageLimitSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineSchema` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OnlineScaleSettings` was modified

* `scaleType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataContainerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `latestVersion()` was added
* `nextVersion()` was added

#### `models.PartialMinimalTrackedResourceWithIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoMLJob` was modified

* `jobType()` was added
* `status()` was added
* `withQueueSettings(models.QueueSettings)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `queueSettings()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withNotificationSetting(models.NotificationSetting)` was added

#### `models.RandomSamplingAlgorithm` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `samplingAlgorithmType()` was added

#### `models.JobInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `jobInputType()` was added

#### `models.JobScheduleAction` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `actionType()` was added

#### `models.JobService` was modified

* `withNodes(models.Nodes)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `nodes()` was added

#### `models.HDInsight` was modified

* `modifiedOn()` was added
* `provisioningState()` was added
* `isAttachedCompute()` was added
* `createdOn()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `computeType()` was added
* `provisioningErrors()` was added

#### `models.KubernetesOnlineDeployment` was modified

* `endpointComputeType()` was added
* `withDataCollector(models.DataCollector)` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AccountKeyDatastoreSecrets` was modified

* `secretsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServicePrincipalDatastoreSecrets` was modified

* `secretsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OnlineEndpointProperties` was modified

* `mirrorTraffic()` was added
* `withMirrorTraffic(java.util.Map)` was added
* `swaggerUri()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `scoringUri()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomSeasonality` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `mode()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceId` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EnvironmentVersion` was modified

* `publish(models.DestinationAsset,com.azure.core.util.Context)` was added
* `publish(models.DestinationAsset)` was added

#### `models.RecurrenceTrigger` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `triggerType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FqdnEndpointDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeInstanceLastOperation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ForecastingSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentContainerResourceArmPaginatedResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobBaseProperties` was modified

* `notificationSetting()` was added
* `withNotificationSetting(models.NotificationSetting)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `jobType()` was added

#### `models.PyTorch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `distributionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataLakeAnalyticsSchemaProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComponentVersionProperties` was modified

* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SamplingAlgorithm` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `samplingAlgorithmType()` was added

#### `models.PatAuthTypeWorkspaceConnectionProperties` was modified

* `createdByWorkspaceArmId()` was added
* `withMetadata(java.util.Map)` was added
* `group()` was added
* `withExpiryTime(java.time.OffsetDateTime)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withSharedUserList(java.util.List)` was added
* `authType()` was added
* `withIsSharedToAll(java.lang.Boolean)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeSchedules` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0 (2023-01-11)

- Azure Resource Manager Machine Learning client library for Java. This package contains Microsoft Azure SDK for Machine Learning Management SDK. These APIs allow end users to operate on Azure Machine Learning Workspace resources. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.EndpointPropertiesBase` was modified

* `java.net.URL swaggerUri()` -> `java.lang.String swaggerUri()`
* `java.net.URL scoringUri()` -> `java.lang.String scoringUri()`

## 1.0.0-beta.3 (2023-01-10)

- Azure Resource Manager Machine Learning client library for Java. This package contains Microsoft Azure SDK for Machine Learning Management SDK. These APIs allow end users to operate on Azure Machine Learning Workspace resources. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ModelVersionData$Update` was removed

* `models.ModelContainerData$UpdateStages` was removed

* `models.PartialCodeConfiguration` was removed

* `models.PartialBatchEndpointPartialTrackedResource` was removed

* `models.TestDataSettings` was removed

* `models.BatchEndpointDetails` was removed

* `models.ModelContainerData$Update` was removed

* `models.WorkspaceConnection$DefinitionStages` was removed

* `models.ComponentContainerData` was removed

* `models.OnlineDeploymentData$Update` was removed

* `models.DatastoreData$Definition` was removed

* `models.NlpVerticalDataSettings` was removed

* `models.JobBaseData$UpdateStages` was removed

* `models.CodeContainerData` was removed

* `models.JobBaseData` was removed

* `models.PartialAssetReferenceBase` was removed

* `models.BatchDeploymentDetails` was removed

* `models.DatastoreData` was removed

* `models.ModelContainerData$DefinitionStages` was removed

* `models.KerberosPasswordSecrets` was removed

* `models.PartialOutputPathAssetReference` was removed

* `models.OnlineEndpointData$UpdateStages` was removed

* `models.RecurrencePattern` was removed

* `models.EnvironmentVersionData$Update` was removed

* `models.EnvironmentVersionData$Definition` was removed

* `models.CodeContainerData$Update` was removed

* `models.DataContainerData$UpdateStages` was removed

* `models.DatastoreData$UpdateStages` was removed

* `models.EnvironmentVersionDetails` was removed

* `models.EnvironmentContainerData` was removed

* `models.DataContainerDetails` was removed

* `models.PartialBatchEndpoint` was removed

* `models.PartialOnlineDeploymentPartialTrackedResource` was removed

* `models.EnvironmentVersionData` was removed

* `models.JobBaseData$Update` was removed

* `models.EnvironmentContainerData$DefinitionStages` was removed

* `models.ModelVersionData$UpdateStages` was removed

* `models.EnvironmentVersionData$DefinitionStages` was removed

* `models.BatchDeploymentData$DefinitionStages` was removed

* `models.Weekday` was removed

* `models.CodeVersionData$DefinitionStages` was removed

* `models.CodeVersionDetails` was removed

* `models.ImageVerticalDataSettings` was removed

* `models.BatchDeploymentData` was removed

* `models.DataContainerData$DefinitionStages` was removed

* `models.ModelVersionDetails` was removed

* `models.BatchDeploymentData$Definition` was removed

* `models.ComponentVersionData$DefinitionStages` was removed

* `models.DataSettings` was removed

* `models.CodeContainerDetails` was removed

* `models.NlpVerticalValidationDataSettings` was removed

* `models.KerberosKeytabCredentials` was removed

* `models.DataVersionBaseData$UpdateStages` was removed

* `models.PartialOnlineDeployment` was removed

* `models.ModelContainerData` was removed

* `models.OnlineEndpointData$DefinitionStages` was removed

* `models.EnvironmentContainerData$Update` was removed

* `models.DataVersionBaseData$Definition` was removed

* `models.OnlineEndpointDetails` was removed

* `models.OnlineDeploymentData` was removed

* `models.ComponentContainerData$DefinitionStages` was removed

* `models.ComponentContainerDetails` was removed

* `models.JobBaseDetails` was removed

* `models.DataVersionBaseDetails` was removed

* `models.JobBaseData$Definition` was removed

* `models.CodeVersionData$Definition` was removed

* `models.PartialBatchDeploymentPartialTrackedResource` was removed

* `models.WorkspaceConnection$Definition` was removed

* `models.PartialDataPathAssetReference` was removed

* `models.TableVerticalValidationDataSettings` was removed

* `models.CodeVersionData$UpdateStages` was removed

* `models.DatastoreData$DefinitionStages` was removed

* `models.DataVersionBaseData$DefinitionStages` was removed

* `models.OnlineEndpointData$Definition` was removed

* `models.CodeVersionData$Update` was removed

* `models.PartialIdAssetReference` was removed

* `models.ModelContainerData$Definition` was removed

* `models.OnlineDeploymentData$Definition` was removed

* `models.PartialKubernetesOnlineDeployment` was removed

* `models.ComponentVersionDetails` was removed

* `models.ModelContainerDetails` was removed

* `models.TrainingDataSettings` was removed

* `models.KerberosCredentials` was removed

* `models.ComponentContainerData$Update` was removed

* `models.ComponentVersionData$Update` was removed

* `models.OnlineDeploymentData$UpdateStages` was removed

* `models.PartialBatchRetrySettings` was removed

* `models.BatchEndpointData$DefinitionStages` was removed

* `models.CodeContainerData$Definition` was removed

* `models.EnvironmentContainerDetails` was removed

* `models.BatchEndpointData$Definition` was removed

* `models.ComponentContainerData$Definition` was removed

* `models.ModelVersionData$Definition` was removed

* `models.CodeVersionData` was removed

* `models.PartialOnlineEndpoint` was removed

* `models.OnlineEndpointData$Update` was removed

* `models.KerberosPasswordCredentials` was removed

* `models.OnlineEndpointData` was removed

* `models.ComponentVersionData$UpdateStages` was removed

* `models.CodeContainerData$UpdateStages` was removed

* `models.HdfsDatastore` was removed

* `models.ModelType` was removed

* `models.OnlineDeploymentDetails` was removed

* `models.DataVersionBaseData` was removed

* `models.EnvironmentContainerData$Definition` was removed

* `models.ValidationDataSettings` was removed

* `models.BatchDeploymentData$UpdateStages` was removed

* `models.ComponentVersionData$Definition` was removed

* `models.BatchEndpointData` was removed

* `models.PartialManagedOnlineDeployment` was removed

* `models.DataContainerData$Update` was removed

* `models.CronSchedule` was removed

* `models.OnlineDeploymentData$DefinitionStages` was removed

* `models.EnvironmentVersionData$UpdateStages` was removed

* `models.BatchEndpointData$Update` was removed

* `models.ModelVersionData` was removed

* `models.PaginatedWorkspaceConnectionsList` was removed

* `models.ImageVerticalValidationDataSettings` was removed

* `models.JobBaseData$DefinitionStages` was removed

* `models.ComponentContainerData$UpdateStages` was removed

* `models.ScheduleType` was removed

* `models.TableVerticalDataSettings` was removed

* `models.DataContainerData$Definition` was removed

* `models.DatastoreDetails` was removed

* `models.EnvironmentContainerData$UpdateStages` was removed

* `models.ModelVersionData$DefinitionStages` was removed

* `models.CodeContainerData$DefinitionStages` was removed

* `models.PartialOnlineEndpointPartialTrackedResource` was removed

* `models.DataVersionBaseData$Update` was removed

* `models.SslConfigurationStatus` was removed

* `models.ImageSweepLimitSettings` was removed

* `models.BatchEndpointData$UpdateStages` was removed

* `models.KerberosKeytabSecrets` was removed

* `models.ComponentVersionData` was removed

* `models.BatchDeploymentData$Update` was removed

* `models.DatastoreData$Update` was removed

* `models.WorkspaceConnection` was removed

* `models.DataContainerData` was removed

#### `models.ImageClassificationMultilabel` was modified

* `withDataSettings(models.ImageVerticalDataSettings)` was removed
* `dataSettings()` was removed

#### `models.AzureBlobDatastore` was modified

* `models.DatastoreDetails withProperties(java.util.Map)` -> `models.DatastoreProperties withProperties(java.util.Map)`
* `models.DatastoreDetails withCredentials(models.DatastoreCredentials)` -> `models.DatastoreProperties withCredentials(models.DatastoreCredentials)`
* `models.DatastoreDetails withTags(java.util.Map)` -> `models.DatastoreProperties withTags(java.util.Map)`
* `models.DatastoreDetails withDescription(java.lang.String)` -> `models.DatastoreProperties withDescription(java.lang.String)`

#### `models.CodeVersions` was modified

* `models.CodeVersionData$DefinitionStages$Blank define(java.lang.String)` -> `models.CodeVersion$DefinitionStages$Blank define(java.lang.String)`
* `models.CodeVersionData get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.CodeVersion get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `models.CodeVersionData getById(java.lang.String)` -> `models.CodeVersion getById(java.lang.String)`

#### `models.ComponentContainers` was modified

* `models.ComponentContainerData$DefinitionStages$Blank define(java.lang.String)` -> `models.ComponentContainer$DefinitionStages$Blank define(java.lang.String)`
* `models.ComponentContainerData getById(java.lang.String)` -> `models.ComponentContainer getById(java.lang.String)`
* `models.ComponentContainerData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.ComponentContainer get(java.lang.String,java.lang.String,java.lang.String)`

#### `models.ImageModelSettingsObjectDetection` was modified

* `withCheckpointDatasetId(java.lang.String)` was removed
* `withCheckpointFilename(java.lang.String)` was removed
* `withSplitRatio(java.lang.Float)` was removed

#### `models.ImageSweepSettings` was modified

* `limits()` was removed
* `withLimits(models.ImageSweepLimitSettings)` was removed

#### `models.DataContainers` was modified

* `models.DataContainerData getById(java.lang.String)` -> `models.DataContainer getById(java.lang.String)`
* `models.DataContainerData$DefinitionStages$Blank define(java.lang.String)` -> `models.DataContainer$DefinitionStages$Blank define(java.lang.String)`
* `models.DataContainerData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.DataContainer get(java.lang.String,java.lang.String,java.lang.String)`

#### `models.ImageInstanceSegmentation` was modified

* `dataSettings()` was removed
* `withDataSettings(models.ImageVerticalDataSettings)` was removed

#### `models.OnlineEndpoints` was modified

* `models.OnlineEndpointData getById(java.lang.String)` -> `models.OnlineEndpoint getById(java.lang.String)`
* `models.OnlineEndpointData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.OnlineEndpoint get(java.lang.String,java.lang.String,java.lang.String)`
* `models.OnlineEndpointData$DefinitionStages$Blank define(java.lang.String)` -> `models.OnlineEndpoint$DefinitionStages$Blank define(java.lang.String)`

#### `models.PartialBatchDeployment` was modified

* `withRetrySettings(models.PartialBatchRetrySettings)` was removed
* `model()` was removed
* `withOutputAction(models.BatchOutputAction)` was removed
* `codeConfiguration()` was removed
* `properties()` was removed
* `miniBatchSize()` was removed
* `maxConcurrencyPerInstance()` was removed
* `retrySettings()` was removed
* `withOutputFileName(java.lang.String)` was removed
* `errorThreshold()` was removed
* `outputFileName()` was removed
* `withProperties(java.util.Map)` was removed
* `withEnvironmentId(java.lang.String)` was removed
* `loggingLevel()` was removed
* `environmentVariables()` was removed
* `withModel(models.PartialAssetReferenceBase)` was removed
* `withCompute(java.lang.String)` was removed
* `withCodeConfiguration(models.PartialCodeConfiguration)` was removed
* `withMiniBatchSize(java.lang.Long)` was removed
* `compute()` was removed
* `environmentId()` was removed
* `outputAction()` was removed
* `withLoggingLevel(models.BatchLoggingLevel)` was removed
* `withEnvironmentVariables(java.util.Map)` was removed
* `withErrorThreshold(java.lang.Integer)` was removed
* `withMaxConcurrencyPerInstance(java.lang.Integer)` was removed

#### `models.ImageClassificationBase` was modified

* `withDataSettings(models.ImageVerticalDataSettings)` was removed

#### `models.BatchDeployments` was modified

* `models.BatchDeploymentData getById(java.lang.String)` -> `models.BatchDeployment getById(java.lang.String)`
* `models.BatchDeploymentData get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.BatchDeployment get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `models.BatchDeploymentData$DefinitionStages$Blank define(java.lang.String)` -> `models.BatchDeployment$DefinitionStages$Blank define(java.lang.String)`

#### `models.TableVerticalFeaturizationSettings` was modified

* `dropColumns()` was removed
* `withDropColumns(java.util.List)` was removed

#### `models.EndpointPropertiesBase` was modified

* `java.lang.String scoringUri()` -> `java.net.URL scoringUri()`
* `java.lang.String swaggerUri()` -> `java.net.URL swaggerUri()`

#### `models.BatchEndpoints` was modified

* `models.BatchEndpointData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.BatchEndpoint get(java.lang.String,java.lang.String,java.lang.String)`
* `models.BatchEndpointData$DefinitionStages$Blank define(java.lang.String)` -> `models.BatchEndpoint$DefinitionStages$Blank define(java.lang.String)`
* `models.BatchEndpointData getById(java.lang.String)` -> `models.BatchEndpoint getById(java.lang.String)`

#### `models.ImageModelDistributionSettingsClassification` was modified

* `withSplitRatio(java.lang.String)` was removed

#### `models.AzureDataLakeGen1Datastore` was modified

* `models.DatastoreDetails withCredentials(models.DatastoreCredentials)` -> `models.DatastoreProperties withCredentials(models.DatastoreCredentials)`
* `models.DatastoreDetails withProperties(java.util.Map)` -> `models.DatastoreProperties withProperties(java.util.Map)`
* `models.DatastoreDetails withTags(java.util.Map)` -> `models.DatastoreProperties withTags(java.util.Map)`
* `models.DatastoreDetails withDescription(java.lang.String)` -> `models.DatastoreProperties withDescription(java.lang.String)`

#### `models.Workspaces` was modified

* `diagnose(java.lang.String,java.lang.String,models.DiagnoseWorkspaceParameters)` was removed

#### `models.ManagedOnlineDeployment` was modified

* `models.OnlineDeploymentDetails withReadinessProbe(models.ProbeSettings)` -> `models.OnlineDeploymentProperties withReadinessProbe(models.ProbeSettings)`
* `models.OnlineDeploymentDetails withCodeConfiguration(models.CodeConfiguration)` -> `models.OnlineDeploymentProperties withCodeConfiguration(models.CodeConfiguration)`
* `models.OnlineDeploymentDetails withEnvironmentId(java.lang.String)` -> `models.OnlineDeploymentProperties withEnvironmentId(java.lang.String)`
* `models.OnlineDeploymentDetails withEgressPublicNetworkAccess(models.EgressPublicNetworkAccessType)` -> `models.OnlineDeploymentProperties withEgressPublicNetworkAccess(models.EgressPublicNetworkAccessType)`
* `models.OnlineDeploymentDetails withDescription(java.lang.String)` -> `models.OnlineDeploymentProperties withDescription(java.lang.String)`
* `models.OnlineDeploymentDetails withModel(java.lang.String)` -> `models.OnlineDeploymentProperties withModel(java.lang.String)`
* `models.OnlineDeploymentDetails withProperties(java.util.Map)` -> `models.OnlineDeploymentProperties withProperties(java.util.Map)`
* `models.OnlineDeploymentDetails withEnvironmentVariables(java.util.Map)` -> `models.OnlineDeploymentProperties withEnvironmentVariables(java.util.Map)`
* `models.OnlineDeploymentDetails withInstanceType(java.lang.String)` -> `models.OnlineDeploymentProperties withInstanceType(java.lang.String)`
* `withPrivateNetworkConnection(java.lang.Boolean)` was removed
* `models.OnlineDeploymentDetails withScaleSettings(models.OnlineScaleSettings)` -> `models.OnlineDeploymentProperties withScaleSettings(models.OnlineScaleSettings)`
* `models.OnlineDeploymentDetails withAppInsightsEnabled(java.lang.Boolean)` -> `models.OnlineDeploymentProperties withAppInsightsEnabled(java.lang.Boolean)`
* `models.OnlineDeploymentDetails withModelMountPath(java.lang.String)` -> `models.OnlineDeploymentProperties withModelMountPath(java.lang.String)`
* `models.OnlineDeploymentDetails withRequestSettings(models.OnlineRequestSettings)` -> `models.OnlineDeploymentProperties withRequestSettings(models.OnlineRequestSettings)`
* `models.OnlineDeploymentDetails withLivenessProbe(models.ProbeSettings)` -> `models.OnlineDeploymentProperties withLivenessProbe(models.ProbeSettings)`

#### `models.EnvironmentContainers` was modified

* `models.EnvironmentContainerData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.EnvironmentContainer get(java.lang.String,java.lang.String,java.lang.String)`
* `models.EnvironmentContainerData$DefinitionStages$Blank define(java.lang.String)` -> `models.EnvironmentContainer$DefinitionStages$Blank define(java.lang.String)`
* `models.EnvironmentContainerData getById(java.lang.String)` -> `models.EnvironmentContainer getById(java.lang.String)`

#### `models.UriFolderDataVersion` was modified

* `models.DataVersionBaseDetails withDescription(java.lang.String)` -> `models.DataVersionBaseProperties withDescription(java.lang.String)`
* `models.DataVersionBaseDetails withTags(java.util.Map)` -> `models.DataVersionBaseProperties withTags(java.util.Map)`
* `models.DataVersionBaseDetails withProperties(java.util.Map)` -> `models.DataVersionBaseProperties withProperties(java.util.Map)`
* `models.DataVersionBaseDetails withDataUri(java.lang.String)` -> `models.DataVersionBaseProperties withDataUri(java.lang.String)`
* `models.DataVersionBaseDetails withIsAnonymous(java.lang.Boolean)` -> `models.DataVersionBaseProperties withIsAnonymous(java.lang.Boolean)`
* `models.DataVersionBaseDetails withIsArchived(java.lang.Boolean)` -> `models.DataVersionBaseProperties withIsArchived(java.lang.Boolean)`

#### `models.Datastores` was modified

* `models.DatastoreData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.Datastore get(java.lang.String,java.lang.String,java.lang.String)`
* `models.DatastoreData$DefinitionStages$Blank define(java.lang.String)` -> `models.Datastore$DefinitionStages$Blank define(java.lang.String)`
* `models.DatastoreData getById(java.lang.String)` -> `models.Datastore getById(java.lang.String)`

#### `models.PipelineJob` was modified

* `models.JobBaseDetails withDescription(java.lang.String)` -> `models.JobBaseProperties withDescription(java.lang.String)`
* `models.JobBaseDetails withTags(java.util.Map)` -> `models.JobBaseProperties withTags(java.util.Map)`
* `models.JobBaseDetails withServices(java.util.Map)` -> `models.JobBaseProperties withServices(java.util.Map)`
* `withSchedule(models.ScheduleBase)` was removed
* `models.JobBaseDetails withIdentity(models.IdentityConfiguration)` -> `models.JobBaseProperties withIdentity(models.IdentityConfiguration)`
* `models.JobBaseDetails withDisplayName(java.lang.String)` -> `models.JobBaseProperties withDisplayName(java.lang.String)`
* `models.JobBaseDetails withIsArchived(java.lang.Boolean)` -> `models.JobBaseProperties withIsArchived(java.lang.Boolean)`
* `models.JobBaseDetails withExperimentName(java.lang.String)` -> `models.JobBaseProperties withExperimentName(java.lang.String)`
* `models.JobBaseDetails withProperties(java.util.Map)` -> `models.JobBaseProperties withProperties(java.util.Map)`
* `models.JobBaseDetails withComputeId(java.lang.String)` -> `models.JobBaseProperties withComputeId(java.lang.String)`

#### `models.RecurrenceSchedule` was modified

* `withStartTime(java.time.OffsetDateTime)` was removed
* `pattern()` was removed
* `withScheduleStatus(models.ScheduleStatus)` was removed
* `withTimeZone(java.lang.String)` was removed
* `withPattern(models.RecurrencePattern)` was removed
* `withInterval(int)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withFrequency(models.RecurrenceFrequency)` was removed
* `interval()` was removed
* `frequency()` was removed

#### `models.Classification` was modified

* `withAllowedModels(java.util.List)` was removed
* `allowedModels()` was removed
* `withTrainingSettings(models.TrainingSettings)` was removed
* `blockedModels()` was removed
* `dataSettings()` was removed
* `withDataSettings(models.TableVerticalDataSettings)` was removed
* `models.TrainingSettings trainingSettings()` -> `models.ClassificationTrainingSettings trainingSettings()`
* `withBlockedModels(java.util.List)` was removed

#### `models.AzureDataLakeGen2Datastore` was modified

* `models.DatastoreDetails withCredentials(models.DatastoreCredentials)` -> `models.DatastoreProperties withCredentials(models.DatastoreCredentials)`
* `models.DatastoreDetails withTags(java.util.Map)` -> `models.DatastoreProperties withTags(java.util.Map)`
* `models.DatastoreDetails withProperties(java.util.Map)` -> `models.DatastoreProperties withProperties(java.util.Map)`
* `models.DatastoreDetails withDescription(java.lang.String)` -> `models.DatastoreProperties withDescription(java.lang.String)`

#### `models.ImageModelSettingsClassification` was modified

* `withCheckpointDatasetId(java.lang.String)` was removed
* `withCheckpointFilename(java.lang.String)` was removed
* `withSplitRatio(java.lang.Float)` was removed

#### `models.UriFileDataVersion` was modified

* `models.DataVersionBaseDetails withIsArchived(java.lang.Boolean)` -> `models.DataVersionBaseProperties withIsArchived(java.lang.Boolean)`
* `models.DataVersionBaseDetails withIsAnonymous(java.lang.Boolean)` -> `models.DataVersionBaseProperties withIsAnonymous(java.lang.Boolean)`
* `models.DataVersionBaseDetails withTags(java.util.Map)` -> `models.DataVersionBaseProperties withTags(java.util.Map)`
* `models.DataVersionBaseDetails withDataUri(java.lang.String)` -> `models.DataVersionBaseProperties withDataUri(java.lang.String)`
* `models.DataVersionBaseDetails withProperties(java.util.Map)` -> `models.DataVersionBaseProperties withProperties(java.util.Map)`
* `models.DataVersionBaseDetails withDescription(java.lang.String)` -> `models.DataVersionBaseProperties withDescription(java.lang.String)`

#### `models.WorkspaceConnections` was modified

* `models.WorkspaceConnection getById(java.lang.String)` -> `models.WorkspaceConnectionPropertiesV2BasicResource getById(java.lang.String)`
* `models.WorkspaceConnection get(java.lang.String,java.lang.String,java.lang.String)` -> `models.WorkspaceConnectionPropertiesV2BasicResource get(java.lang.String,java.lang.String,java.lang.String)`
* `models.WorkspaceConnection$DefinitionStages$Blank define(java.lang.String)` -> `models.WorkspaceConnectionPropertiesV2BasicResource$DefinitionStages$Blank define(java.lang.String)`

#### `models.Workspace` was modified

* `diagnose(models.DiagnoseWorkspaceParameters)` was removed

#### `models.AmlComputeProperties` was modified

* `withPropertyBag(java.util.Map)` was removed
* `java.util.Map propertyBag()` -> `java.lang.Object propertyBag()`

#### `models.NlpVertical` was modified

* `dataSettings()` was removed
* `withDataSettings(models.NlpVerticalDataSettings)` was removed

#### `models.TrialComponent` was modified

* `models.ResourceConfiguration resources()` -> `models.JobResourceConfiguration resources()`
* `withResources(models.ResourceConfiguration)` was removed

#### `models.Regression` was modified

* `withAllowedModels(java.util.List)` was removed
* `allowedModels()` was removed
* `blockedModels()` was removed
* `withBlockedModels(java.util.List)` was removed
* `withTrainingSettings(models.TrainingSettings)` was removed
* `models.TrainingSettings trainingSettings()` -> `models.RegressionTrainingSettings trainingSettings()`
* `dataSettings()` was removed
* `withDataSettings(models.TableVerticalDataSettings)` was removed

#### `models.MLTableData` was modified

* `models.DataVersionBaseDetails withProperties(java.util.Map)` -> `models.DataVersionBaseProperties withProperties(java.util.Map)`
* `models.DataVersionBaseDetails withIsArchived(java.lang.Boolean)` -> `models.DataVersionBaseProperties withIsArchived(java.lang.Boolean)`
* `models.DataVersionBaseDetails withTags(java.util.Map)` -> `models.DataVersionBaseProperties withTags(java.util.Map)`
* `models.DataVersionBaseDetails withDescription(java.lang.String)` -> `models.DataVersionBaseProperties withDescription(java.lang.String)`
* `models.DataVersionBaseDetails withIsAnonymous(java.lang.Boolean)` -> `models.DataVersionBaseProperties withIsAnonymous(java.lang.Boolean)`
* `models.DataVersionBaseDetails withDataUri(java.lang.String)` -> `models.DataVersionBaseProperties withDataUri(java.lang.String)`

#### `models.Forecasting` was modified

* `models.TrainingSettings trainingSettings()` -> `models.ForecastingTrainingSettings trainingSettings()`
* `withDataSettings(models.TableVerticalDataSettings)` was removed
* `withBlockedModels(java.util.List)` was removed
* `dataSettings()` was removed
* `allowedModels()` was removed
* `withTrainingSettings(models.TrainingSettings)` was removed
* `withAllowedModels(java.util.List)` was removed
* `blockedModels()` was removed

#### `models.AzureFileDatastore` was modified

* `models.DatastoreDetails withTags(java.util.Map)` -> `models.DatastoreProperties withTags(java.util.Map)`
* `models.DatastoreDetails withProperties(java.util.Map)` -> `models.DatastoreProperties withProperties(java.util.Map)`
* `models.DatastoreDetails withDescription(java.lang.String)` -> `models.DatastoreProperties withDescription(java.lang.String)`
* `models.DatastoreDetails withCredentials(models.DatastoreCredentials)` -> `models.DatastoreProperties withCredentials(models.DatastoreCredentials)`

#### `models.ImageModelDistributionSettings` was modified

* `withSplitRatio(java.lang.String)` was removed
* `splitRatio()` was removed

#### `models.OnlineDeployments` was modified

* `models.OnlineDeploymentData getById(java.lang.String)` -> `models.OnlineDeployment getById(java.lang.String)`
* `models.OnlineDeploymentData$DefinitionStages$Blank define(java.lang.String)` -> `models.OnlineDeployment$DefinitionStages$Blank define(java.lang.String)`
* `models.OnlineDeploymentData get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.OnlineDeployment get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`

#### `models.ImageVertical` was modified

* `withDataSettings(models.ImageVerticalDataSettings)` was removed
* `dataSettings()` was removed

#### `models.TextClassificationMultilabel` was modified

* `dataSettings()` was removed
* `withDataSettings(models.NlpVerticalDataSettings)` was removed

#### `models.ComponentVersions` was modified

* `models.ComponentVersionData$DefinitionStages$Blank define(java.lang.String)` -> `models.ComponentVersion$DefinitionStages$Blank define(java.lang.String)`
* `models.ComponentVersionData get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.ComponentVersion get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `models.ComponentVersionData getById(java.lang.String)` -> `models.ComponentVersion getById(java.lang.String)`

#### `models.ImageClassification` was modified

* `withDataSettings(models.ImageVerticalDataSettings)` was removed
* `dataSettings()` was removed

#### `models.CodeContainers` was modified

* `models.CodeContainerData$DefinitionStages$Blank define(java.lang.String)` -> `models.CodeContainer$DefinitionStages$Blank define(java.lang.String)`
* `models.CodeContainerData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.CodeContainer get(java.lang.String,java.lang.String,java.lang.String)`
* `models.CodeContainerData getById(java.lang.String)` -> `models.CodeContainer getById(java.lang.String)`

#### `models.Jobs` was modified

* `cancelWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.JobBaseData$DefinitionStages$Blank define(java.lang.String)` -> `models.JobBase$DefinitionStages$Blank define(java.lang.String)`
* `models.JobBaseData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.JobBase get(java.lang.String,java.lang.String,java.lang.String)`
* `models.JobBaseData getById(java.lang.String)` -> `models.JobBase getById(java.lang.String)`
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListViewType,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.TableVertical` was modified

* `dataSettings()` was removed
* `withDataSettings(models.TableVerticalDataSettings)` was removed
* `trainingSettings()` was removed
* `withTrainingSettings(models.TrainingSettings)` was removed

#### `models.DataVersions` was modified

* `models.DataVersionBaseData get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.DataVersionBase get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `models.DataVersionBaseData$DefinitionStages$Blank define(java.lang.String)` -> `models.DataVersionBase$DefinitionStages$Blank define(java.lang.String)`
* `models.DataVersionBaseData getById(java.lang.String)` -> `models.DataVersionBase getById(java.lang.String)`

#### `models.ImageObjectDetectionBase` was modified

* `withDataSettings(models.ImageVerticalDataSettings)` was removed

#### `models.TextNer` was modified

* `dataSettings()` was removed
* `withDataSettings(models.NlpVerticalDataSettings)` was removed

#### `models.EnvironmentVersions` was modified

* `models.EnvironmentVersionData get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.EnvironmentVersion get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `models.EnvironmentVersionData$DefinitionStages$Blank define(java.lang.String)` -> `models.EnvironmentVersion$DefinitionStages$Blank define(java.lang.String)`
* `models.EnvironmentVersionData getById(java.lang.String)` -> `models.EnvironmentVersion getById(java.lang.String)`

#### `models.CommandJob` was modified

* `models.JobBaseDetails withDescription(java.lang.String)` -> `models.JobBaseProperties withDescription(java.lang.String)`
* `models.JobBaseDetails withExperimentName(java.lang.String)` -> `models.JobBaseProperties withExperimentName(java.lang.String)`
* `withSchedule(models.ScheduleBase)` was removed
* `models.JobBaseDetails withTags(java.util.Map)` -> `models.JobBaseProperties withTags(java.util.Map)`
* `models.JobBaseDetails withServices(java.util.Map)` -> `models.JobBaseProperties withServices(java.util.Map)`
* `withResources(models.ResourceConfiguration)` was removed
* `models.ResourceConfiguration resources()` -> `models.JobResourceConfiguration resources()`
* `models.JobBaseDetails withProperties(java.util.Map)` -> `models.JobBaseProperties withProperties(java.util.Map)`
* `models.JobBaseDetails withComputeId(java.lang.String)` -> `models.JobBaseProperties withComputeId(java.lang.String)`
* `models.JobBaseDetails withIsArchived(java.lang.Boolean)` -> `models.JobBaseProperties withIsArchived(java.lang.Boolean)`
* `models.JobBaseDetails withDisplayName(java.lang.String)` -> `models.JobBaseProperties withDisplayName(java.lang.String)`
* `models.JobBaseDetails withIdentity(models.IdentityConfiguration)` -> `models.JobBaseProperties withIdentity(models.IdentityConfiguration)`

#### `models.SslConfiguration` was modified

* `withStatus(models.SslConfigurationStatus)` was removed
* `models.SslConfigurationStatus status()` -> `models.SslConfigStatus status()`

#### `models.ImageObjectDetection` was modified

* `dataSettings()` was removed
* `withDataSettings(models.ImageVerticalDataSettings)` was removed

#### `models.ModelVersions` was modified

* `models.ModelVersionData get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.ModelVersion get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `models.ModelVersionData$DefinitionStages$Blank define(java.lang.String)` -> `models.ModelVersion$DefinitionStages$Blank define(java.lang.String)`
* `models.ModelVersionData getById(java.lang.String)` -> `models.ModelVersion getById(java.lang.String)`

#### `models.ImageModelSettings` was modified

* `splitRatio()` was removed
* `checkpointDatasetId()` was removed
* `checkpointFilename()` was removed
* `withCheckpointDatasetId(java.lang.String)` was removed
* `withSplitRatio(java.lang.Float)` was removed
* `withCheckpointFilename(java.lang.String)` was removed

#### `models.SweepJob` was modified

* `models.JobBaseDetails withDisplayName(java.lang.String)` -> `models.JobBaseProperties withDisplayName(java.lang.String)`
* `models.JobBaseDetails withTags(java.util.Map)` -> `models.JobBaseProperties withTags(java.util.Map)`
* `withSchedule(models.ScheduleBase)` was removed
* `models.JobBaseDetails withExperimentName(java.lang.String)` -> `models.JobBaseProperties withExperimentName(java.lang.String)`
* `models.JobBaseDetails withComputeId(java.lang.String)` -> `models.JobBaseProperties withComputeId(java.lang.String)`
* `models.JobBaseDetails withProperties(java.util.Map)` -> `models.JobBaseProperties withProperties(java.util.Map)`
* `models.JobBaseDetails withDescription(java.lang.String)` -> `models.JobBaseProperties withDescription(java.lang.String)`
* `models.JobBaseDetails withServices(java.util.Map)` -> `models.JobBaseProperties withServices(java.util.Map)`
* `models.JobBaseDetails withIdentity(models.IdentityConfiguration)` -> `models.JobBaseProperties withIdentity(models.IdentityConfiguration)`
* `models.JobBaseDetails withIsArchived(java.lang.Boolean)` -> `models.JobBaseProperties withIsArchived(java.lang.Boolean)`

#### `models.ModelContainers` was modified

* `models.ModelContainerData get(java.lang.String,java.lang.String,java.lang.String)` -> `models.ModelContainer get(java.lang.String,java.lang.String,java.lang.String)`
* `models.ModelContainerData$DefinitionStages$Blank define(java.lang.String)` -> `models.ModelContainer$DefinitionStages$Blank define(java.lang.String)`
* `models.ModelContainerData getById(java.lang.String)` -> `models.ModelContainer getById(java.lang.String)`

#### `models.ScheduleBase` was modified

* `withScheduleStatus(models.ScheduleStatus)` was removed
* `timeZone()` was removed
* `scheduleStatus()` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `endTime()` was removed
* `startTime()` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withTimeZone(java.lang.String)` was removed

#### `models.ImageModelDistributionSettingsObjectDetection` was modified

* `withSplitRatio(java.lang.String)` was removed

#### `models.TextClassification` was modified

* `dataSettings()` was removed
* `withDataSettings(models.NlpVerticalDataSettings)` was removed

#### `models.AutoMLJob` was modified

* `models.JobBaseDetails withDescription(java.lang.String)` -> `models.JobBaseProperties withDescription(java.lang.String)`
* `models.ResourceConfiguration resources()` -> `models.JobResourceConfiguration resources()`
* `models.JobBaseDetails withIdentity(models.IdentityConfiguration)` -> `models.JobBaseProperties withIdentity(models.IdentityConfiguration)`
* `withSchedule(models.ScheduleBase)` was removed
* `models.JobBaseDetails withDisplayName(java.lang.String)` -> `models.JobBaseProperties withDisplayName(java.lang.String)`
* `models.JobBaseDetails withServices(java.util.Map)` -> `models.JobBaseProperties withServices(java.util.Map)`
* `withResources(models.ResourceConfiguration)` was removed
* `models.JobBaseDetails withIsArchived(java.lang.Boolean)` -> `models.JobBaseProperties withIsArchived(java.lang.Boolean)`
* `models.JobBaseDetails withExperimentName(java.lang.String)` -> `models.JobBaseProperties withExperimentName(java.lang.String)`
* `models.JobBaseDetails withTags(java.util.Map)` -> `models.JobBaseProperties withTags(java.util.Map)`
* `models.JobBaseDetails withComputeId(java.lang.String)` -> `models.JobBaseProperties withComputeId(java.lang.String)`
* `models.JobBaseDetails withProperties(java.util.Map)` -> `models.JobBaseProperties withProperties(java.util.Map)`

#### `models.KubernetesOnlineDeployment` was modified

* `models.OnlineDeploymentDetails withCodeConfiguration(models.CodeConfiguration)` -> `models.OnlineDeploymentProperties withCodeConfiguration(models.CodeConfiguration)`
* `models.OnlineDeploymentDetails withModel(java.lang.String)` -> `models.OnlineDeploymentProperties withModel(java.lang.String)`
* `models.OnlineDeploymentDetails withRequestSettings(models.OnlineRequestSettings)` -> `models.OnlineDeploymentProperties withRequestSettings(models.OnlineRequestSettings)`
* `models.OnlineDeploymentDetails withInstanceType(java.lang.String)` -> `models.OnlineDeploymentProperties withInstanceType(java.lang.String)`
* `models.OnlineDeploymentDetails withModelMountPath(java.lang.String)` -> `models.OnlineDeploymentProperties withModelMountPath(java.lang.String)`
* `models.OnlineDeploymentDetails withAppInsightsEnabled(java.lang.Boolean)` -> `models.OnlineDeploymentProperties withAppInsightsEnabled(java.lang.Boolean)`
* `withPrivateNetworkConnection(java.lang.Boolean)` was removed
* `models.OnlineDeploymentDetails withDescription(java.lang.String)` -> `models.OnlineDeploymentProperties withDescription(java.lang.String)`
* `models.OnlineDeploymentDetails withEnvironmentVariables(java.util.Map)` -> `models.OnlineDeploymentProperties withEnvironmentVariables(java.util.Map)`
* `models.OnlineDeploymentDetails withEnvironmentId(java.lang.String)` -> `models.OnlineDeploymentProperties withEnvironmentId(java.lang.String)`
* `models.OnlineDeploymentDetails withReadinessProbe(models.ProbeSettings)` -> `models.OnlineDeploymentProperties withReadinessProbe(models.ProbeSettings)`
* `models.OnlineDeploymentDetails withLivenessProbe(models.ProbeSettings)` -> `models.OnlineDeploymentProperties withLivenessProbe(models.ProbeSettings)`
* `models.OnlineDeploymentDetails withScaleSettings(models.OnlineScaleSettings)` -> `models.OnlineDeploymentProperties withScaleSettings(models.OnlineScaleSettings)`
* `models.OnlineDeploymentDetails withProperties(java.util.Map)` -> `models.OnlineDeploymentProperties withProperties(java.util.Map)`
* `models.OnlineDeploymentDetails withEgressPublicNetworkAccess(models.EgressPublicNetworkAccessType)` -> `models.OnlineDeploymentProperties withEgressPublicNetworkAccess(models.EgressPublicNetworkAccessType)`

### Features Added

* `models.Datastore$Definition` was added

* `models.OnlineDeployment$UpdateStages` was added

* `models.DataVersionBase$UpdateStages` was added

* `models.ComponentContainer$DefinitionStages` was added

* `models.ModelContainer$UpdateStages` was added

* `models.ConnectionCategory` was added

* `models.WorkspaceConnectionPropertiesV2` was added

* `models.JobResourceConfiguration` was added

* `models.CronTrigger` was added

* `models.CodeVersion$UpdateStages` was added

* `models.SslConfigStatus` was added

* `models.ScheduleProvisioningStatus` was added

* `models.ComponentContainer` was added

* `models.ComponentVersion$Definition` was added

* `models.ScheduleProperties` was added

* `models.DataContainer$Definition` was added

* `models.PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties` was added

* `models.DataVersionBaseProperties` was added

* `models.OnlineEndpoint$Definition` was added

* `models.DataVersionBase` was added

* `models.CodeVersion$Update` was added

* `models.CodeContainer$Update` was added

* `models.BatchEndpoint$Update` was added

* `models.JobBase$DefinitionStages` was added

* `models.ComponentVersion` was added

* `models.OnlineDeployment` was added

* `models.BatchDeploymentProperties` was added

* `models.PartialMinimalTrackedResourceWithSku` was added

* `models.WorkspaceConnectionPropertiesV2BasicResourceArmPaginatedResult` was added

* `models.BatchDeployment$Update` was added

* `models.ClassificationTrainingSettings` was added

* `models.ModelVersionProperties` was added

* `models.BatchEndpointProperties` was added

* `models.OnlineDeploymentProperties` was added

* `models.AutoRebuildSetting` was added

* `models.WorkspaceConnectionUsernamePassword` was added

* `models.OnlineEndpoint$DefinitionStages` was added

* `models.BatchDeployment$UpdateStages` was added

* `models.WorkspaceConnectionPersonalAccessToken` was added

* `models.CodeContainerProperties` was added

* `models.JobBase$Definition` was added

* `models.ModelVersion$UpdateStages` was added

* `models.EnvironmentVersion$Update` was added

* `models.ModelContainerProperties` was added

* `models.DataContainer$DefinitionStages` was added

* `models.Datastore$DefinitionStages` was added

* `models.Schedule$Update` was added

* `models.EnvironmentVersionProperties` was added

* `models.Schedules` was added

* `models.EnvironmentContainerProperties` was added

* `models.ConnectionAuthType` was added

* `models.Schedule$UpdateStages` was added

* `models.CodeVersionProperties` was added

* `models.WorkspaceConnectionPropertiesV2BasicResource$DefinitionStages` was added

* `models.EnvironmentContainer$DefinitionStages` was added

* `models.CodeVersion$DefinitionStages` was added

* `models.WorkspaceConnectionPropertiesV2BasicResource$Definition` was added

* `models.ModelContainer$Definition` was added

* `models.CodeContainer$Definition` was added

* `models.EnvironmentVersion$UpdateStages` was added

* `models.WeekDay` was added

* `models.BatchEndpoint$UpdateStages` was added

* `models.CodeContainer$UpdateStages` was added

* `models.ModelVersion$Update` was added

* `models.PartialMinimalTrackedResource` was added

* `models.ScheduleProvisioningState` was added

* `models.ComponentVersion$Update` was added

* `models.WorkspaceConnectionManagedIdentity` was added

* `models.ScheduleResourceArmPaginatedResult` was added

* `models.RegressionTrainingSettings` was added

* `models.BatchDeployment` was added

* `models.DataContainer` was added

* `models.CodeContainer$DefinitionStages` was added

* `models.ComponentContainer$UpdateStages` was added

* `models.OnlineEndpoint` was added

* `models.EnvironmentContainer$Definition` was added

* `models.CodeVersion$Definition` was added

* `models.ModelContainer` was added

* `models.ComponentContainerProperties` was added

* `models.ScheduleActionType` was added

* `models.CodeVersion` was added

* `models.BatchEndpoint$DefinitionStages` was added

* `models.Datastore` was added

* `models.NoneAuthTypeWorkspaceConnectionProperties` was added

* `models.ScheduleActionBase` was added

* `models.DataVersionBase$Update` was added

* `models.EnvironmentVersion$DefinitionStages` was added

* `models.BatchEndpoint` was added

* `models.UsernamePasswordAuthTypeWorkspaceConnectionProperties` was added

* `models.TriggerType` was added

* `models.OnlineDeployment$DefinitionStages` was added

* `models.BatchEndpoint$Definition` was added

* `models.TriggerBase` was added

* `models.BatchDeployment$DefinitionStages` was added

* `models.OnlineEndpoint$Update` was added

* `models.Schedule` was added

* `models.JobBase$Update` was added

* `models.WorkspaceConnectionSharedAccessSignature` was added

* `models.EnvironmentContainer$Update` was added

* `models.WorkspaceConnectionPropertiesV2BasicResource` was added

* `models.ModelContainer$Update` was added

* `models.DatastoreProperties` was added

* `models.OnlineEndpoint$UpdateStages` was added

* `models.DeploymentResourceConfiguration` was added

* `models.ComponentVersion$DefinitionStages` was added

* `models.ComponentVersion$UpdateStages` was added

* `models.ModelVersion$Definition` was added

* `models.ScheduleListViewType` was added

* `models.EnvironmentVersion$Definition` was added

* `models.EnvironmentContainer` was added

* `models.SasAuthTypeWorkspaceConnectionProperties` was added

* `models.OnlineDeployment$Update` was added

* `models.OnlineDeployment$Definition` was added

* `models.Schedule$DefinitionStages` was added

* `models.Datastore$UpdateStages` was added

* `models.ComponentContainer$Definition` was added

* `models.BatchDeployment$Definition` was added

* `models.DataContainer$Update` was added

* `models.ManagedIdentityAuthTypeWorkspaceConnectionProperties` was added

* `models.DataContainer$UpdateStages` was added

* `models.BlockedTransformers` was added

* `models.ModelVersion$DefinitionStages` was added

* `models.Schedule$Definition` was added

* `models.EndpointScheduleAction` was added

* `models.CodeContainer` was added

* `models.JobBase` was added

* `models.Datastore$Update` was added

* `models.ModelVersion` was added

* `models.EnvironmentContainer$UpdateStages` was added

* `models.DataVersionBase$Definition` was added

* `models.ForecastingTrainingSettings` was added

* `models.ModelContainer$DefinitionStages` was added

* `models.DataContainerProperties` was added

* `models.PartialMinimalTrackedResourceWithIdentity` was added

* `models.JobScheduleAction` was added

* `models.OnlineEndpointProperties` was added

* `models.EnvironmentVersion` was added

* `models.JobBase$UpdateStages` was added

* `models.RecurrenceTrigger` was added

* `models.JobBaseProperties` was added

* `models.ComponentContainer$Update` was added

* `models.DataVersionBase$DefinitionStages` was added

* `models.ComponentVersionProperties` was added

* `models.PatAuthTypeWorkspaceConnectionProperties` was added

#### `models.ImageClassificationMultilabel` was modified

* `withValidationData(models.MLTableJobInput)` was added
* `withTrainingData(models.MLTableJobInput)` was added
* `withTargetColumnName(java.lang.String)` was added
* `withValidationDataSize(java.lang.Double)` was added
* `validationDataSize()` was added
* `validationData()` was added

#### `models.ImageModelSettingsObjectDetection` was modified

* `withCheckpointModel(models.MLFlowModelJobInput)` was added

#### `models.SynapseSpark` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.ImageInstanceSegmentation` was modified

* `withTargetColumnName(java.lang.String)` was added
* `withValidationData(models.MLTableJobInput)` was added
* `withTrainingData(models.MLTableJobInput)` was added
* `withValidationDataSize(java.lang.Double)` was added
* `validationDataSize()` was added
* `validationData()` was added

#### `models.Aks` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.DataLakeAnalytics` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.ComputeStartStopSchedule` was modified

* `status()` was added
* `withRecurrence(models.RecurrenceTrigger)` was added
* `recurrence()` was added
* `cron()` was added
* `withStatus(models.ScheduleStatus)` was added
* `triggerType()` was added
* `withCron(models.CronTrigger)` was added
* `withTriggerType(models.TriggerType)` was added

#### `models.ImageClassificationBase` was modified

* `withValidationDataSize(java.lang.Double)` was added
* `withValidationData(models.MLTableJobInput)` was added

#### `models.Kubernetes` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.Workspace$Definition` was modified

* `withV1LegacyMode(java.lang.Boolean)` was added

#### `models.PipelineJob` was modified

* `withComponentId(java.lang.String)` was added
* `sourceJobId()` was added
* `withSourceJobId(java.lang.String)` was added

#### `models.RecurrenceSchedule` was modified

* `minutes()` was added
* `monthDays()` was added
* `hours()` was added
* `withMinutes(java.util.List)` was added
* `withWeekDays(java.util.List)` was added
* `withHours(java.util.List)` was added
* `weekDays()` was added
* `withMonthDays(java.util.List)` was added

#### `models.Classification` was modified

* `withNCrossValidations(models.NCrossValidations)` was added
* `testDataSize()` was added
* `withTrainingData(models.MLTableJobInput)` was added
* `withTargetColumnName(java.lang.String)` was added
* `withTestData(models.MLTableJobInput)` was added
* `withValidationDataSize(java.lang.Double)` was added
* `positiveLabel()` was added
* `validationData()` was added
* `validationDataSize()` was added
* `withTestDataSize(java.lang.Double)` was added
* `weightColumnName()` was added
* `withCvSplitColumnNames(java.util.List)` was added
* `withTrainingSettings(models.ClassificationTrainingSettings)` was added
* `testData()` was added
* `withWeightColumnName(java.lang.String)` was added
* `withValidationData(models.MLTableJobInput)` was added
* `cvSplitColumnNames()` was added
* `nCrossValidations()` was added
* `withPositiveLabel(java.lang.String)` was added

#### `models.VirtualMachine` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.AmlCompute` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.ImageModelSettingsClassification` was modified

* `withCheckpointModel(models.MLFlowModelJobInput)` was added

#### `models.Databricks` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.ComputeInstance` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.Workspace` was modified

* `v1LegacyMode()` was added

#### `models.AmlComputeProperties` was modified

* `withPropertyBag(java.lang.Object)` was added

#### `models.NlpVertical` was modified

* `validationData()` was added
* `withValidationData(models.MLTableJobInput)` was added

#### `models.TrialComponent` was modified

* `withResources(models.JobResourceConfiguration)` was added

#### `models.Regression` was modified

* `withTestDataSize(java.lang.Double)` was added
* `withTrainingData(models.MLTableJobInput)` was added
* `cvSplitColumnNames()` was added
* `withValidationData(models.MLTableJobInput)` was added
* `withTargetColumnName(java.lang.String)` was added
* `validationDataSize()` was added
* `validationData()` was added
* `withCvSplitColumnNames(java.util.List)` was added
* `withNCrossValidations(models.NCrossValidations)` was added
* `nCrossValidations()` was added
* `withValidationDataSize(java.lang.Double)` was added
* `testDataSize()` was added
* `withTestData(models.MLTableJobInput)` was added
* `withTrainingSettings(models.RegressionTrainingSettings)` was added
* `withWeightColumnName(java.lang.String)` was added
* `weightColumnName()` was added
* `testData()` was added

#### `MachineLearningManager` was modified

* `schedules()` was added

#### `models.Forecasting` was modified

* `withTrainingSettings(models.ForecastingTrainingSettings)` was added
* `validationData()` was added
* `withTestData(models.MLTableJobInput)` was added
* `withTargetColumnName(java.lang.String)` was added
* `testData()` was added
* `withNCrossValidations(models.NCrossValidations)` was added
* `withWeightColumnName(java.lang.String)` was added
* `withTrainingData(models.MLTableJobInput)` was added
* `withValidationDataSize(java.lang.Double)` was added
* `withCvSplitColumnNames(java.util.List)` was added
* `validationDataSize()` was added
* `nCrossValidations()` was added
* `testDataSize()` was added
* `withValidationData(models.MLTableJobInput)` was added
* `withTestDataSize(java.lang.Double)` was added
* `weightColumnName()` was added
* `cvSplitColumnNames()` was added

#### `models.Compute` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.AutoMLVertical` was modified

* `trainingData()` was added
* `withTargetColumnName(java.lang.String)` was added
* `targetColumnName()` was added
* `withTrainingData(models.MLTableJobInput)` was added

#### `models.ImageVertical` was modified

* `validationDataSize()` was added
* `validationData()` was added
* `withValidationDataSize(java.lang.Double)` was added
* `withValidationData(models.MLTableJobInput)` was added

#### `models.TextClassificationMultilabel` was modified

* `withTrainingData(models.MLTableJobInput)` was added
* `withValidationData(models.MLTableJobInput)` was added
* `withTargetColumnName(java.lang.String)` was added
* `validationData()` was added

#### `models.WorkspaceProperties` was modified

* `v1LegacyMode()` was added

#### `models.ImageClassification` was modified

* `withTrainingData(models.MLTableJobInput)` was added
* `withValidationData(models.MLTableJobInput)` was added
* `validationData()` was added
* `validationDataSize()` was added
* `withTargetColumnName(java.lang.String)` was added
* `withValidationDataSize(java.lang.Double)` was added

#### `models.Jobs` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListViewType,com.azure.core.util.Context)` was added
* `cancel(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.TableVertical` was modified

* `cvSplitColumnNames()` was added
* `nCrossValidations()` was added
* `withValidationData(models.MLTableJobInput)` was added
* `testData()` was added
* `withValidationDataSize(java.lang.Double)` was added
* `withCvSplitColumnNames(java.util.List)` was added
* `validationData()` was added
* `withTestDataSize(java.lang.Double)` was added
* `weightColumnName()` was added
* `testDataSize()` was added
* `withTestData(models.MLTableJobInput)` was added
* `validationDataSize()` was added
* `withNCrossValidations(models.NCrossValidations)` was added
* `withWeightColumnName(java.lang.String)` was added

#### `models.ImageObjectDetectionBase` was modified

* `withValidationData(models.MLTableJobInput)` was added
* `withValidationDataSize(java.lang.Double)` was added

#### `models.TextNer` was modified

* `withTrainingData(models.MLTableJobInput)` was added
* `withTargetColumnName(java.lang.String)` was added
* `withValidationData(models.MLTableJobInput)` was added
* `validationData()` was added

#### `models.CommandJob` was modified

* `withResources(models.JobResourceConfiguration)` was added
* `withComponentId(java.lang.String)` was added

#### `models.SslConfiguration` was modified

* `withStatus(models.SslConfigStatus)` was added

#### `models.DataFactory` was modified

* `withComputeLocation(java.lang.String)` was added

#### `models.ImageObjectDetection` was modified

* `withTrainingData(models.MLTableJobInput)` was added
* `withValidationData(models.MLTableJobInput)` was added
* `validationDataSize()` was added
* `withValidationDataSize(java.lang.Double)` was added
* `validationData()` was added
* `withTargetColumnName(java.lang.String)` was added

#### `models.ImageModelSettings` was modified

* `withCheckpointModel(models.MLFlowModelJobInput)` was added
* `checkpointModel()` was added

#### `models.SweepJob` was modified

* `withComponentId(java.lang.String)` was added

#### `models.ScheduleBase` was modified

* `status()` was added
* `withStatus(models.ScheduleStatus)` was added
* `provisioningStatus()` was added
* `withProvisioningStatus(models.ScheduleProvisioningState)` was added
* `id()` was added
* `withId(java.lang.String)` was added

#### `models.TextClassification` was modified

* `withTrainingData(models.MLTableJobInput)` was added
* `validationData()` was added
* `withValidationData(models.MLTableJobInput)` was added
* `withTargetColumnName(java.lang.String)` was added

#### `models.AutoMLJob` was modified

* `withResources(models.JobResourceConfiguration)` was added
* `withComponentId(java.lang.String)` was added

#### `models.HDInsight` was modified

* `withComputeLocation(java.lang.String)` was added

## 1.0.0-beta.2 (2022-05-27)

- Azure Resource Manager Machine Learning client library for Java. This package contains Microsoft Azure SDK for Machine Learning Management SDK. These APIs allow end users to operate on Azure Machine Learning Workspace resources. Package tag package-2022-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `MachineLearningServicesManager` was removed

* `MachineLearningServicesManager$Configurable` was removed

### Features Added

* `MachineLearningManager` was added

* `MachineLearningManager$Configurable` was added

## 1.0.0-beta.1 (2022-05-27)

- Azure Resource Manager MachineLearningServices client library for Java. This package contains Microsoft Azure SDK for MachineLearningServices Management SDK. These APIs allow end users to operate on Azure Machine Learning Workspace resources. Package tag package-2022-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
