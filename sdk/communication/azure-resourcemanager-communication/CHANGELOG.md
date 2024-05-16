# Release History

## 2.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.1.0 (2024-03-18)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2023-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ManagedServiceIdentityType` was added

* `models.ManagedServiceIdentity` was added

* `models.UserAssignedIdentity` was added

#### `models.CommunicationServiceResource$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CommunicationServiceResource$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CommunicationServiceResourceUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

#### `models.CommunicationServiceResource` was modified

* `identity()` was added

## 2.1.0-beta.2 (2023-11-23)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-preview-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SuppressionListAddressResourceCollection` was added

* `models.SuppressionListAddressResource` was added

* `models.SuppressionListResource$UpdateStages` was added

* `models.SuppressionListResource$Update` was added

* `models.SuppressionListAddressResource$Update` was added

* `models.SuppressionListAddressResource$Definition` was added

* `models.SuppressionListAddresses` was added

* `models.SuppressionListAddressResource$DefinitionStages` was added

* `models.SuppressionListResource$Definition` was added

* `models.SuppressionListResource` was added

* `models.SuppressionLists` was added

* `models.SuppressionListResourceCollection` was added

* `models.SuppressionListResource$DefinitionStages` was added

* `models.SuppressionListAddressResource$UpdateStages` was added

#### `CommunicationManager` was modified

* `suppressionLists()` was added
* `suppressionListAddresses()` was added

## 2.1.0-beta.1 (2023-09-18)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-preview-2023-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ManagedServiceIdentityType` was added

* `models.ManagedServiceIdentity` was added

* `models.UserAssignedIdentity` was added

#### `models.CommunicationServiceResource$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CommunicationServiceResource$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CommunicationServiceResourceUpdate` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CommunicationServiceResource` was modified

* `identity()` was added

## 2.0.0 (2023-04-03)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.LocationResource` was removed

* `models.CommunicationServicesCreateOrUpdateHeaders` was removed

* `models.OperationStatus` was removed

* `models.NameAvailability` was removed

* `models.CommunicationServicesCreateOrUpdateResponse` was removed

* `models.OperationStatuses` was removed

* `models.Status` was removed

* `models.CommunicationServicesDeleteResponse` was removed

* `models.CommunicationServicesDeleteHeaders` was removed

#### `models.CommunicationServices` was modified

* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner)` was removed
* `update(java.lang.String,java.lang.String)` was removed
* `checkNameAvailability()` was removed
* `updateWithResponse(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner,com.azure.core.util.Context)` was removed

#### `models.NameAvailabilityParameters` was modified

* `type()` was removed
* `name()` was removed

#### `models.CommunicationServiceResource` was modified

* `models.ProvisioningState provisioningState()` -> `models.CommunicationServicesProvisioningState provisioningState()`

#### `CommunicationManager` was modified

* `operationStatuses()` was removed

### Features Added

* `models.DomainsProvisioningState` was added

* `models.CommunicationServiceResource$DefinitionStages` was added

* `models.VerificationStatusRecord` was added

* `models.UpdateDomainRequestParameters` was added

* `models.DomainResourceList` was added

* `models.SenderUsernameResource$DefinitionStages` was added

* `models.DomainResource$Definition` was added

* `models.EmailServiceResource` was added

* `models.DomainResource$Update` was added

* `models.CheckNameAvailabilityReason` was added

* `models.EmailServiceResource$Update` was added

* `models.VerificationType` was added

* `models.EmailServiceResource$UpdateStages` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.CommunicationServiceResource$Update` was added

* `models.SenderUsernameResource$Definition` was added

* `models.CommunicationServiceResource$Definition` was added

* `models.DomainResource$DefinitionStages` was added

* `models.CommunicationServiceResourceUpdate` was added

* `models.EmailServiceResource$Definition` was added

* `models.DomainResource$UpdateStages` was added

* `models.EmailServiceResource$DefinitionStages` was added

* `models.DnsRecord` was added

* `models.SenderUsernames` was added

* `models.SenderUsernameResource` was added

* `models.DomainPropertiesVerificationStates` was added

* `models.EmailServicesProvisioningState` was added

* `models.EmailServiceResourceUpdate` was added

* `models.EmailServices` was added

* `models.VerificationParameter` was added

