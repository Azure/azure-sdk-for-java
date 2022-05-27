# Release History

## 1.1.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.2 (2022-04-13)

### Bugs Fixed

- Set Ttl parameter in the Communication Relay Async client

### Other Changes

- Update of azure-core to 1.27.0
- Update of azure-communication-common to 1.1.2

## 1.1.0-beta.1 (2022-03-15)

### Features Added

- Added interfaces from `com.azure.core.client.traits` to `CommunicationRelayClientBuilder`
- Added `retryOptions` to `CommunicationRelayClientBuilder`
- Added optional parameter to GetRelayConfiguration to set credential Time-To-Live in seconds of max 48 hours. The default value will be used if given value exceeds it.

### Breaking Changes

- Making Ttl part of the options parameter

## 1.0.0 (2022-02-11) (Deprecated)

### Features Added

- Added GetRelayConfigurationOptions with communicationUser and
  routeType as parameters when calling getRelayConfiguration and getRelayConfigurationWithResponse

## 1.0.0-beta.2 (2021-11-18)

### Features Added

- Made User Identity an optional parameter when getting a Relay Configuration.
- Added RouteType as optional parameter when getting a Relay Configuration so users can
  choose the routing type protocol of the requested Relay Configuration.

## 1.0.0-beta.1 (2021-09-09)

The first preview of the Azure Communication Relay Client has the following features:

- get a relay configuration by creating a CommunicationRelayClient

### Features Added

- Added CommunicationRelayClient in preview.
- Added CommunicationRelayClient.getRelayConfiguration in preview.
