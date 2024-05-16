# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2024-03-21)

- Azure Resource Manager Confluent client library for Java. This package contains Microsoft Azure SDK for Confluent Management SDK.  Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SCMetadataEntity` was added

* `models.ApiKeyResourceEntity` was added

* `models.SchemaRegistryClusterRecord` was added

* `models.ListSchemaRegistryClustersResponse` was added

* `models.GetEnvironmentsResponse` was added

* `models.ApiKeyRecord` was added

* `models.AccessRoleBindingNameListSuccessResponse` was added

* `models.SchemaRegistryClusterSpecEntity` was added

* `models.ListRegionsSuccessResponse` was added

* `models.SCEnvironmentRecord` was added

* `models.RegionRecord` was added

* `models.CreateApiKeyModel` was added

* `models.SCClusterSpecEntity` was added

* `models.RegionSpecEntity` was added

* `models.ListClustersSuccessResponse` was added

* `models.SchemaRegistryClusterStatusEntity` was added

* `models.SCClusterRecord` was added

* `models.SCClusterByokEntity` was added

* `models.SCClusterNetworkEnvironmentEntity` was added

* `models.ApiKeyOwnerEntity` was added

* `models.SchemaRegistryClusterEnvironmentRegionEntity` was added

* `models.AccessCreateRoleBindingRequestModel` was added

* `models.ApiKeySpecEntity` was added

#### `models.Organizations` was modified

* `listEnvironments(java.lang.String,java.lang.String)` was added
* `deleteClusterApiKeyWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getClusterApiKey(java.lang.String,java.lang.String,java.lang.String)` was added
* `getSchemaRegistryClusterById(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listSchemaRegistryClusters(java.lang.String,java.lang.String,java.lang.String)` was added
* `getSchemaRegistryClusterByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listClusters(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteClusterApiKey(java.lang.String,java.lang.String,java.lang.String)` was added
* `createApiKey(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.CreateApiKeyModel)` was added
* `listEnvironments(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `getClusterByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createApiKeyWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.CreateApiKeyModel,com.azure.core.util.Context)` was added
* `listRegions(java.lang.String,java.lang.String,models.ListAccessRequestModel)` was added
* `listSchemaRegistryClusters(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `getClusterApiKeyWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listRegionsWithResponse(java.lang.String,java.lang.String,models.ListAccessRequestModel,com.azure.core.util.Context)` was added
* `getEnvironmentById(java.lang.String,java.lang.String,java.lang.String)` was added
* `getClusterById(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listClusters(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `getEnvironmentByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Access` was modified

* `createRoleBinding(java.lang.String,java.lang.String,models.AccessCreateRoleBindingRequestModel)` was added
* `listRoleBindingNameList(java.lang.String,java.lang.String,models.ListAccessRequestModel)` was added
* `listRoleBindingNameListWithResponse(java.lang.String,java.lang.String,models.ListAccessRequestModel,com.azure.core.util.Context)` was added
* `deleteRoleBinding(java.lang.String,java.lang.String,java.lang.String)` was added
* `createRoleBindingWithResponse(java.lang.String,java.lang.String,models.AccessCreateRoleBindingRequestModel,com.azure.core.util.Context)` was added
* `deleteRoleBindingWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.OrganizationResource` was modified

* `listRegionsWithResponse(models.ListAccessRequestModel,com.azure.core.util.Context)` was added
* `listRegions(models.ListAccessRequestModel)` was added

## 1.0.0 (2023-11-16)

- Azure Resource Manager Confluent client library for Java. This package contains Microsoft Azure SDK for Confluent Management SDK.  Package tag package-2023-08-22. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.UserRecord` was added

* `models.ClusterByokEntity` was added

* `models.AccessListInvitationsSuccessResponse` was added

* `models.ClusterConfigEntity` was added

* `models.ClusterStatusEntity` was added

* `models.EnvironmentRecord` was added

* `models.ClusterEnvironmentEntity` was added

* `models.ServiceAccountRecord` was added

* `models.ListAccessRequestModel` was added

* `models.LinkOrganization` was added

* `models.Access` was added

* `models.AccessListClusterSuccessResponse` was added

* `models.AccessListUsersSuccessResponse` was added

* `models.ClusterRecord` was added

* `models.ValidationResponse` was added

* `models.ConfluentListMetadata` was added

* `models.MetadataEntity` was added

* `models.InvitationRecord` was added

* `models.AccessInvitedUserDetails` was added

* `models.ClusterSpecEntity` was added

* `models.AccessListServiceAccountsSuccessResponse` was added

* `models.AccessListEnvironmentsSuccessResponse` was added

* `models.ClusterNetworkEntity` was added

* `models.AccessListRoleBindingsSuccessResponse` was added

* `models.AccessInviteUserAccountModel` was added

* `models.RoleBindingRecord` was added

#### `models.OfferDetail` was modified

* `withPrivateOfferId(java.lang.String)` was added
* `privateOfferIds()` was added
* `withTermId(java.lang.String)` was added
* `privateOfferId()` was added
* `termId()` was added
* `withPrivateOfferIds(java.util.List)` was added

#### `models.OrganizationResource$Definition` was modified

* `withLinkOrganization(models.LinkOrganization)` was added

#### `ConfluentManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `ConfluentManager` was modified

* `access()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.OrganizationResource` was modified

* `resourceGroupName()` was added
* `linkOrganization()` was added

#### `models.Validations` was modified

* `validateOrganizationV2WithResponse(java.lang.String,java.lang.String,fluent.models.OrganizationResourceInner,com.azure.core.util.Context)` was added
* `validateOrganizationV2(java.lang.String,java.lang.String,fluent.models.OrganizationResourceInner)` was added

#### `models.UserDetail` was modified

* `aadEmail()` was added
* `withAadEmail(java.lang.String)` was added
* `withUserPrincipalName(java.lang.String)` was added
* `userPrincipalName()` was added

## 1.0.0-beta.3 (2021-11-11)

- Azure Resource Manager Confluent client library for Java. This package contains Microsoft Azure SDK for Confluent Management SDK.  Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `ConfluentManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.2 (2021-05-13)

- Azure Resource Manager Confluent client library for Java. This package contains Microsoft Azure SDK for Confluent Management SDK.  Package tag package-2021-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.OrganizationResourcePropertiesUserDetail` was removed

* `models.OrganizationResourcePropertiesOfferDetail` was removed

* `models.OrganizationResourceProperties` was removed

#### `models.OrganizationResource$DefinitionStages` was modified

* Stage 3, 4 was added

#### `models.OrganizationResource$Definition` was modified

* `withProvisioningState(models.ProvisionState)` was removed

### New Feature

* `models.Validations` was added

#### `ConfluentManager` was modified

* `validations()` was added

#### `models.OrganizationResource` was modified

* `systemData()` was added

#### `models.ConfluentAgreementResource` was modified

* `systemData()` was added

## 1.0.0-beta.1 (2021-01-14)

- Azure Resource Manager Confluent client library for Java. This package contains Microsoft Azure SDK for Confluent Management SDK.  Package tag package-2020-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
