# Release History

## 1.0.0-beta.5 (Unreleased)


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
