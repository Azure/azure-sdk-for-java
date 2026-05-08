# Release History

## 1.1.0-beta.4 (2026-05-08)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. The HDInsight Management Client. Package api-version 2025-01-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.ApplicationListResult` was removed

#### `models.ScriptActionExecutionHistoryList` was removed

#### `models.ScriptActionsList` was removed

#### `models.ClusterListResult` was removed

#### `models.ResourceId` was modified

* `validate()` was removed

#### `models.LocalizedName` was modified

* `LocalizedName()` was changed to private access
* `withValue(java.lang.String)` was removed
* `validate()` was removed
* `withLocalizedValue(java.lang.String)` was removed

#### `models.NetworkProperties` was modified

* `validate()` was removed

#### `models.LinuxOperatingSystemProfile` was modified

* `validate()` was removed

#### `models.Role` was modified

* `validate()` was removed

#### `models.OsProfile` was modified

* `validate()` was removed

#### `models.DiskEncryptionProperties` was modified

* `validate()` was removed

#### `models.ApplicationProperties` was modified

* `validate()` was removed

#### `models.SshPublicKey` was modified

* `validate()` was removed

#### `models.KafkaRestProperties` was modified

* `validate()` was removed

#### `models.ClusterCreateParametersExtended` was modified

* `validate()` was removed

#### `models.ApplicationGetEndpoint` was modified

* `validate()` was removed

#### `models.ClusterDiskEncryptionParameters` was modified

* `validate()` was removed

#### `models.Errors` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `PrivateEndpoint()` was changed to private access
* `withId(java.lang.String)` was removed
* `validate()` was removed

#### `models.ScriptActionExecutionSummary` was modified

* `ScriptActionExecutionSummary()` was changed to private access
* `validate()` was removed

#### `models.AzureMonitorTableConfiguration` was modified

* `validate()` was removed

#### `models.EntraUserInfo` was modified

* `validate()` was removed

#### `models.HardwareProfile` was modified

* `validate()` was removed

#### `models.VmSizeProperty` was modified

* `VmSizeProperty()` was changed to private access
* `withWebWorkerResourceDiskSizeInMb(java.lang.Long)` was removed
* `withMaxDataDiskCount(java.lang.Long)` was removed
* `withDataDiskStorageTier(java.lang.String)` was removed
* `validate()` was removed
* `withSupportedByVirtualMachines(java.lang.Boolean)` was removed
* `withVirtualMachineResourceDiskSizeInMb(java.lang.Long)` was removed
* `withName(java.lang.String)` was removed
* `withCores(java.lang.Integer)` was removed
* `withMemoryInMb(java.lang.Long)` was removed
* `withSupportedByWebWorkerRoles(java.lang.Boolean)` was removed
* `withLabel(java.lang.String)` was removed

#### `models.ClusterResizeParameters` was modified

* `validate()` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `withMetricSpecifications(java.util.List)` was removed
* `validate()` was removed

#### `models.Usage` was modified

* `Usage()` was changed to private access
* `withUnit(java.lang.String)` was removed
* `withCurrentValue(java.lang.Long)` was removed
* `withLimit(java.lang.Long)` was removed
* `validate()` was removed

#### `models.ClientGroupInfo` was modified

* `validate()` was removed

#### `models.AutoscaleSchedule` was modified

* `validate()` was removed

#### `models.ClusterIdentity` was modified

* `validate()` was removed

#### `models.NameAvailabilityCheckRequestParameters` was modified

* `validate()` was removed

#### `models.ClusterCreateProperties` was modified

* `validate()` was removed

#### `models.QuotaCapability` was modified

* `QuotaCapability()` was changed to private access
* `withRegionalQuotas(java.util.List)` was removed
* `validate()` was removed
* `withMaxCoresAllowed(java.lang.Long)` was removed
* `withCoresUsed(java.lang.Long)` was removed

#### `models.VersionsCapability` was modified

* `VersionsCapability()` was changed to private access
* `withAvailable(java.util.List)` was removed
* `validate()` was removed

