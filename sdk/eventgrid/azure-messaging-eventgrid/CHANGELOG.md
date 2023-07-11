# Release History

## 4.18.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 4.17.0 (2023-06-16)

### Features Added
- New Container Service events

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-core` from `1.39.0` to version `1.41.0`.

## 4.16.0 (2023-05-22)

### Features Added
- Added new events for Healthcare, Acs, and Storage.

### Other Changes
- Moved customization to its own project.

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.3`.
- Upgraded `azure-core` from `1.37.0` to version `1.39.0`.

## 4.15.1 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 4.15.0 (2023-03-31)

### Features Added

- Added new ACS Email events

## 4.14.2 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 4.14.1 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.

## 4.14.0 (2023-01-19)

### Features Added

- Added new System Events:

    - `Microsoft.ApiManagement.GatewayAPIAdded`
    - `Microsoft.ApiManagement.GatewayAPIRemoved`
    - `Microsoft.ApiManagement.GatewayCertificateAuthorityCreated`
    - `Microsoft.ApiManagement.GatewayCertificateAuthorityDeleted`
    - `Microsoft.ApiManagement.GatewayCertificateAuthorityUpdated`
    - `Microsoft.ApiManagement.GatewayCreated`
    - `Microsoft.ApiManagement.GatewayDeleted`
    - `Microsoft.ApiManagement.GatewayHostnameConfigurationCreated`
    - `Microsoft.ApiManagement.GatewayHostnameConfigurationDeleted`
    - `Microsoft.ApiManagement.GatewayHostnameConfigurationUpdated`
    - `Microsoft.ApiManagement.GatewayUpdated`
    - `Microsoft.DataBox.CopyCompleted`
    - `Microsoft.DataBox.CopyStarted`
    - `Microsoft.DataBox.OrderCompleted`

## 4.13.1 (2023-01-13)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.35.0`.
- Updated `azure-core-http-netty` to `1.12.8`.

## 4.13.0 (2022-11-16)

### Features Added

- Added new System Events:

    - `Microsoft.HealthcareApis.DicomImageCreated`
    - `Microsoft.HealthcareApis.DicomImageDeleted`

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.34.0`.
- Updated `azure-core-http-netty` to `1.12.7`.

## 4.12.3 (2022-10-13)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.33.0`.
- Updated `azure-core-http-netty` to `1.12.6`.

## 4.12.2 (2022-09-14)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.32.0`.
- Updated `azure-core-http-netty` to `1.12.5`.

## 4.12.1 (2022-08-15)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.31.0`.
- Updated `azure-core-http-netty` to `1.12.4`.

## 4.12.0 (2022-07-08)

### Features Added
- Added support to publish CloudEvents to partner topics by setting the channel name.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.30.0`.
- Updated `azure-core-http-netty` to `1.12.3`.

## 4.11.2 (2022-06-09)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.29.1`.
- Updated `azure-core-http-netty` to `1.12.2`.

## 4.12.0-beta.2 (2022-05-17)

### Breaking Changes
- Removed class `SendEventsOptions` and replaced it by the String `channelName`.

## 4.11.1 (2022-05-16)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.28.0`.
- Updated `azure-core-http-netty` to `1.12.0`.

## 4.12.0-beta.1 (2022-04-13)

### Features Added
- Added support to publish CloudEvents to partner topics by setting the channel name in `SendEventsOptions`.

## 4.11.0 (2022-04-12)

### Features Added
- Added system event classes for Azure Healthcare FHIR Services under package `com.azure.messaging.eventgrid.systemevents`,
  `HealthcareFhirResourceCreatedEventData`, `HealthcareFhirResourceDeletedEventData` and `HealthcareFhirResourceUpdatedEventData`.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.27.0`.
- Updated `azure-core-http-netty` to `1.11.9`.

## 4.10.0 (2022-03-10)

### Breaking Changes
- Added new enum values for `MediaJobErrorCategory` and `MediaJobErrorCode`.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.26.0`.
- Updated `azure-core-http-netty` to `1.11.8`.

## 4.9.0 (2022-02-10)

