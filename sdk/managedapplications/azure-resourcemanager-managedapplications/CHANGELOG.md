# Release History

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

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `createOrUpdateById(java.lang.String,java.lang.String,fluent.models.ApplicationDefinitionInner,com.azure.core.util.Context)` was removed
* `deleteById(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ApplicationDefinition$Definition` was modified

* `withIdentity(models.Identity)` was removed
* `withIsEnabled(java.lang.String)` was removed

#### `models.ApplicationDefinition` was modified

* `identity()` was removed
* `java.lang.String isEnabled()` -> `java.lang.Boolean isEnabled()`

#### `models.ApplicationPatchable` was modified

* `validate()` was removed
* `java.lang.String applicationDefinitionId()` -> `java.lang.String applicationDefinitionId()`
* `models.PlanPatchable plan()` -> `models.PlanPatchable plan()`
* `withLocation(java.lang.String)` was removed
* `withIdentity(models.Identity)` was removed
* `withTags(java.util.Map)` was removed
* `java.lang.String kind()` -> `java.lang.String kind()`
* `withParameters(java.lang.Object)` was removed
* `withKind(java.lang.String)` was removed
* `java.lang.Object parameters()` -> `java.lang.Object parameters()`
* `models.ProvisioningState provisioningState()` -> `models.ProvisioningState provisioningState()`
* `withManagedBy(java.lang.String)` was removed
* `withSku(models.Sku)` was removed
* `java.lang.Object outputs()` -> `java.lang.Object outputs()`
* `java.lang.String managedResourceGroupId()` -> `java.lang.String managedResourceGroupId()`
* `withApplicationDefinitionId(java.lang.String)` was removed
* `withManagedResourceGroupId(java.lang.String)` was removed
* `withPlan(models.PlanPatchable)` was removed

#### `models.ApplicationDefinition$Update` was modified

* `withManagedBy(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withMainTemplate(java.lang.Object)` was removed
* `withIdentity(models.Identity)` was removed
* `withCreateUiDefinition(java.lang.Object)` was removed
* `withAuthorizations(java.util.List)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withSku(models.Sku)` was removed
* `withIsEnabled(java.lang.String)` was removed
* `withPackageFileUri(java.lang.String)` was removed
* `withArtifacts(java.util.List)` was removed
* `withLockLevel(models.ApplicationLockLevel)` was removed

#### `models.Application$Update` was modified

* `withPlan(models.PlanPatchable)` was removed

#### `models.GenericResource` was modified

* `withIdentity(models.Identity)` was removed
* `identity()` was removed

#### `models.OperationDisplay` was modified

* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

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

* `updateAccess(java.lang.String,java.lang.String,fluent.models.UpdateAccessDefinitionInner)` was added
* `refreshPermissions(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateById(java.lang.String,fluent.models.ApplicationPatchableInner,com.azure.core.util.Context)` was added
* `listAllowedUpgradePlans(java.lang.String,java.lang.String)` was added
* `update(java.lang.String,java.lang.String,fluent.models.ApplicationPatchableInner,com.azure.core.util.Context)` was added
* `listAllowedUpgradePlansWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listTokensWithResponse(java.lang.String,java.lang.String,models.ListTokenRequest,com.azure.core.util.Context)` was added
* `updateAccess(java.lang.String,java.lang.String,fluent.models.UpdateAccessDefinitionInner,com.azure.core.util.Context)` was added
* `refreshPermissions(java.lang.String,java.lang.String)` was added
* `update(java.lang.String,java.lang.String)` was added
* `listTokens(java.lang.String,java.lang.String,models.ListTokenRequest)` was added

#### `models.Identity` was modified

* `userAssignedIdentities()` was added
* `withUserAssignedIdentities(java.util.Map)` was added

#### `models.Application$Definition` was modified

* `withJitAccessPolicy(models.ApplicationJitAccessPolicy)` was added

#### `models.Application` was modified

* `listAllowedUpgradePlans()` was added
* `updateAccess(fluent.models.UpdateAccessDefinitionInner,com.azure.core.util.Context)` was added
* `managementMode()` was added
* `createdBy()` was added
* `artifacts()` was added
* `jitAccessPolicy()` was added
* `authorizations()` was added
* `publisherTenantId()` was added
* `customerSupport()` was added
* `supportUrls()` was added
* `updatedBy()` was added
* `billingDetails()` was added
* `listTokensWithResponse(models.ListTokenRequest,com.azure.core.util.Context)` was added
* `refreshPermissions()` was added
* `systemData()` was added
* `listTokens(models.ListTokenRequest)` was added
* `listAllowedUpgradePlansWithResponse(com.azure.core.util.Context)` was added
* `refreshPermissions(com.azure.core.util.Context)` was added
* `updateAccess(fluent.models.UpdateAccessDefinitionInner)` was added

#### `models.ApplicationArtifact` was modified

* `withName(models.ApplicationArtifactName)` was added

#### `models.ApplicationDefinitions` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdateByIdWithResponse(java.lang.String,java.lang.String,fluent.models.ApplicationDefinitionInner,com.azure.core.util.Context)` was added
* `updateById(java.lang.String,java.lang.String,models.ApplicationDefinitionPatchable)` was added
* `updateByIdWithResponse(java.lang.String,java.lang.String,models.ApplicationDefinitionPatchable,com.azure.core.util.Context)` was added
* `list(com.azure.core.util.Context)` was added
* `list()` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Operation` was modified

* `isDataAction()` was added
* `origin()` was added
* `actionType()` was added

#### `models.ApplicationDefinition$Definition` was modified

* `withManagementPolicy(models.ApplicationManagementPolicy)` was added
* `withIsEnabled(java.lang.Boolean)` was added
* `withPolicies(java.util.List)` was added
* `withNotificationPolicy(models.ApplicationNotificationPolicy)` was added
* `withStorageAccountId(java.lang.String)` was added
* `withDeploymentPolicy(models.ApplicationDeploymentPolicy)` was added
* `withLockingPolicy(models.ApplicationPackageLockingPolicyDefinition)` was added

#### `models.ApplicationDefinition` was modified

* `deploymentPolicy()` was added
* `systemData()` was added
* `storageAccountId()` was added
* `policies()` was added
* `lockingPolicy()` was added
* `managementPolicy()` was added
* `notificationPolicy()` was added

#### `ApplicationManager` was modified

* `jitRequests()` was added

#### `models.ApplicationPatchable` was modified

* `customerSupport()` was added
* `publisherTenantId()` was added
* `id()` was added
* `artifacts()` was added
* `authorizations()` was added
* `identity()` was added
* `systemData()` was added
* `name()` was added
* `innerModel()` was added
* `createdBy()` was added
* `sku()` was added
* `managedBy()` was added
* `billingDetails()` was added
* `jitAccessPolicy()` was added
* `tags()` was added
* `updatedBy()` was added
* `managementMode()` was added
* `location()` was added
* `supportUrls()` was added
* `type()` was added

#### `models.Application$Update` was modified

* `withPlan(models.Plan)` was added
* `withJitAccessPolicy(models.ApplicationJitAccessPolicy)` was added

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
