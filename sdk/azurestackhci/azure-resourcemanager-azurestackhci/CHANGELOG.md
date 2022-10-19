# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2022-05-25)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-2022-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Clusters` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

### Features Added

* `models.UploadCertificateRequest` was added

* `models.ClusterIdentityResponse` was added

* `models.ArcSetting$UpdateStages` was added

* `models.ArcSettingsPatch` was added

* `models.ArcSetting$Update` was added

* `models.ArcIdentityResponse` was added

* `models.PasswordCredential` was added

* `models.RawCertificateData` was added

#### `models.Extension` was modified

* `resourceGroupName()` was added

#### `models.Cluster` was modified

* `resourceGroupName()` was added
* `serviceEndpoint()` was added
* `aadServicePrincipalObjectId()` was added
* `aadApplicationObjectId()` was added
* `createIdentity(com.azure.core.util.Context)` was added
* `uploadCertificate(models.UploadCertificateRequest,com.azure.core.util.Context)` was added
* `uploadCertificate(models.UploadCertificateRequest)` was added
* `createIdentity()` was added

#### `models.ArcSetting$Definition` was modified

* `withArcApplicationClientId(java.lang.String)` was added
* `withArcServicePrincipalObjectId(java.lang.String)` was added
* `withConnectivityProperties(java.lang.Object)` was added
* `withArcApplicationObjectId(java.lang.String)` was added
* `withArcApplicationTenantId(java.lang.String)` was added

#### `models.Clusters` was modified

* `createIdentity(java.lang.String,java.lang.String)` was added
* `uploadCertificate(java.lang.String,java.lang.String,models.UploadCertificateRequest)` was added
* `uploadCertificate(java.lang.String,java.lang.String,models.UploadCertificateRequest,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createIdentity(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ArcSetting` was modified

* `arcServicePrincipalObjectId()` was added
* `createIdentity(com.azure.core.util.Context)` was added
* `generatePassword()` was added
* `resourceGroupName()` was added
* `arcApplicationTenantId()` was added
* `createIdentity()` was added
* `connectivityProperties()` was added
* `arcApplicationClientId()` was added
* `update()` was added
* `arcApplicationObjectId()` was added
* `generatePasswordWithResponse(com.azure.core.util.Context)` was added

#### `models.Cluster$Definition` was modified

* `withAadApplicationObjectId(java.lang.String)` was added
* `withAadServicePrincipalObjectId(java.lang.String)` was added

#### `models.ArcSettings` was modified

* `generatePassword(java.lang.String,java.lang.String,java.lang.String)` was added
* `createIdentity(java.lang.String,java.lang.String,java.lang.String)` was added
* `createIdentity(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `generatePasswordWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.2 (2022-04-12)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AvailableOperations` was removed

* `models.OperationDetail` was removed

* `models.CreatedByType` was removed

* `models.ClusterUpdate` was removed

#### `models.Cluster` was modified

* `lastModifiedBy()` was removed
* `createdByType()` was removed
* `createdBy()` was removed
* `lastModifiedByType()` was removed
* `createdAt()` was removed
* `lastModifiedAt()` was removed

#### `models.OperationDisplay` was modified

* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.Operations` was modified

* `models.AvailableOperations list()` -> `models.OperationListResult list()`

#### `models.Cluster$Definition` was modified

* `withCreatedBy(java.lang.String)` was removed
* `withCreatedByType(models.CreatedByType)` was removed
* `withReportedProperties(models.ClusterReportedProperties)` was removed
* `withLastModifiedBy(java.lang.String)` was removed
* `withLastModifiedByType(models.CreatedByType)` was removed
* `withCreatedAt(java.time.OffsetDateTime)` was removed
* `withLastModifiedAt(java.time.OffsetDateTime)` was removed

### Features Added

* `models.Extension` was added

* `models.DiagnosticLevel` was added

* `models.WindowsServerSubscription` was added

* `models.Operation` was added

* `models.ClusterDesiredProperties` was added

* `models.Extension$DefinitionStages` was added

* `models.Extension$UpdateStages` was added

* `models.NodeExtensionState` was added

* `models.ArcSetting$DefinitionStages` was added

* `models.ClusterPatch` was added

* `models.ArcSetting$Definition` was added

* `models.ActionType` was added

* `models.PerNodeState` was added

* `models.OperationListResult` was added

* `models.Extension$Definition` was added

* `models.ExtensionList` was added

* `models.ArcSetting` was added

* `models.ArcSettingAggregateState` was added

* `models.Extension$Update` was added

* `models.PerNodeExtensionState` was added

* `models.Origin` was added

* `models.NodeArcState` was added

* `models.ArcSettings` was added

* `models.ImdsAttestation` was added

* `models.Extensions` was added

* `models.ExtensionAggregateState` was added

* `models.ArcSettingList` was added

#### `models.Cluster$Update` was modified

* `withCloudManagementEndpoint(java.lang.String)` was added
* `withDesiredProperties(models.ClusterDesiredProperties)` was added
* `withAadClientId(java.lang.String)` was added
* `withAadTenantId(java.lang.String)` was added

#### `AzureStackHciManager` was modified

* `arcSettings()` was added
* `extensions()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Cluster` was modified

* `cloudManagementEndpoint()` was added
* `desiredProperties()` was added
* `systemData()` was added

#### `models.ClusterNode` was modified

* `windowsServerSubscription()` was added

#### `models.ClusterReportedProperties` was modified

* `diagnosticLevel()` was added
* `imdsAttestation()` was added
* `withDiagnosticLevel(models.DiagnosticLevel)` was added

#### `models.Cluster$Definition` was modified

* `withDesiredProperties(models.ClusterDesiredProperties)` was added
* `withCloudManagementEndpoint(java.lang.String)` was added

#### `AzureStackHciManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager AzureStackHci client library for Java. This package contains Microsoft Azure SDK for AzureStackHci Management SDK. Azure Stack HCI management service. Package tag package-2020-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