### Features Added
- Added interfaces from `com.azure.core.client.traits` to `EventGridPublisherClientBuilder`.
- Added a new method `retryOptions` to `EventGridPublisherClientBuilder`.
- Updated ARM events, `ResourceActionCancelEventData`, `ResourceActionFailureEventData`, 
`ResourceActionSuccessEventData`, `ResourceDeleteCancelEventData`, `ResourceDeleteFailureEventData`, 
`ResourceDeleteSuccessEventData`, `ResourceWriteCancelEventData`, `ResourceWriteFailureEventData`,
`ResourceWriteSuccessEventData`. 
    - Added new type `ResourceAuthorization` and `ResourceHttpRequest`.
    - Deprecated 
      `getHttpRequest()` and replaced it with `getResourceAuthorization()`,
      `setHttpRequest(String httpRequest)` and replaced it with `setResourceAuthorization(ResourceAuthorization authorization)`,
      `getClaims()` and replaced it with `getResourceClaims()`,
      `setClaims(String claims)` and replaced it with `setResourceClaims(Map<String, String> claims)`,
      `getAuthorization()` and replaced it with `getResourceHttpRequest()`,
      `setAuthorization(String authorization)` and replaced it with `setResourceHttpRequest(ResourceHttpRequest httpRequest)`.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.25.0`.
- Updated `azure-core-http-netty` to `1.11.7`.

## 4.8.0 (2022-01-14)

### Features Added
- Added new properties
    - `deleteLocation` to `AcsRecordingChunkInfoProperties`
    - `recordingChannelType`, `recordingContentType`, `recordingFormatType` to `AcsRecordingFileStatusUpdatedEventData`
    - `connectedRegistry`, `location` to `ContainerRegistryImagePushedEventData`, `ContainerRegistryImageDeletedEventData`,
      `ContainerRegistryEventConnectedRegistry`, `ContainerRegistryChartPushedEventData`,
      `ContainerRegistryChartDeletedEventData`, `ContainerResistryArtifactEventData`

- Added new model types,
    - `ContainerRegistryEventConnectedRegistry`, `RecordingChannelType`, `RecordingContentType`, `RecordingFormatType`

### Other Changes

#### Dependency Updates
- Update `azure-core` dependency to `1.24.1`.
- Update `azure-core-http-netty` dependency to `1.11.6`.

## 4.7.1 (2021-11-11)

### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.21.0` to `1.22.0`.
- Updated `azure-core-http-netty` from `1.11.1` to `1.11.2`.

## 4.7.0 (2021-10-13)

### Features Added
- Added new Api Management service system events, `ApiManagementApiCreatedEventData`, `ApiManagementApiDeletedEventData`, 
  `ApiManagementApiReleaseCreatedEventData`, `ApiManagementApiReleaseDeletedEventData`, 
  `ApiManagementApiReleaseUpdatedEventData`, `ApiManagementApiUpdatedEventData`, `ApiManagementProductCreatedEventData`,
  `ApiManagementProductDeletedEventData`, `ApiManagementProductUpdatedEventData`,
  `ApiManagementSubscriptionCreatedEventData`, `ApiManagementSubscriptionDeletedEventData`,
  `ApiManagementSubscriptionUpdatedEventData`,`ApiManagementUserCreatedEventData`, `ApiManagementUserDeletedEventData`,
  `ApiManagementUserUpdatedEventData`. 
- Added a new Media service system event, `MediaLiveEventChannelArchiveHeartbeatEventData`.
- Added a new Communication service system event, `AcsUserDisconnectedEventData`.
- Added fields `transcriptionLanguage`, `transcriptionState`, `ingestDriftValue`, `lastFragmentArrivalTime` 
  to system event classes `MediaLiveEventIngestHeartbeatEventData`.

### Other Changes

#### Dependency Updates
- Update `azure-core` dependency to `1.21.0`.
- Update `azure-core-http-netty` dependency to `1.11.1`.

## 4.6.1 (2021-09-10)

### Other Changes

#### Dependency Updates
- Update `azure-core` dependency to `1.20.0`.
- Update `azure-core-http-netty` dependency to `1.11.0`.

## 4.6.0 (2021-08-11)
### Features Added
- Added new Container Service system event `ContainerServiceNewKubernetesVersionAvailableEventData`.

### Dependency Updates
- Update `azure-core` dependency to `1.19.0`.
- Update `azure-core-http-netty` dependency to `1.10.2`.

## 4.5.0 (2021-07-19)
### Features Added
- Added `EventGridPublisherClientBuilder#credential(TokenCredential credential)` to support Azure Active Directory authentication.
- Added field `metadata` to system event classes `AcsChatMessageEditedEventData`, `AcsChatMessageEditedInThreadEventData`, `AcsChatMessageReceivedEventData` and `AcsChatMessageReceivedInThreadEventData`.

### Dependency Updates
- Update `azure-core` dependency to `1.18.0`.
- Update `azure-core-http-netty` dependency to `1.10.1`.

## 4.4.0 (2021-06-09)
### New Features
- Added new Storage system event `StorageBlobInventoryPolicyCompletedEventData`.
- Added new attributes `contentLocation` and `metadataLocation` to `AcsRecordingChunkInfoProperties`.

### Dependency Updates
- Update `azure-core` dependency to `1.17.0`.
- Update `azure-core-http-netty` dependency to `1.10.0`.


