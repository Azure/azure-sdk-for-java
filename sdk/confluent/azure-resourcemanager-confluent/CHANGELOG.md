# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
