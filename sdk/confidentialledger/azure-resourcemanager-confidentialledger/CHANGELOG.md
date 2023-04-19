# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