## 4.3.0 (2021-05-12)
### New Features
- Added new Storage system events `StorageAsyncOperationInitiatedEventData` and `StorageBlobTierChangedEventData`.
- Added new Policy Insights system events `PolicyInsightsPolicyStateCreatedEventData`, `PolicyInsightsPolicyStateChangedEventData`, and `PolicyInsightsPolicyStateDeletedEventData`.

### Dependency Updates
- Update `azure-core` dependency to `1.16.0`.
- Update `azure-core-http-netty` dependency to `1.9.2`.

## 4.2.0 (2021-04-07)

### Dependency Updates
- Update `azure-core` dependency to `1.15.0`.
- Update `azure-core-http-netty` dependency to `1.9.1`.

## 4.1.0 (2021-03-25)
### New Features
- Added new system event model class `AcsRecordingFileStatusUpdatedEventData`.
- Added new attribute `transactionId` to `AcsChatEventInThreadBaseProperties`.
- Added new attribute `tag` to `AcsSmsDeliveryReportReceivedEventData`.

### Bug Fixes
- Fixed event types in `SystemEventNames` for system event data `AcsChatParticipantRemovedFromThreadEventData` and `AcsChatParticipantAddedToThreadEventData`.
- Added `COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD` to `SystemEventNames` and deprecated `COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD`.
- Added `COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD_WITH_USER` to `SystemEventNames` and deprecated `COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD_WITH_USER`.

## 4.0.0 (2021-03-11)
### New Features
- added `sendEvent` to `EventGridPublisherClient` and `EventGridPublisherAsyncClient` to send a single event.

### Breaking changes
- `CloudEvent` is moved to `azure-core` SDK version 1.14.0. Its constructor uses `BinaryData` instead of `Object` as the data type for `data`.
- `EventGridEvent` constructor also uses `BinaryData` instead of `Object` as the data type for `data`.
- To send custom events, `sendEvents` accepts `Iterable<BinaryData>` instead of `Iterable<Object>`.
- `EventGridPublisherClientBuilder.serializer()` is removed because `BinaryData.fromObject(Object data, ObjectSerializer serializer)` already supports custom serializer, which can be used to
  serialize custom events and the data of `CloudEvent` and `EventGridEvent`.
- `EventGridPublisherClient` is changed to `EventGridPublisherClient<T>` that can be statically instantiated to send `CloudEvent`, `EventGridEvent` or custom events (use `BinaryData`)
  with methods `sendEvents` and `sendEvent`. 
  `EventGridPublisherClientBuilder` now has `buildCloudEventPublisherClient`, `buildEventGridEventPublisherClient` and `buildCustomEventPublisherClient` to build the generic-instantiated clients respectively.
  The async client has the same change.
- `EventGridPublisherClientBuilder.endpoint()` now requires the EventGrid topic or domain full url endpoint because
  different EventGrid service deployments may require different url patterns.
- `EventGridSasGenerator` is removed. Method `generateSas` is moved to `EventGridPublisherClient` and `EventGridPublisherAsyncClient`. 

### Dependency Updates
- Update `azure-core` dependency to `1.14.0`.
- Update `azure-core-http-netty` dependency to `1.9.0`.

## 2.0.0-beta.4 (2021-02-10)
### Breaking changes
- `CloudEvent` constructor now accepts parameter "data". Removed `setData()`.
- `CloudEvent.parse()` and `EventGridEvent.parse()` are renamed to `fromString()`.
- `CloudEvent::getData()` of CloudEvent and EventGridEvent now returns `com.azure.core.util.BinaryData`. 
  Users can use methods `BinaryData` to deserialize event data. The generic version of `getData()` is then removed.
- Removed `CloudEvent::getDataAsync()`
- Added `EventGridSasGenerator` class and removed `EventGridSasCredential`. Use `EventGridSasGenerator.generateSas()` to
  create a Shared Access Signature and use `AzureSasCredential` to build a `EventGridPublisherClient`.
- Renamed `sendEvents` to `sendEventGridEvents`

### Dependency Updates
- Update `azure-core` dependency to `1.13.0`.
- Update `azure-core-http-netty` dependency to `1.8.0`.
- Remove dependency on `azure-core-serializer-json-jackson`.

## 2.0.0-beta.3 (2020-10-06)
### New Features
- Added support for distributed tracing.

## 2.0.0-beta.2 (2020-09-24)
Added system event classes for Azure Communication Services under package `com.azure.messaging.eventgrid.systemevents`.

## 2.0.0-beta.1 (2020-09-09)

Initial preview of the Event Grid library with an effort to create a Java idiomatic
set of libraries that are consistent across multiple services as well as different languages.

### Features:

+ Configurable synchronous and asynchronous publishing clients, supporting sending of user-defined events in 
    Event Grid, Cloud Event, or a custom schema.
+ Parsing and deserialization of system and user-defined events from JSON payload
    at an event destination in EventGrid or Cloud Event schema.
