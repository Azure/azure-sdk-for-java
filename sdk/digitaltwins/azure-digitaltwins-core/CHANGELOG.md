# Release History

## 1.0.1 (Unreleased)


## 1.0.0 (2020-10-30)

### New Features

- Regenerate protocol layer from service API version 2020-10-31
- Update service API version to use service API version 2020-10-31 by default

### Breaking changes

- Replace all Response<string> and Pageable<string> APIs with Response<T> and Pageable<T> respectively
- Rename CreateDigitalTwin, CreateRelationship and CreateEventRoute APIs to CreateOrReplaceDigitalTwin, CreateOrReplaceRelationship and CreateOrReplaceEventRoute respectively
- Renamed model type "ModelData" to "DigitalTwinsModelData" to make type less generic, and less likely to conflict with other libraries
- Renamed model type "EventRoute" to "DigitalTwinsEventRoute" to make type less generic, and less likely to conflict with other libraries
- Remove UpdateOperationsUtility and replace it with a direct dependency on JsonPatchDocument from azure-core
- Remove WritableProperty since service no longer returns that type
- Remove MaxItemCount parameter as an option for GetEventRoutes APIs since users are expected to provide page size in pageable type's .AsPages() method instead
- Rename DigitalTwinsModelData field "DisplayName" to "LanguageDisplayNames" for clarity
- Rename DigitalTwinsModelData field "Description" to "LanguageDescriptions" for clarity
- Flatten DigitalTwinsRequestOptions so that each API takes in ifMatch and ifNoneMatch header directly
- Rework BasicDigitalTwin and other helper classes to better match the service definitions
- Add messageId as mandatory parameter for telemetry APIs. Service API version 2020-10-31 requires this parameter.

### Fixes and improvements
- Fix bug where CreateDigitalTwin and CreateRelationship APIs always sent ifNoneMatch header with value "*" making it impossible to replace an existing entity


## 1.0.0-beta.3 (2020-10-01)

- Fixed issue with pagination APIs that support max-item-count where the item count was not respected from the second page forward.

## 1.0.0-beta.2 (2020-09-24)

### Fixes and improvements

- Paging functionality for list operations has been fixed.
- `listModelOptions` is renamed to `modelsListOptions` for naming consistency.

## 1.0.0-beta.1 (2020-09-22)

### New features

- Official public preview of azure-digitalTwins-core SDK
- [Azure Digital Twins Public Repo](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core)
- [Azure Digital Twins Samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core/src/samples)

### Breaking changes

- N/A

### Added

- N/A

### Fixes and improvements

- N/A
