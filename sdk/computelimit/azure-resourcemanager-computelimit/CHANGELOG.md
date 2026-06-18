# Release History

## 1.2.0 (2026-06-17)

- Azure Resource Manager ComputeLimit client library for Java. This package contains Microsoft Azure SDK for ComputeLimit Management SDK. Microsoft Azure Compute Limit Resource Provider. Package api-version 2026-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SharedLimitCap$DefinitionStages` was added

* `models.MemberCapOverride$UpdateStages` was added

* `models.MemberCapOverride$Update` was added

* `models.MemberCapOverrides` was added

* `models.MemberCapOverride$DefinitionStages` was added

* `models.SetMemberCapOverridesRequest` was added

* `models.MemberCapOverrideProperties` was added

* `models.SharedLimitCap$Update` was added

* `models.MemberCapOverride$Definition` was added

* `models.SharedLimitCap$Definition` was added

* `models.SharedLimitCaps` was added

* `models.SharedLimitCapProperties` was added

* `models.MemberCap` was added

* `models.SetMemberCapOverridesResult` was added

* `models.MemberCapOverride` was added

* `models.SharedLimitCap$UpdateStages` was added

* `models.SharedLimitCap` was added

#### `ComputeLimitManager` was modified

* `sharedLimitCaps()` was added
* `memberCapOverrides()` was added

## 1.1.0 (2026-05-26)

- Azure Resource Manager ComputeLimit client library for Java. This package contains Microsoft Azure SDK for ComputeLimit Management SDK. Microsoft Azure Compute Limit Resource Provider. Package api-version 2026-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Features` was modified

* `enable(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.FeatureEnableRequest` was added

#### `models.Features` was modified

* `enable(java.lang.String,java.lang.String,models.FeatureEnableRequest,com.azure.core.util.Context)` was added

## 1.0.0 (2026-04-21)

- Azure Resource Manager ComputeLimit client library for Java. This package contains Microsoft Azure SDK for ComputeLimit Management SDK. Microsoft Azure Compute Limit Resource Provider. Package api-version 2026-04-30. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OperationStatusResult` was added

* `models.VmFamilyProperties` was added

* `models.VmFamilies` was added

* `models.VmFamily` was added

* `models.FeatureProperties` was added

* `models.Feature` was added

* `models.FeatureState` was added

* `models.Features` was added

#### `ComputeLimitManager` was modified

* `features()` was added
* `vmFamilies()` was added

## 1.0.0-beta.1 (2025-11-12)

- Azure Resource Manager ComputeLimit client library for Java. This package contains Microsoft Azure SDK for ComputeLimit Management SDK. Microsoft Azure Compute Limit Resource Provider. Package api-version 2025-08-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-computelimit Java SDK.

