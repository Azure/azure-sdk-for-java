# Release History

## 2.0.0-beta.5 (Unreleased)


## 2.0.0-beta.4 (2020-02-10)
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

## 2.0.0-beta.1 (2020-09-09): 

Initial preview of the Event Grid library with an effort to create a Java idiomatic
set of libraries that are consistent across multiple services as well as different languages.

### Features:

+ Configurable synchronous and asynchronous publishing clients, supporting sending of user-defined events in 
    Event Grid, Cloud Event, or a custom schema.
+ Parsing and deserialization of system and user-defined events from JSON payload
    at an event destination in EventGrid or Cloud Event schema.

