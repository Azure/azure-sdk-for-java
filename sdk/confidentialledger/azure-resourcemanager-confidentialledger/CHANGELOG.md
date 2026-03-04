# Release History

## 1.1.0-beta.2 (2026-03-04)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Microsoft Azure Confidential Compute Ledger Managed CCF Control Plane REST API version 2025-06-10-preview. Package api-version 2025-06-10-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ManagedCcfRestore` was removed

#### `models.ManagedCcf$Update` was removed

#### `models.ManagedCcfBackup` was removed

#### `models.ManagedCcf$UpdateStages` was removed

#### `models.ManagedCcf$Definition` was removed

#### `models.ManagedCcf$DefinitionStages` was removed

#### `models.ManagedCcfs` was removed

#### `models.ManagedCcfRestoreResponse` was removed

#### `models.ManagedCcfProperties` was removed

#### `models.ConfidentialLedgerList` was removed

#### `models.ManagedCcfList` was removed

#### `models.ResourceProviderOperationList` was removed

#### `models.ManagedCcf` was removed

#### `models.ManagedCcfBackupResponse` was removed

#### `models.MemberIdentityCertificate` was modified

* `java.lang.Object tags()` -> `com.azure.core.util.BinaryData tags()`
* `validate()` was removed
* `withTags(java.lang.Object)` was removed

#### `models.AadBasedSecurityPrincipal` was modified

* `validate()` was removed

#### `models.ConfidentialLedgerBackup` was modified

* `validate()` was removed

#### `models.CertBasedSecurityPrincipal` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.LedgerProperties` was modified

* `withEnclavePlatform(models.EnclavePlatform)` was removed
* `validate()` was removed

#### `ConfidentialLedgerManager` was modified

* `managedCcfs()` was removed

#### `models.ConfidentialLedgerRestore` was modified

* `validate()` was removed

#### `models.ResourceProviderOperationDisplay` was modified

* `ResourceProviderOperationDisplay()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withResource(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed

#### `models.DeploymentType` was modified

* `validate()` was removed

### Features Added

* `models.ManagedCCF$Definition` was added

* `models.ManagedCCF$DefinitionStages` was added

* `models.ManagedCCFBackup` was added

* `models.ManagedCCFs` was added

* `models.ManagedCCFRestoreResponse` was added

* `models.ManagedCCF$UpdateStages` was added

* `models.ManagedCCFBackupResponse` was added

* `models.ManagedCCFRestore` was added

* `models.ManagedCCF` was added

* `models.ManagedCCF$Update` was added

* `models.ManagedCCFProperties` was added

#### `models.MemberIdentityCertificate` was modified

* `withTags(com.azure.core.util.BinaryData)` was added

#### `models.LedgerProperties` was modified

* `withScittConfiguration(java.lang.String)` was added
* `scittConfiguration()` was added

#### `ConfidentialLedgerManager` was modified

* `managedCCFs()` was added

## 1.1.0-beta.1 (2025-05-15)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Microsoft Azure Confidential Compute Ledger Control Plane REST API version 2020-12-01-preview. Package tag package-preview-2024-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ResourceLocation` was removed

#### `models.Tags` was removed

### Features Added

* `models.MemberIdentityCertificate` was added

* `models.ManagedCcfRestore` was added

* `models.LanguageRuntime` was added

* `models.ManagedCcf$Update` was added

* `models.ManagedCcfBackup` was added

* `models.ConfidentialLedgerBackup` was added

* `models.ManagedCcf$UpdateStages` was added

* `models.ManagedCcf$Definition` was added

* `models.ManagedCcf$DefinitionStages` was added

* `models.ManagedCcfs` was added

* `models.EnclavePlatform` was added

* `models.ApplicationType` was added

* `models.ConfidentialLedgerBackupResponse` was added

* `models.ManagedCcfRestoreResponse` was added

* `models.ManagedCcfProperties` was added

* `models.ManagedCcfList` was added

* `models.ManagedCcf` was added

* `models.ConfidentialLedgerRestore` was added

* `models.ManagedCcfBackupResponse` was added

* `models.RunningState` was added

* `models.DeploymentType` was added

* `models.ConfidentialLedgerRestoreResponse` was added

* `models.LedgerSku` was added

#### `models.LedgerProperties` was modified

* `withRunningState(models.RunningState)` was added
* `enclavePlatform()` was added
* `withWriteLBAddressPrefix(java.lang.String)` was added
* `hostLevel()` was added
* `withSubjectName(java.lang.String)` was added
* `withHostLevel(java.lang.String)` was added
* `applicationType()` was added
* `nodeCount()` was added
* `withApplicationType(models.ApplicationType)` was added
* `withNodeCount(java.lang.Integer)` was added
* `withEnclavePlatform(models.EnclavePlatform)` was added
* `subjectName()` was added
* `ledgerSku()` was added
* `maxBodySizeInMb()` was added
* `workerThreads()` was added
* `writeLBAddressPrefix()` was added
* `withWorkerThreads(java.lang.Integer)` was added
* `runningState()` was added
* `withLedgerSku(models.LedgerSku)` was added
* `withMaxBodySizeInMb(java.lang.Integer)` was added

#### `ConfidentialLedgerManager` was modified

* `managedCcfs()` was added

#### `models.ConfidentialLedger` was modified

* `backup(models.ConfidentialLedgerBackup)` was added
* `backup(models.ConfidentialLedgerBackup,com.azure.core.util.Context)` was added
* `restore(models.ConfidentialLedgerRestore)` was added
* `restore(models.ConfidentialLedgerRestore,com.azure.core.util.Context)` was added

