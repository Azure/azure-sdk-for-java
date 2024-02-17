# Release History

## 1.0.8 (2024-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-communication-common` from `1.2.15` to version `1.3.0`.


## 1.0.7 (2023-12-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.14` to version `1.2.15`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.0.6 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-communication-common` from `1.2.13` to version `1.2.14`.


## 1.0.5 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-communication-common` from `1.2.12` to version `1.2.13`.


## 1.0.4 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-communication-common` from `1.2.11` to version `1.2.12`.


## 1.0.3 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-communication-common` from `1.2.10` to version `1.2.11`.


## 1.0.2 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.9` to version `1.2.10`.
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.


## 1.0.1 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.8` to version `1.2.9`.


## 1.0.0 (2023-06-12)
General Availability version of the Azure Communication Services Rooms Java SDK.

## 1.0.0-beta.3 (2023-05-17)

### Features Added

- Added new function `listRooms` to list all created rooms by returning `PagedIterable<CommunicationRoom>`,
- Added pagination support for `listParticipants` by returning `PagedIterable<RoomParticipant>`.

### Breaking Changes

- Changed: `updateRoom` no longer accepts participant list as input.
- Changed: Replaced `addParticipants` and `updateParticipants` with `addOrUpdateParticipants`.
- Changed: Renamed `RoleType` to `ParticipantRole`.
- Changed: Renamed `getParticipants` to `listParticipants`.
- Changed: Renamed `CreatedOn` to `CreatedAt` in `CommunicationRoom`.
- Changed: `removeParticipants` now takes in a `Iterable<CommunicationIdentifier>` instead of `Iterable<RoomParticipant>`.
- Removed: `participants` from `CommunicationRoom` model.
- Removed: `roomJoinPolicy` so all rooms are invite-only by default.

## 1.0.0-beta.2 (2022-08-12)
Azure Communication Services for rooms. For more information, please see the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-rooms/README.md) and [documentation](https://docs.microsoft.com/azure/communication-services/concepts/rooms/room-concept).


## 1.0.0-beta.1 (2022-08-10)
This is the initial release of Azure Communication Services for rooms. For more information, please see the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-rooms/README.md) and [documentation](https://docs.microsoft.com/azure/communication-services/concepts/rooms/room-concept).
