# Release History

## 1.1.0-beta.3 (2022-06-07)

- Azure Resource Manager Communication client library for Java. This package contains Microsoft Azure SDK for Communication Management SDK. REST API for Azure Communication Services. Package tag package-2021-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