#### `models.BillingMeters` was modified

* `BillingMeters()` was changed to private access
* `withUnit(java.lang.String)` was removed
* `withMeter(java.lang.String)` was removed
* `validate()` was removed
* `withMeterParameter(java.lang.String)` was removed

#### `models.DiskBillingMeters` was modified

* `DiskBillingMeters()` was changed to private access
* `withDiskRpMeter(java.lang.String)` was removed
* `withSku(java.lang.String)` was removed
* `withTier(models.Tier)` was removed
* `validate()` was removed

#### `models.Autoscale` was modified

* `validate()` was removed

#### `models.StorageProfile` was modified

* `validate()` was removed

#### `models.Dimension` was modified

* `Dimension()` was changed to private access
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withToBeExportedForShoebox(java.lang.Boolean)` was removed
* `withInternalName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.PrivateLinkConfiguration` was modified

* `validate()` was removed

#### `models.IpConfiguration` was modified

* `validate()` was removed

#### `models.UpdateGatewaySettingsParameters` was modified

* `validate()` was removed

#### `models.RegionsCapability` was modified

* `RegionsCapability()` was changed to private access
* `withAvailable(java.util.List)` was removed
* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `validate()` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed

#### `models.AutoscaleTimeAndCapacity` was modified

* `validate()` was removed

#### `models.VmSizeCompatibilityFilterV2` was modified

* `VmSizeCompatibilityFilterV2()` was changed to private access
* `withClusterVersions(java.util.List)` was removed
* `withComputeIsolationSupported(java.lang.String)` was removed
* `withEspApplied(java.lang.String)` was removed
* `validate()` was removed
* `withFilterMode(models.FilterMode)` was removed
* `withOsType(java.util.List)` was removed
* `withRegions(java.util.List)` was removed
* `withVmSizes(java.util.List)` was removed
* `withClusterFlavors(java.util.List)` was removed
* `withNodeTypes(java.util.List)` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed

#### `models.ValidationErrorInfo` was modified

* `ValidationErrorInfo()` was changed to private access
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed
* `withErrorResource(java.lang.String)` was removed
* `withMessageArguments(java.util.List)` was removed
* `validate()` was removed

#### `models.SshProfile` was modified

* `validate()` was removed

#### `models.VirtualNetworkProfile` was modified

* `validate()` was removed

#### `models.ComputeProfile` was modified

* `validate()` was removed

#### `models.ExecuteScriptActionParameters` was modified

* `validate()` was removed

#### `models.VersionSpec` was modified

* `VersionSpec()` was changed to private access
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withComponentVersions(java.util.Map)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withIsDefault(java.lang.Boolean)` was removed

#### `models.RegionalQuotaCapability` was modified

* `RegionalQuotaCapability()` was changed to private access
* `withRegionName(java.lang.String)` was removed
* `withCoresUsed(java.lang.Long)` was removed
* `withCoresAvailable(java.lang.Long)` was removed
* `validate()` was removed

#### `models.DataDisksGroups` was modified

* `validate()` was removed

#### `models.ClusterDefinition` was modified

* `validate()` was removed

#### `models.ScriptAction` was modified

* `validate()` was removed

#### `models.StorageAccount` was modified

* `validate()` was removed

#### `models.BillingResources` was modified

* `BillingResources()` was changed to private access
* `withRegion(java.lang.String)` was removed
* `withDiskBillingMeters(java.util.List)` was removed
* `withBillingMeters(java.util.List)` was removed
* `validate()` was removed

#### `models.ConnectivityEndpoint` was modified

* `ConnectivityEndpoint()` was changed to private access
* `withLocation(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withProtocol(java.lang.String)` was removed
* `withPrivateIpAddress(java.lang.String)` was removed
* `withPort(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.AutoscaleConfigurationUpdateParameter` was modified

* `validate()` was removed

#### `models.MetricSpecifications` was modified

