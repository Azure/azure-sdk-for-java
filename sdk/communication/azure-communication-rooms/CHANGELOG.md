# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

- Added new function `listRooms` to list all created rooms by returning `PagedIterable<CommunicationRoom>`,
- Added pagination support for `listParticipants` by returning `PagedIterable<RoomParticipant>`.

### Breaking Changes

- Removed `participants` from `CommunicationRoom` model.
- Removed `roomJoinPolicy`, all rooms are invite-only by default.
- `updateRoom` no longer accepts participant list as input.
- Replaced `addParticipants` and `updateParticipants` with `upsertParticipants`
- Renamed `RoleType` to `ParticipantRole`
- Renamed `getParticipants` to `listParticipants`
- Renamed `CreatedOn` to `CreatedAt` in `CommunicationRoom`
- `removeParticipants` now takes in a `Iterable<CommunicationIdentifier>` instead of `Iterable<RoomParticipant>`

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-08-12)
Azure Communication Services for rooms. For more information, please see the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-rooms/README.md) and [documentation](https://docs.microsoft.com/azure/communication-services/concepts/rooms/room-concept).


## 1.0.0-beta.1 (2022-08-10)
This is the initial release of Azure Communication Services for rooms. For more information, please see the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-rooms/README.md) and [documentation](https://docs.microsoft.com/azure/communication-services/concepts/rooms/room-concept).
