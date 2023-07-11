# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