* `models.DomainManagement` was added

* `models.Domains` was added

* `models.VerificationStatus` was added

* `models.DomainResource` was added

* `models.CommunicationServicesProvisioningState` was added

* `models.UserEngagementTracking` was added

* `models.SenderUsernameResourceCollection` was added

* `models.EmailServiceResourceList` was added

* `models.SenderUsernameResource$Update` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.DomainPropertiesVerificationRecords` was added

* `models.CommunicationServiceResource$UpdateStages` was added

* `models.SenderUsernameResource$UpdateStages` was added

#### `models.CommunicationServices` was modified

* `getById(java.lang.String)` was added
* `checkNameAvailability(models.NameAvailabilityParameters)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `define(java.lang.String)` was added

#### `models.NameAvailabilityParameters` was modified

* `withName(java.lang.String)` was added
* `withType(java.lang.String)` was added

#### `models.CommunicationServiceResource` was modified

* `linkedDomains()` was added
* `name()` was added
* `listKeys()` was added
* `regionName()` was added
* `type()` was added
* `id()` was added
* `linkNotificationHubWithResponse(models.LinkNotificationHubParameters,com.azure.core.util.Context)` was added
* `listKeysWithResponse(com.azure.core.util.Context)` was added
* `refresh()` was added
* `update()` was added
* `refresh(com.azure.core.util.Context)` was added
* `regenerateKeyWithResponse(models.RegenerateKeyParameters,com.azure.core.util.Context)` was added
* `region()` was added
* `linkNotificationHub()` was added
* `resourceGroupName()` was added
* `regenerateKey(models.RegenerateKeyParameters)` was added

#### `CommunicationManager` was modified

* `emailServices()` was added
* `domains()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `senderUsernames()` was added

#### `CommunicationManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.1.0-beta.4 (2023-03-20)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-preview-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CommunicationServiceResource` was modified

* `regenerateKey(models.RegenerateKeyParameters,com.azure.core.util.Context)` was removed

#### `models.UpdateDomainRequestParameters` was modified

* `withValidSenderUsernames(java.util.Map)` was removed
* `validSenderUsernames()` was removed

#### `models.DomainResource$Definition` was modified

* `withValidSenderUsernames(java.util.Map)` was removed

#### `models.DomainResource$Update` was modified

* `withValidSenderUsernames(java.util.Map)` was removed

#### `models.DomainResource` was modified

* `validSenderUsernames()` was removed

#### `models.CommunicationServices` was modified

* `regenerateKey(java.lang.String,java.lang.String,models.RegenerateKeyParameters,com.azure.core.util.Context)` was removed

### Features Added

* `models.SenderUsernameResource$DefinitionStages` was added

* `models.ProvisioningState` was added

* `models.SenderUsernameResourceCollection` was added

* `models.SenderUsernameResource$Update` was added

* `models.SenderUsernameResource$UpdateStages` was added

* `models.SenderUsernameResource$Definition` was added

* `models.SenderUsernames` was added

* `models.SenderUsernameResource` was added

#### `models.CommunicationServiceResource` was modified

* `regenerateKeyWithResponse(models.RegenerateKeyParameters,com.azure.core.util.Context)` was added
* `systemData()` was added

#### `CommunicationManager` was modified

* `senderUsernames()` was added

#### `models.CommunicationServices` was modified

* `regenerateKeyWithResponse(java.lang.String,java.lang.String,models.RegenerateKeyParameters,com.azure.core.util.Context)` was added

## 1.1.0-beta.3 (2022-08-23)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-preview-2022-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.1.0-beta.2 (2022-05-25)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2021-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ProvisioningState` was removed

* `models.LocationResource` was removed

* `models.CommunicationServicesDeleteResponse` was removed

* `models.CommunicationServicesDeleteHeaders` was removed

* `models.CommunicationServicesCreateOrUpdateHeaders` was removed

* `models.NameAvailability` was removed

* `models.CommunicationServicesCreateOrUpdateResponse` was removed

#### `models.CommunicationServiceResource` was modified

* `regenerateKeyWithResponse(models.RegenerateKeyParameters,com.azure.core.util.Context)` was removed
* `systemData()` was removed
* `models.ProvisioningState provisioningState()` -> `models.CommunicationServicesProvisioningState provisioningState()`

