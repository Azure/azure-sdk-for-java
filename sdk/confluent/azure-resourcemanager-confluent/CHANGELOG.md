# Release History

## 1.3.0-beta.1 (2026-02-10)

- Azure Resource Manager Confluent client library for Java. This package contains Microsoft Azure SDK for Confluent Management SDK.  Package api-version 2025-08-18-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.OrganizationResourceListResult` was removed

#### `models.ListSchemaRegistryClustersResponse` was removed

#### `models.GetEnvironmentsResponse` was removed

#### `models.ConfluentAgreementResourceListResponse` was removed

#### `models.ListClustersSuccessResponse` was removed

#### `models.SCMetadataEntity` was modified

* `validate()` was removed

#### `models.ApiKeyResourceEntity` was modified

* `ApiKeyResourceEntity()` was changed to private access
* `withKind(java.lang.String)` was removed
* `withEnvironment(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withRelated(java.lang.String)` was removed
* `withResourceName(java.lang.String)` was removed
* `validate()` was removed

#### `models.UserRecord` was modified

* `UserRecord()` was changed to private access
* `validate()` was removed
* `withFullName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withKind(java.lang.String)` was removed
* `withAuthType(java.lang.String)` was removed
* `withMetadata(models.MetadataEntity)` was removed
* `withEmail(java.lang.String)` was removed

#### `models.ClusterByokEntity` was modified

* `ClusterByokEntity()` was changed to private access
* `validate()` was removed
* `withId(java.lang.String)` was removed
* `withRelated(java.lang.String)` was removed
* `withResourceName(java.lang.String)` was removed

#### `models.OrganizationResourceUpdate` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `validate()` was removed
* `withDescription(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.SchemaRegistryClusterSpecEntity` was modified

* `SchemaRegistryClusterSpecEntity()` was changed to private access
* `withRegion(models.SchemaRegistryClusterEnvironmentRegionEntity)` was removed
* `withCloud(java.lang.String)` was removed
* `withEnvironment(models.SchemaRegistryClusterEnvironmentRegionEntity)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withPackageProperty(java.lang.String)` was removed
* `withHttpEndpoint(java.lang.String)` was removed

#### `models.ClusterStatusEntity` was modified

* `validate()` was removed

#### `models.ClusterEnvironmentEntity` was modified

* `ClusterEnvironmentEntity()` was changed to private access
* `withResourceName(java.lang.String)` was removed
* `withRelated(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `validate()` was removed
* `withEnvironment(java.lang.String)` was removed

#### `models.ServiceAccountRecord` was modified

* `ServiceAccountRecord()` was changed to private access
* `withKind(java.lang.String)` was removed
* `withMetadata(models.MetadataEntity)` was removed
* `withDescription(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `validate()` was removed

#### `models.CreateApiKeyModel` was modified

* `validate()` was removed

#### `models.SCClusterSpecEntity` was modified

* `validate()` was removed

#### `models.LinkOrganization` was modified

* `validate()` was removed

#### `models.SchemaRegistryClusterStatusEntity` was modified

* `SchemaRegistryClusterStatusEntity()` was changed to private access
* `withPhase(java.lang.String)` was removed
* `validate()` was removed

#### `models.SCClusterByokEntity` was modified

* `validate()` was removed

#### `models.ClusterRecord` was modified

* `ClusterRecord()` was changed to private access
* `withKind(java.lang.String)` was removed
* `validate()` was removed
* `withSpec(models.ClusterSpecEntity)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withStatus(models.ClusterStatusEntity)` was removed
* `withMetadata(models.MetadataEntity)` was removed
* `withId(java.lang.String)` was removed

#### `models.ClusterSpecEntity` was modified

* `ClusterSpecEntity()` was changed to private access
* `withByok(models.ClusterByokEntity)` was removed
* `withRegion(java.lang.String)` was removed
* `withZone(java.lang.String)` was removed
* `withApiEndpoint(java.lang.String)` was removed
* `withKafkaBootstrapEndpoint(java.lang.String)` was removed
* `withConfig(models.ClusterConfigEntity)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withAvailability(java.lang.String)` was removed
* `withEnvironment(models.ClusterEnvironmentEntity)` was removed
* `withHttpEndpoint(java.lang.String)` was removed
* `validate()` was removed
* `withNetwork(models.ClusterNetworkEntity)` was removed
* `withCloud(java.lang.String)` was removed

#### `models.UserDetail` was modified

* `validate()` was removed

#### `models.AccessCreateRoleBindingRequestModel` was modified

* `validate()` was removed

#### `models.AccessInviteUserAccountModel` was modified

* `validate()` was removed

#### `models.ApiKeySpecEntity` was modified

* `ApiKeySpecEntity()` was changed to private access
* `withOwner(models.ApiKeyOwnerEntity)` was removed
* `withDescription(java.lang.String)` was removed
* `withSecret(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withResource(models.ApiKeyResourceEntity)` was removed
* `validate()` was removed

#### `models.RoleBindingRecord` was modified

* `RoleBindingRecord()` was removed
* `withPrincipal(java.lang.String)` was removed
* `withMetadata(models.MetadataEntity)` was removed
* `java.lang.String principal()` -> `java.lang.String principal()`
* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String roleName()` -> `java.lang.String roleName()`
* `java.lang.String kind()` -> `java.lang.String kind()`
* `withCrnPattern(java.lang.String)` was removed
* `withKind(java.lang.String)` was removed
* `models.MetadataEntity metadata()` -> `models.MetadataEntity metadata()`
* `java.lang.String crnPattern()` -> `java.lang.String crnPattern()`
* `java.lang.String id()` -> `java.lang.String id()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `validate()` was removed
* `withRoleName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.OfferDetail` was modified

* `validate()` was removed

#### `models.ClusterConfigEntity` was modified

* `validate()` was removed

#### `models.EnvironmentRecord` was modified

* `EnvironmentRecord()` was changed to private access
* `withKind(java.lang.String)` was removed
* `withMetadata(models.MetadataEntity)` was removed
* `withId(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `validate()` was removed

#### `models.RegionRecord` was modified

* `RegionRecord()` was changed to private access
* `withKind(java.lang.String)` was removed
* `withMetadata(models.SCMetadataEntity)` was removed
* `withId(java.lang.String)` was removed
* `withSpec(models.RegionSpecEntity)` was removed
* `validate()` was removed

#### `models.RegionSpecEntity` was modified

* `RegionSpecEntity()` was changed to private access
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withCloud(java.lang.String)` was removed
* `withRegionName(java.lang.String)` was removed
* `withPackages(java.util.List)` was removed

#### `models.ListAccessRequestModel` was modified

* `validate()` was removed

#### `models.SCClusterNetworkEnvironmentEntity` was modified

* `validate()` was removed

#### `models.ConfluentListMetadata` was modified

* `ConfluentListMetadata()` was changed to private access
* `withFirst(java.lang.String)` was removed
* `withTotalSize(java.lang.Integer)` was removed
* `withPrev(java.lang.String)` was removed
* `validate()` was removed
* `withLast(java.lang.String)` was removed
* `withNext(java.lang.String)` was removed

#### `models.MetadataEntity` was modified

* `MetadataEntity()` was changed to private access
* `validate()` was removed
* `withDeletedAt(java.lang.String)` was removed
* `withCreatedAt(java.lang.String)` was removed
* `withUpdatedAt(java.lang.String)` was removed
* `withSelf(java.lang.String)` was removed
* `withResourceName(java.lang.String)` was removed

#### `models.AccessInvitedUserDetails` was modified

* `validate()` was removed

#### `models.ApiKeyOwnerEntity` was modified

* `ApiKeyOwnerEntity()` was changed to private access
* `withResourceName(java.lang.String)` was removed
* `validate()` was removed
* `withKind(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withRelated(java.lang.String)` was removed

#### `models.ClusterNetworkEntity` was modified

* `ClusterNetworkEntity()` was changed to private access
* `withResourceName(java.lang.String)` was removed
* `validate()` was removed
* `withEnvironment(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withRelated(java.lang.String)` was removed

#### `models.SchemaRegistryClusterEnvironmentRegionEntity` was modified

* `SchemaRegistryClusterEnvironmentRegionEntity()` was changed to private access
* `withResourceName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `validate()` was removed
* `withRelated(java.lang.String)` was removed

### Features Added

* `models.ConnectorStatus` was added

* `models.DataFormatType` was added

* `models.Clusters` was added

* `models.ConnectorResource` was added

* `models.KafkaAzureBlobStorageSourceConnectorInfo` was added

* `models.TopicMetadataEntity` was added

* `models.ConnectorServiceTypeInfoBase` was added

* `models.PartnerConnectorType` was added

* `models.Topics` was added

* `models.SCEnvironmentRecord$DefinitionStages` was added

* `models.SCEnvironmentRecord$UpdateStages` was added

* `models.TopicRecord$DefinitionStages` was added

* `models.ConnectorServiceType` was added

* `models.ConnectorResource$Update` was added

* `models.KafkaAzureSynapseAnalyticsSinkConnectorInfo` was added

* `models.StreamGovernanceConfig` was added

* `models.SCClusterRecord$DefinitionStages` was added

* `models.AuthType` was added

* `models.AzureSynapseAnalyticsSinkConnectorServiceInfo` was added

* `models.AzureCosmosDBSourceConnectorServiceInfo` was added

* `models.ConnectorResource$DefinitionStages` was added

* `models.Connectors` was added

* `models.Package` was added

* `models.SCEnvironmentRecord$Update` was added

* `models.SCClusterRecord$UpdateStages` was added

* `models.KafkaAzureCosmosDBSinkConnectorInfo` was added

* `models.TopicsInputConfig` was added

* `models.PartnerInfoBase` was added

* `models.AzureBlobStorageSourceConnectorServiceInfo` was added

* `models.TopicsRelatedLink` was added

* `models.KafkaAzureCosmosDBSourceConnectorInfo` was added

* `models.SCEnvironmentRecord$Definition` was added

* `models.TopicRecord` was added

* `models.TopicRecord$Definition` was added

* `models.ConnectorResource$Definition` was added

* `models.ConnectorInfoBase` was added

* `models.ConnectorResource$UpdateStages` was added

* `models.AzureBlobStorageSinkConnectorServiceInfo` was added

* `models.ConnectorType` was added

* `models.SCClusterRecord$Update` was added

* `models.KafkaAzureBlobStorageSinkConnectorInfo` was added

* `models.AzureCosmosDBSinkConnectorServiceInfo` was added

* `models.Environments` was added

* `models.ConnectorClass` was added

* `models.SCClusterRecord$Definition` was added

#### `models.SCEnvironmentRecord` was modified

* `systemData()` was added
* `resourceGroupName()` was added
* `type()` was added
* `streamGovernanceConfig()` was added
* `update()` was added

#### `models.SCClusterSpecEntity` was modified

* `packageProperty()` was added
* `withPackageProperty(models.Package)` was added

#### `ConfluentManager` was modified

* `topics()` was added
* `connectors()` was added
* `clusters()` was added
* `environments()` was added

#### `models.RoleBindingRecord` was modified

* `innerModel()` was added

#### `models.SCClusterRecord` was modified

* `update()` was added
* `resourceGroupName()` was added
* `type()` was added
* `systemData()` was added

## 1.2.0 (2024-12-19)

- Azure Resource Manager Confluent client library for Java. This package contains Microsoft Azure SDK for Confluent Management SDK.  Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

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
