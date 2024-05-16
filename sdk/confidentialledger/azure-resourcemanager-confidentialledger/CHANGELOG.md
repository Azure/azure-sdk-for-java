# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
