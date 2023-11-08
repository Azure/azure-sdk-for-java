# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2023-09-21)

- Azure Resource Manager Application client library for Java. This package contains Microsoft Azure SDK for Application Management SDK. ARM applications. Package tag package-managedapplications-2021-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorResponse` was removed

* `models.ErrorResponseException` was removed

* `models.ApplicationProviderAuthorization` was removed

#### `models.Applications` was modified

* `updateByIdWithResponse(java.lang.String,fluent.models.ApplicationInner,com.azure.core.util.Context)` was removed
* `models.Application updateById(java.lang.String)` -> `models.ApplicationPatchable updateById(java.lang.String)`

#### `models.OperationListResult` was modified

* `withNextLink(java.lang.String)` was removed
* `withValue(java.util.List)` was removed

#### `models.ApplicationArtifact` was modified

* `withName(java.lang.String)` was removed
* `java.lang.String name()` -> `models.ApplicationArtifactName name()`

#### `models.ApplicationDefinitions` was modified

* `deleteById(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `createOrUpdateById(java.lang.String,java.lang.String,fluent.models.ApplicationDefinitionInner,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ApplicationDefinition$Definition` was modified

* `withIsEnabled(java.lang.String)` was removed
* `withIdentity(models.Identity)` was removed

#### `models.ApplicationDefinition` was modified

* `java.lang.String isEnabled()` -> `java.lang.Boolean isEnabled()`
* `identity()` was removed

#### `models.ApplicationPatchable` was modified

* `withSku(models.Sku)` was removed
* `validate()` was removed
* `java.lang.String applicationDefinitionId()` -> `java.lang.String applicationDefinitionId()`
* `models.PlanPatchable plan()` -> `models.PlanPatchable plan()`
* `java.lang.Object parameters()` -> `java.lang.Object parameters()`
* `withApplicationDefinitionId(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withKind(java.lang.String)` was removed
* `java.lang.Object outputs()` -> `java.lang.Object outputs()`
* `withIdentity(models.Identity)` was removed
* `withLocation(java.lang.String)` was removed
* `models.ProvisioningState provisioningState()` -> `models.ProvisioningState provisioningState()`
* `withManagedResourceGroupId(java.lang.String)` was removed
* `withPlan(models.PlanPatchable)` was removed
* `withManagedBy(java.lang.String)` was removed
* `java.lang.String managedResourceGroupId()` -> `java.lang.String managedResourceGroupId()`
* `java.lang.String kind()` -> `java.lang.String kind()`
* `withParameters(java.lang.Object)` was removed

#### `models.ApplicationDefinition$Update` was modified

* `withAuthorizations(java.util.List)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withSku(models.Sku)` was removed
* `withPackageFileUri(java.lang.String)` was removed
* `withMainTemplate(java.lang.Object)` was removed
* `withLockLevel(models.ApplicationLockLevel)` was removed
* `withIsEnabled(java.lang.String)` was removed
* `withManagedBy(java.lang.String)` was removed
* `withArtifacts(java.util.List)` was removed
* `withIdentity(models.Identity)` was removed
* `withCreateUiDefinition(java.lang.Object)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.Application$Update` was modified

* `withPlan(models.PlanPatchable)` was removed

#### `models.GenericResource` was modified

* `withIdentity(models.Identity)` was removed
* `identity()` was removed

#### `models.OperationDisplay` was modified

* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed

### Features Added

* `models.JitApproverDefinition` was added

* `models.Substatus` was added

* `models.JitSchedulingPolicy` was added

* `models.ApplicationDeploymentPolicy` was added

* `models.JitRequestPatchable` was added

* `models.ApplicationBillingDetailsDefinition` was added

* `models.Origin` was added

* `models.ApplicationClientDetails` was added

* `models.ApplicationAuthorization` was added

* `models.JitApprovalMode` was added

* `models.Status` was added

* `models.JitSchedulingType` was added

* `models.ApplicationNotificationPolicy` was added

* `models.ApplicationPolicy` was added

* `models.JitRequestState` was added

* `models.ApplicationDefinitionPatchable` was added

* `models.UpdateAccessDefinition` was added

* `models.ApplicationNotificationEndpoint` was added

* `models.JitRequestDefinitionListResult` was added

* `models.JitApproverType` was added

* `models.ApplicationArtifactName` was added

* `models.ApplicationManagementPolicy` was added

* `models.ActionType` was added

* `models.ApplicationPackageLockingPolicyDefinition` was added

* `models.JitRequestDefinition$Definition` was added

* `models.DeploymentMode` was added

* `models.UserAssignedResourceIdentity` was added

* `models.JitRequestDefinition$DefinitionStages` was added

* `models.ApplicationManagementMode` was added

* `models.ManagedIdentityTokenResult` was added

* `models.ListTokenRequest` was added

* `models.ApplicationPackageContact` was added

* `models.JitAuthorizationPolicies` was added

* `models.ApplicationPackageSupportUrls` was added

* `models.ApplicationDefinitionArtifact` was added

* `models.JitRequestDefinition$Update` was added

* `models.ApplicationDefinitionArtifactName` was added

* `models.ApplicationJitAccessPolicy` was added

* `models.JitRequestDefinition$UpdateStages` was added

* `models.JitRequestDefinition` was added

* `models.JitRequestMetadata` was added

* `models.JitRequests` was added

* `models.ManagedIdentityToken` was added

* `models.AllowedUpgradePlansResult` was added

#### `models.Applications` was modified

* `refreshPermissions(java.lang.String,java.lang.String)` was added
* `updateAccess(java.lang.String,java.lang.String,fluent.models.UpdateAccessDefinitionInner,com.azure.core.util.Context)` was added
* `refreshPermissions(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateAccess(java.lang.String,java.lang.String,fluent.models.UpdateAccessDefinitionInner)` was added
* `updateById(java.lang.String,fluent.models.ApplicationPatchableInner,com.azure.core.util.Context)` was added
* `listAllowedUpgradePlansWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listTokensWithResponse(java.lang.String,java.lang.String,models.ListTokenRequest,com.azure.core.util.Context)` was added
* `listTokens(java.lang.String,java.lang.String,models.ListTokenRequest)` was added
* `update(java.lang.String,java.lang.String)` was added
* `listAllowedUpgradePlans(java.lang.String,java.lang.String)` was added
* `update(java.lang.String,java.lang.String,fluent.models.ApplicationPatchableInner,com.azure.core.util.Context)` was added

#### `models.Identity` was modified

* `withUserAssignedIdentities(java.util.Map)` was added
* `userAssignedIdentities()` was added

#### `models.Application$Definition` was modified

* `withJitAccessPolicy(models.ApplicationJitAccessPolicy)` was added

#### `models.Application` was modified

* `supportUrls()` was added
* `listTokens(models.ListTokenRequest)` was added
* `updateAccess(fluent.models.UpdateAccessDefinitionInner,com.azure.core.util.Context)` was added
* `listAllowedUpgradePlansWithResponse(com.azure.core.util.Context)` was added
* `managementMode()` was added
* `artifacts()` was added
* `customerSupport()` was added
* `createdBy()` was added
* `refreshPermissions(com.azure.core.util.Context)` was added
* `authorizations()` was added
* `billingDetails()` was added
* `listTokensWithResponse(models.ListTokenRequest,com.azure.core.util.Context)` was added
* `jitAccessPolicy()` was added
* `publisherTenantId()` was added
* `systemData()` was added
* `updateAccess(fluent.models.UpdateAccessDefinitionInner)` was added
* `updatedBy()` was added
* `refreshPermissions()` was added
* `listAllowedUpgradePlans()` was added

#### `models.ApplicationArtifact` was modified

* `withName(models.ApplicationArtifactName)` was added

#### `models.ApplicationDefinitions` was modified

* `updateById(java.lang.String,java.lang.String,models.ApplicationDefinitionPatchable)` was added
* `createOrUpdateByIdWithResponse(java.lang.String,java.lang.String,fluent.models.ApplicationDefinitionInner,com.azure.core.util.Context)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list()` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(com.azure.core.util.Context)` was added
* `updateByIdWithResponse(java.lang.String,java.lang.String,models.ApplicationDefinitionPatchable,com.azure.core.util.Context)` was added

#### `models.Operation` was modified

* `isDataAction()` was added
* `origin()` was added
* `actionType()` was added

#### `models.ApplicationDefinition$Definition` was modified

* `withLockingPolicy(models.ApplicationPackageLockingPolicyDefinition)` was added
* `withPolicies(java.util.List)` was added
* `withNotificationPolicy(models.ApplicationNotificationPolicy)` was added
* `withIsEnabled(java.lang.Boolean)` was added
* `withStorageAccountId(java.lang.String)` was added
* `withDeploymentPolicy(models.ApplicationDeploymentPolicy)` was added
* `withManagementPolicy(models.ApplicationManagementPolicy)` was added

#### `models.ApplicationDefinition` was modified

* `deploymentPolicy()` was added
* `storageAccountId()` was added
* `systemData()` was added
* `notificationPolicy()` was added
* `policies()` was added
* `managementPolicy()` was added
* `lockingPolicy()` was added

#### `ApplicationManager` was modified

* `jitRequests()` was added

#### `models.ApplicationPatchable` was modified

* `updatedBy()` was added
* `publisherTenantId()` was added
* `identity()` was added
* `authorizations()` was added
* `id()` was added
* `systemData()` was added
* `sku()` was added
* `managementMode()` was added
* `location()` was added
* `billingDetails()` was added
* `tags()` was added
* `jitAccessPolicy()` was added
* `customerSupport()` was added
* `name()` was added
* `supportUrls()` was added
* `createdBy()` was added
* `managedBy()` was added
* `type()` was added
* `artifacts()` was added
* `innerModel()` was added

#### `models.Application$Update` was modified

* `withJitAccessPolicy(models.ApplicationJitAccessPolicy)` was added
* `withPlan(models.Plan)` was added

#### `models.GenericResource` was modified

* `systemData()` was added

#### `models.OperationDisplay` was modified

* `description()` was added

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager Application client library for Java. This package contains Microsoft Azure SDK for Application Management SDK. ARM applications. Package tag package-managedapplications-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ApplicationDefinition` was modified

* `resourceGroupName()` was added

#### `models.Application` was modified

* `resourceGroupName()` was added

#### `ApplicationManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `ApplicationManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-26)

- Azure Resource Manager Application client library for Java. This package contains Microsoft Azure SDK for Application Management SDK. ARM applications. Package tag package-managedapplications-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