* `MetricSpecifications()` was changed to private access
* `withEnableRegionalMdmAccount(java.lang.Boolean)` was removed
* `withSourceMdmAccount(java.lang.String)` was removed
* `withCategory(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withUnit(java.lang.String)` was removed
* `withSourceMdmNamespace(java.lang.String)` was removed
* `withDelegateMetricNameOverride(java.lang.String)` was removed
* `withMetricFilterPattern(java.lang.String)` was removed
* `withSupportedAggregationTypes(java.util.List)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed
* `validate()` was removed
* `withIsInternal(java.lang.Boolean)` was removed
* `withDimensions(java.util.List)` was removed
* `withSupportedTimeGrainTypes(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withAggregationType(java.lang.String)` was removed
* `withResourceIdDimensionNameOverride(java.lang.String)` was removed

#### `models.AzureMonitorSelectedConfigurations` was modified

* `validate()` was removed

#### `models.ClusterPatchParameters` was modified

* `validate()` was removed

#### `models.ComputeIsolationProperties` was modified

* `validate()` was removed

#### `models.ExcludedServicesConfig` was modified

* `ExcludedServicesConfig()` was changed to private access
* `validate()` was removed
* `withExcludedServicesList(java.lang.String)` was removed
* `withExcludedServicesConfigId(java.lang.String)` was removed

#### `models.AutoscaleRecurrence` was modified

* `validate()` was removed

#### `models.Extension` was modified

* `validate()` was removed

#### `models.OperationProperties` was modified

* `OperationProperties()` was changed to private access
* `validate()` was removed
* `withServiceSpecification(models.ServiceSpecification)` was removed

#### `models.ApplicationGetHttpsEndpoint` was modified

* `validate()` was removed

#### `models.ClusterCreateRequestValidationParameters` was modified

* `validate()` was removed

#### `models.AaddsResourceDetails` was modified

* `AaddsResourceDetails()` was changed to private access
* `withResourceId(java.lang.String)` was removed
* `validate()` was removed
* `withSubnetId(java.lang.String)` was removed
* `withInitialSyncComplete(java.lang.Boolean)` was removed
* `withDomainName(java.lang.String)` was removed
* `withLdapsPublicCertificateInBase64(java.lang.String)` was removed
* `withLdapsEnabled(java.lang.Boolean)` was removed
* `withTenantId(java.lang.String)` was removed

#### `models.EncryptionInTransitProperties` was modified

* `validate()` was removed

#### `models.AutoscaleCapacity` was modified

* `validate()` was removed

#### `models.IpTag` was modified

* `validate()` was removed

#### `models.AzureMonitorRequest` was modified

* `validate()` was removed

#### `models.UpdateClusterIdentityCertificateParameters` was modified

* `validate()` was removed

#### `models.QuotaInfo` was modified

* `QuotaInfo()` was changed to private access
* `validate()` was removed
* `withCoresUsed(java.lang.Integer)` was removed

#### `models.ClusterGetProperties` was modified

* `ClusterGetProperties()` was changed to private access
* `withPrivateLinkConfigurations(java.util.List)` was removed
* `withDiskEncryptionProperties(models.DiskEncryptionProperties)` was removed
* `withClusterHdpVersion(java.lang.String)` was removed
* `withStorageProfile(models.StorageProfile)` was removed
* `withOsType(models.OSType)` was removed
* `withComputeIsolationProperties(models.ComputeIsolationProperties)` was removed
* `withQuotaInfo(models.QuotaInfo)` was removed
* `withEncryptionInTransitProperties(models.EncryptionInTransitProperties)` was removed
* `withCreatedDate(java.lang.String)` was removed
* `withConnectivityEndpoints(java.util.List)` was removed
* `withClusterId(java.lang.String)` was removed
* `withExcludedServicesConfig(models.ExcludedServicesConfig)` was removed
* `withClusterDefinition(models.ClusterDefinition)` was removed
* `withErrors(java.util.List)` was removed
* `withComputeProfile(models.ComputeProfile)` was removed
* `validate()` was removed
* `withClusterState(java.lang.String)` was removed
* `withProvisioningState(models.HDInsightClusterProvisioningState)` was removed
* `withSecurityProfile(models.SecurityProfile)` was removed
* `withKafkaRestProperties(models.KafkaRestProperties)` was removed
* `withNetworkProperties(models.NetworkProperties)` was removed
* `withClusterVersion(java.lang.String)` was removed
* `withMinSupportedTlsVersion(java.lang.String)` was removed
* `withTier(models.Tier)` was removed

#### `models.RuntimeScriptAction` was modified

* `validate()` was removed

#### `models.SecurityProfile` was modified

* `validate()` was removed

#### `models.ClusterMonitoringRequest` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

## 1.1.0-beta.3 (2025-10-15)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2025-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.EntraUserInfo` was added

#### `models.UpdateGatewaySettingsParameters` was modified

* `withRestAuthEntraUsers(java.util.List)` was added
* `restAuthEntraUsers()` was added

#### `models.GatewaySettings` was modified

* `restAuthEntraUsers()` was added

## 1.1.0-beta.2 (2024-08-21)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2024-08-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OutboundDependenciesManagedType` was added

* `models.IpTag` was added

#### `models.Cluster$Update` was modified

* `withIdentity(models.ClusterIdentity)` was added

#### `models.ResourceId` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LocalizedName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkProperties` was modified

* `outboundDependenciesManagedType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withOutboundDependenciesManagedType(models.OutboundDependenciesManagedType)` was added
* `publicIpTag()` was added
* `withPublicIpTag(models.IpTag)` was added

#### `models.LinuxOperatingSystemProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Extensions` was modified

* `disableAzureMonitorAgent(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getAzureMonitorAgentStatus(java.lang.String,java.lang.String)` was added
* `enableAzureMonitorAgent(java.lang.String,java.lang.String,models.AzureMonitorRequest)` was added
* `disableAzureMonitorAgent(java.lang.String,java.lang.String)` was added
* `enableAzureMonitorAgent(java.lang.String,java.lang.String,models.AzureMonitorRequest,com.azure.core.util.Context)` was added
* `getAzureMonitorAgentStatusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Role` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OsProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiskEncryptionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SshPublicKey` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KafkaRestProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterCreateParametersExtended` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApplicationGetEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterDiskEncryptionParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Errors` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptActionExecutionSummary` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMonitorTableConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HardwareProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VmSizeProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterResizeParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Usage` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClientGroupInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoscaleSchedule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NameAvailabilityCheckRequestParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterCreateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VersionsCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BillingMeters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiskBillingMeters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Autoscale` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Dimension` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IpConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateGatewaySettingsParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegionsCapability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoscaleTimeAndCapacity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VmSizeCompatibilityFilterV2` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValidationErrorInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExecuteScriptActionParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VersionSpec` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegionalQuotaCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataDisksGroups` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptAction` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageAccount` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BillingResources` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectivityEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutoscaleConfigurationUpdateParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetricSpecifications` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMonitorSelectedConfigurations` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withIdentity(models.ClusterIdentity)` was added
* `identity()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeIsolationProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScriptActionExecutionHistoryList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExcludedServicesConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoscaleRecurrence` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Extension` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApplicationGetHttpsEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterCreateRequestValidationParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AaddsResourceDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EncryptionInTransitProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScriptActionsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutoscaleCapacity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureMonitorRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateClusterIdentityCertificateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterGetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RuntimeScriptAction` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecurityProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterMonitoringRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.1.0-beta.1 (2023-07-20)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2023-04-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.StorageAccount` was modified

* `withEnableSecureChannel(java.lang.Boolean)` was added
* `enableSecureChannel()` was added

## 1.0.0 (2022-11-17)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.Cluster` was modified

* `resourceGroupName()` was added

#### `HDInsightManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `HDInsightManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.5 (2021-10-09)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ClusterIdentityUserAssignedIdentities` was removed

* `models.VmSizeCompatibilityFilter` was removed

* `models.VmSizesCapability` was removed

#### `models.ResourceIdentityType` was modified

* `valueOf(java.lang.String)` was removed
* `models.ResourceIdentityType[] values()` -> `java.util.Collection values()`
* `toString()` was removed

#### `models.AsyncOperationState` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.AsyncOperationState[] values()` -> `java.util.Collection values()`

#### `models.CapabilitiesResult` was modified

* `vmsizes()` was removed
* `vmsizeFilters()` was removed

#### `models.OSType` was modified

* `toString()` was removed
* `models.OSType[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.DaysOfWeek` was modified

* `valueOf(java.lang.String)` was removed
* `models.DaysOfWeek[] values()` -> `java.util.Collection values()`
* `toString()` was removed

#### `models.Tier` was modified

* `valueOf(java.lang.String)` was removed
* `models.Tier[] values()` -> `java.util.Collection values()`
* `toString()` was removed

#### `models.DirectoryType` was modified

* `valueOf(java.lang.String)` was removed
* `toString()` was removed
* `models.DirectoryType[] values()` -> `java.util.Collection values()`

#### `models.HDInsightClusterProvisioningState` was modified

* `toString()` was removed
* `models.HDInsightClusterProvisioningState[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

### Features Added

* `models.ResourceId` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.PrivateEndpoint` was added

* `models.PrivateEndpointConnections` was added

* `models.PrivateLinkResources` was added

* `models.PrivateIpAllocationMethod` was added

* `models.PrivateLinkConfiguration` was added

* `models.IpConfiguration` was added

* `models.PrivateLinkResourceListResult` was added

* `models.PrivateLinkResource` was added

* `models.UserAssignedIdentity` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.PrivateLinkConfigurationProvisioningState` was added

* `models.PrivateLinkServiceConnectionStatus` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateLinkServiceConnectionState` was added

#### `models.Cluster$Definition` was modified

* `withZones(java.util.List)` was added

#### `models.ApplicationProperties` was modified

* `withPrivateLinkConfigurations(java.util.List)` was added
* `privateLinkConfigurations()` was added

#### `models.ClusterCreateParametersExtended` was modified

* `zones()` was added
* `withZones(java.util.List)` was added

#### `models.Cluster` was modified

* `systemData()` was added
* `zones()` was added

#### `models.ClusterCreateProperties` was modified

* `privateLinkConfigurations()` was added
* `withPrivateLinkConfigurations(java.util.List)` was added

#### `models.Application` was modified

* `systemData()` was added

#### `models.VmSizeCompatibilityFilterV2` was modified

* `espApplied()` was added
* `withEspApplied(java.lang.String)` was added
* `withComputeIsolationSupported(java.lang.String)` was added
* `computeIsolationSupported()` was added

#### `models.ClusterCreateRequestValidationParameters` was modified

* `withZones(java.util.List)` was added
* `withZones(java.util.List)` was added

#### `HDInsightManager` was modified

* `privateLinkResources()` was added
* `privateEndpointConnections()` was added

#### `models.ClusterGetProperties` was modified

* `withPrivateLinkConfigurations(java.util.List)` was added
* `privateLinkConfigurations()` was added
* `privateEndpointConnections()` was added

## 1.0.0-beta.4 (2021-08-12)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2018-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `HDInsightManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.3 (2021-05-31)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2018-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### New Feature

* `models.AzureMonitorTableConfiguration` was added

* `models.AzureMonitorResponse` was added

* `models.AzureMonitorSelectedConfigurations` was added

* `models.AzureMonitorRequest` was added

#### `models.Extensions` was modified

* `disableAzureMonitor(java.lang.String,java.lang.String)` was added
* `enableAzureMonitor(java.lang.String,java.lang.String,models.AzureMonitorRequest)` was added
* `enableAzureMonitor(java.lang.String,java.lang.String,models.AzureMonitorRequest,com.azure.core.util.Context)` was added
* `disableAzureMonitor(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getAzureMonitorStatusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getAzureMonitorStatus(java.lang.String,java.lang.String)` was added

## 1.0.0-beta.2 (2021-04-12)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2018-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.Extensions` was modified

* `create(java.lang.String,java.lang.String,java.lang.String,fluent.models.ExtensionInner)` was removed
* `create(java.lang.String,java.lang.String,java.lang.String,fluent.models.ExtensionInner,com.azure.core.util.Context)` was removed
* `models.Extension get(java.lang.String,java.lang.String,java.lang.String)` -> `models.ClusterMonitoringResponse get(java.lang.String,java.lang.String,java.lang.String)`

#### `models.Usage` was modified

* `java.lang.Integer currentValue()` -> `java.lang.Long currentValue()`
* `withCurrentValue(java.lang.Integer)` was removed
* `withLimit(java.lang.Integer)` was removed
* `java.lang.Integer limit()` -> `java.lang.Long limit()`

#### `models.VmSizeCompatibilityFilter` was modified

* `vmsizes()` was removed
* `withVmsizes(java.util.List)` was removed

#### `models.CapabilitiesResult` was modified

* `vmSizeFilters()` was removed
* `vmSizes()` was removed

#### `models.VersionSpec` was modified

* `java.lang.String isDefault()` -> `java.lang.Boolean isDefault()`
* `withIsDefault(java.lang.String)` was removed

#### `models.Extension` was modified

* `innerModel()` was removed
* `java.lang.String workspaceId()` -> `java.lang.String workspaceId()`
* `java.lang.String primaryKey()` -> `java.lang.String primaryKey()`

#### `models.ApplicationGetHttpsEndpoint` was modified

* `withLocation(java.lang.String)` was removed
* `withPublicPort(java.lang.Integer)` was removed

### New Feature

* `models.AsyncOperationResult` was added

* `models.VmSizeProperty` was added

* `models.ServiceSpecification` was added

* `models.NameAvailabilityCheckRequestParameters` was added

* `models.Dimension` was added

* `models.ValidationErrorInfo` was added

* `models.ClusterCreateValidationResult` was added

* `models.MetricSpecifications` was added

* `models.ExcludedServicesConfig` was added

* `models.OperationProperties` was added

* `models.NameAvailabilityCheckResult` was added

* `models.ClusterCreateRequestValidationParameters` was added

* `models.AaddsResourceDetails` was added

* `models.UpdateClusterIdentityCertificateParameters` was added

#### `models.VirtualMachines` was modified

* `getAsyncOperationStatus(java.lang.String,java.lang.String,java.lang.String)` was added
* `getAsyncOperationStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Role` was modified

* `encryptDataDisks()` was added
* `withEncryptDataDisks(java.lang.Boolean)` was added
* `withVMGroupName(java.lang.String)` was added
* `vMGroupName()` was added

#### `models.Extensions` was modified

* `getAzureAsyncOperationStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `create(java.lang.String,java.lang.String,java.lang.String,models.Extension)` was added
* `create(java.lang.String,java.lang.String,java.lang.String,models.Extension,com.azure.core.util.Context)` was added
* `getAzureAsyncOperationStatus(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.KafkaRestProperties` was modified

* `configurationOverride()` was added
* `withConfigurationOverride(java.util.Map)` was added

#### `models.ApplicationGetEndpoint` was modified

* `privateIpAddress()` was added
* `withPrivateIpAddress(java.lang.String)` was added

#### `models.Cluster` was modified

* `rotateDiskEncryptionKey(models.ClusterDiskEncryptionParameters)` was added
* `executeScriptActions(models.ExecuteScriptActionParameters)` was added
* `rotateDiskEncryptionKey(models.ClusterDiskEncryptionParameters,com.azure.core.util.Context)` was added
* `updateIdentityCertificate(models.UpdateClusterIdentityCertificateParameters,com.azure.core.util.Context)` was added
* `updateGatewaySettings(models.UpdateGatewaySettingsParameters,com.azure.core.util.Context)` was added
* `getGatewaySettingsWithResponse(com.azure.core.util.Context)` was added
* `getGatewaySettings()` was added
* `updateGatewaySettings(models.UpdateGatewaySettingsParameters)` was added
* `updateIdentityCertificate(models.UpdateClusterIdentityCertificateParameters)` was added
* `executeScriptActions(models.ExecuteScriptActionParameters,com.azure.core.util.Context)` was added

#### `models.BillingResponseListResult` was modified

* `vmSizesWithEncryptionAtHost()` was added
* `vmSizeProperties()` was added

#### `models.Usage` was modified

* `withCurrentValue(java.lang.Long)` was added
* `withLimit(java.lang.Long)` was added

#### `models.Locations` was modified

* `validateClusterCreateRequestWithResponse(java.lang.String,models.ClusterCreateRequestValidationParameters,com.azure.core.util.Context)` was added
* `checkNameAvailabilityWithResponse(java.lang.String,models.NameAvailabilityCheckRequestParameters,com.azure.core.util.Context)` was added
* `getAzureAsyncOperationStatus(java.lang.String,java.lang.String)` was added
* `checkNameAvailability(java.lang.String,models.NameAvailabilityCheckRequestParameters)` was added
* `getAzureAsyncOperationStatusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `validateClusterCreateRequest(java.lang.String,models.ClusterCreateRequestValidationParameters)` was added

#### `models.ClusterIdentityUserAssignedIdentities` was modified

* `withTenantId(java.lang.String)` was added
* `tenantId()` was added

#### `models.VmSizeCompatibilityFilter` was modified

* `osType()` was added
* `computeIsolationSupported()` was added
* `espApplied()` was added
* `withVMSizes(java.util.List)` was added
* `withOsType(java.util.List)` was added
* `vMSizes()` was added
* `withComputeIsolationSupported(java.lang.String)` was added
* `withEspApplied(java.lang.String)` was added

#### `models.OperationDisplay` was modified

* `withDescription(java.lang.String)` was added
* `description()` was added

#### `models.CapabilitiesResult` was modified

* `vmsizes()` was added
* `vmsizeFilters()` was added

#### `models.VersionSpec` was modified

* `withIsDefault(java.lang.Boolean)` was added

#### `models.StorageAccount` was modified

* `withSaskey(java.lang.String)` was added
* `withFileshare(java.lang.String)` was added
* `fileshare()` was added
* `saskey()` was added

#### `models.ConnectivityEndpoint` was modified

* `privateIpAddress()` was added
* `withPrivateIpAddress(java.lang.String)` was added

#### `models.Operation` was modified

* `properties()` was added

#### `models.Extension` was modified

* `withPrimaryKey(java.lang.String)` was added
* `validate()` was added
* `withWorkspaceId(java.lang.String)` was added

#### `models.ApplicationGetHttpsEndpoint` was modified

* `privateIpAddress()` was added
* `withPrivateIpAddress(java.lang.String)` was added

#### `models.Clusters` was modified

* `getAzureAsyncOperationStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateIdentityCertificate(java.lang.String,java.lang.String,models.UpdateClusterIdentityCertificateParameters)` was added
* `getAzureAsyncOperationStatus(java.lang.String,java.lang.String,java.lang.String)` was added
* `updateIdentityCertificate(java.lang.String,java.lang.String,models.UpdateClusterIdentityCertificateParameters,com.azure.core.util.Context)` was added

#### `models.ScriptActions` was modified

* `getExecutionAsyncOperationStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getExecutionAsyncOperationStatus(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ClusterGetProperties` was modified

* `excludedServicesConfig()` was added
* `storageProfile()` was added
* `withStorageProfile(models.StorageProfile)` was added
* `clusterHdpVersion()` was added
* `withExcludedServicesConfig(models.ExcludedServicesConfig)` was added
* `withClusterHdpVersion(java.lang.String)` was added

#### `models.Applications` was modified

* `getAzureAsyncOperationStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getAzureAsyncOperationStatus(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

## 1.0.0-beta.1 (2020-12-17)

- Azure Resource Manager HDInsight client library for Java. This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2018-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