#### `models.CommunicationServices` was modified

* `checkNameAvailability()` was removed
* `regenerateKeyWithResponse(java.lang.String,java.lang.String,models.RegenerateKeyParameters,com.azure.core.util.Context)` was removed

#### `models.NameAvailabilityParameters` was modified

* `type()` was removed
* `name()` was removed

### Features Added

* `models.DomainsProvisioningState` was added

* `models.VerificationStatusRecord` was added

* `models.DomainPropertiesVerificationStates` was added

* `models.UpdateDomainRequestParameters` was added

* `models.DomainResourceList` was added

* `models.DomainResource$Definition` was added

* `models.EmailServiceResource` was added

* `models.EmailServicesProvisioningState` was added

* `models.EmailServiceResourceUpdate` was added

* `models.DomainResource$Update` was added

* `models.CheckNameAvailabilityReason` was added

* `models.EmailServices` was added

* `models.VerificationParameter` was added

* `models.EmailServiceResource$Update` was added

* `models.DomainManagement` was added

* `models.Domains` was added

* `models.VerificationStatus` was added

* `models.DomainResource` was added

* `models.CommunicationServicesProvisioningState` was added

* `models.UserEngagementTracking` was added

* `models.VerificationType` was added

* `models.EmailServiceResourceList` was added

* `models.EmailServiceResource$UpdateStages` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.DomainPropertiesVerificationRecords` was added

* `models.DomainResource$DefinitionStages` was added

* `models.CommunicationServiceResourceUpdate` was added

* `models.EmailServiceResource$Definition` was added

* `models.DomainResource$UpdateStages` was added

* `models.EmailServiceResource$DefinitionStages` was added

* `models.DnsRecord` was added

#### `models.CommunicationServiceResource` was modified

* `regenerateKey(models.RegenerateKeyParameters,com.azure.core.util.Context)` was added
* `linkedDomains()` was added
* `resourceGroupName()` was added

#### `CommunicationManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `domains()` was added
* `emailServices()` was added

#### `CommunicationManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.CommunicationServices` was modified

* `checkNameAvailability(models.NameAvailabilityParameters)` was added
* `regenerateKey(java.lang.String,java.lang.String,models.RegenerateKeyParameters,com.azure.core.util.Context)` was added

#### `models.NameAvailabilityParameters` was modified

* `withType(java.lang.String)` was added
* `withName(java.lang.String)` was added

#### `models.CommunicationServiceResource$Update` was modified

* `withLinkedDomains(java.util.List)` was added

#### `models.CommunicationServiceResource$Definition` was modified

* `withLinkedDomains(java.util.List)` was added

## 1.1.0-beta.1 (2022-01-24)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2020-08-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OperationStatus` was removed

* `models.Status` was removed

* `models.OperationStatuses` was removed

#### `models.CommunicationServices` was modified

* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner,com.azure.core.util.Context)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String)` was removed
* `createOrUpdate(java.lang.String,java.lang.String)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.CommunicationServiceResourceInner)` was removed

#### `CommunicationManager` was modified

* `operationStatuses()` was removed

### Features Added

* `models.CommunicationServiceResource$DefinitionStages` was added

* `models.CommunicationServiceResource$Update` was added

* `models.CommunicationServiceResource$UpdateStages` was added

* `models.CommunicationServiceResource$Definition` was added

#### `models.CommunicationServiceResource` was modified

* `name()` was added
* `refresh()` was added
* `update()` was added
* `linkNotificationHubWithResponse(models.LinkNotificationHubParameters,com.azure.core.util.Context)` was added
* `linkNotificationHub()` was added
* `refresh(com.azure.core.util.Context)` was added
* `region()` was added
* `regenerateKey(models.RegenerateKeyParameters)` was added
* `listKeys()` was added
* `id()` was added
* `type()` was added
* `listKeysWithResponse(com.azure.core.util.Context)` was added
* `regenerateKeyWithResponse(models.RegenerateKeyParameters,com.azure.core.util.Context)` was added
* `regionName()` was added

#### `models.CommunicationServices` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added

#### `CommunicationManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0 (2021-04-08)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2020-08-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ErrorAdditionalInfo` was removed

## 1.0.0-beta.1 (2021-03-23)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2020-08-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