#### `models.Ledgers` was modified

* `backup(java.lang.String,java.lang.String,models.ConfidentialLedgerBackup)` was added
* `backup(java.lang.String,java.lang.String,models.ConfidentialLedgerBackup,com.azure.core.util.Context)` was added
* `restore(java.lang.String,java.lang.String,models.ConfidentialLedgerRestore,com.azure.core.util.Context)` was added
* `restore(java.lang.String,java.lang.String,models.ConfidentialLedgerRestore)` was added

## 1.0.0 (2024-12-26)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Package tag package-2022-05-13. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager ConfidentialLedger client library for Java.

## 1.0.0-beta.5 (2024-12-04)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Microsoft Azure Confidential Compute Ledger Control Plane REST API version 2020-12-01-preview. Package tag package-preview-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0-beta.4 (2024-04-23)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Microsoft Azure Confidential Compute Ledger Control Plane REST API version 2020-12-01-preview. Package tag package-preview-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ManagedCcfs` was modified

* `update(java.lang.String,java.lang.String,fluent.models.ManagedCcfInner,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,fluent.models.ManagedCcfInner)` was removed

### Features Added

* `models.ManagedCcfRestore` was added

* `models.ManagedCcf$Update` was added

* `models.ManagedCcfBackup` was added

* `models.ConfidentialLedgerBackup` was added

* `models.ManagedCcf$UpdateStages` was added

* `models.ConfidentialLedgerBackupResponse` was added

* `models.ManagedCcfRestoreResponse` was added

* `models.ConfidentialLedgerRestore` was added

* `models.ManagedCcfBackupResponse` was added

* `models.ConfidentialLedgerRestoreResponse` was added

* `models.LedgerSku` was added

#### `models.ManagedCcfs` was modified

* `backup(java.lang.String,java.lang.String,models.ManagedCcfBackup)` was added
* `restore(java.lang.String,java.lang.String,models.ManagedCcfRestore,com.azure.core.util.Context)` was added
* `backup(java.lang.String,java.lang.String,models.ManagedCcfBackup,com.azure.core.util.Context)` was added
* `restore(java.lang.String,java.lang.String,models.ManagedCcfRestore)` was added

#### `models.LedgerProperties` was modified

* `ledgerSku()` was added
* `withLedgerSku(models.LedgerSku)` was added

#### `models.ManagedCcfProperties` was modified

* `withRunningState(models.RunningState)` was added
* `runningState()` was added

#### `models.ConfidentialLedger` was modified

* `backup(models.ConfidentialLedgerBackup,com.azure.core.util.Context)` was added
* `backup(models.ConfidentialLedgerBackup)` was added
* `restore(models.ConfidentialLedgerRestore,com.azure.core.util.Context)` was added
* `restore(models.ConfidentialLedgerRestore)` was added

#### `models.ManagedCcf` was modified

* `restore(models.ManagedCcfRestore,com.azure.core.util.Context)` was added
* `backup(models.ManagedCcfBackup)` was added
* `resourceGroupName()` was added
* `backup(models.ManagedCcfBackup,com.azure.core.util.Context)` was added
* `restore(models.ManagedCcfRestore)` was added
* `update()` was added

#### `models.Ledgers` was modified

* `restore(java.lang.String,java.lang.String,models.ConfidentialLedgerRestore,com.azure.core.util.Context)` was added
* `backup(java.lang.String,java.lang.String,models.ConfidentialLedgerBackup)` was added
* `restore(java.lang.String,java.lang.String,models.ConfidentialLedgerRestore)` was added
* `backup(java.lang.String,java.lang.String,models.ConfidentialLedgerBackup,com.azure.core.util.Context)` was added

## 1.0.0-beta.3 (2023-04-20)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Microsoft Azure Confidential Compute Ledger Control Plane REST API version 2020-12-01-preview. Package tag package-preview-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Tags` was removed

#### `models.ConfidentialLedger` was modified

* `runningState()` was removed

#### `models.ConfidentialLedger$Update` was modified

* `withRunningState(models.RunningState)` was removed

#### `models.ConfidentialLedger$Definition` was modified

* `withRunningState(models.RunningState)` was removed

### Features Added

#### `models.LedgerProperties` was modified

* `withRunningState(models.RunningState)` was added
* `runningState()` was added

## 1.0.0-beta.2 (2023-04-17)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Microsoft Azure Confidential Compute Ledger Control Plane REST API version 2020-12-01-preview. Package tag package-preview-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ResourceLocation` was removed

### Features Added

* `models.MemberIdentityCertificate` was added

* `models.LanguageRuntime` was added

* `models.ManagedCcf$Definition` was added

* `models.ManagedCcf$DefinitionStages` was added

* `models.ManagedCcfs` was added

* `models.ManagedCcfProperties` was added

* `models.ManagedCcfList` was added

* `models.ManagedCcf` was added

* `models.RunningState` was added

* `models.DeploymentType` was added

#### `ConfidentialLedgerManager` was modified

* `managedCcfs()` was added

#### `models.ConfidentialLedger` was modified

* `runningState()` was added

#### `models.ConfidentialLedger$Update` was modified

* `withRunningState(models.RunningState)` was added

#### `models.ConfidentialLedger$Definition` was modified

* `withRunningState(models.RunningState)` was added

## 1.0.0-beta.1 (2022-05-20)

- Azure Resource Manager ConfidentialLedger client library for Java. This package contains Microsoft Azure SDK for ConfidentialLedger Management SDK. Microsoft Azure Confidential Compute Ledger Control Plane REST API version 2020-12-01-preview. Package tag package-2022-05-13. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
