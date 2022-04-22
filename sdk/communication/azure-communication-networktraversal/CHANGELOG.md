# Release History

## 1.0.1 (2022-04-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.0.8` to version `1.1.3`.
- Upgraded `azure-core` from `1.25.0` to version `1.28.0`.


## 1.0.0 (2022-02-11)

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
