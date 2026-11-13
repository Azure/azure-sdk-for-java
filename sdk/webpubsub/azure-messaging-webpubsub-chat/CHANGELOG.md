# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-11-12)

### Features Added

- Added support for service API version `2026-08-01-preview`, now the default service version.
- Added synchronous and asynchronous operations to list rooms, users, and rooms associated with a user.
- Added topic creation, replacement, retrieval, update, soft deletion, and listing.
- Added `ChatTopic`, `ChatTopicState`, and the read-only `ChatUserRoom` projection.

### Breaking Changes

- Replaced the public `MessageContent` constructor and `setText`/`setBinary` methods with `MessageContent.fromText` and `MessageContent.fromBinary`. Each instance contains exactly one non-null representation; empty text and binary payloads remain supported.
- Renamed `GetClientAccessTokenOptions` to `ClientAccessTokenOptions`.
- Renamed `ChatConversation.getParentRoom()` to `getRoomId()`. The JSON property remains `parentRoom`.

### Bugs Fixed

- Fixed typed asynchronous collections to preserve page-size overrides, including continuation requests.

### Other Changes

- Upgraded `azure-core` from `1.59.1` to version `1.61.0`.
- Upgraded `azure-core-http-netty` from `1.16.7` to version `1.17.0`.
- Upgraded `nimbus-jose-jwt` from `9.37.3` to version `9.37.4`.

## 1.0.0-beta.1 (2026-09-15)

- Azure WebPubSubChat client library for Java. This package contains Microsoft Azure WebPubSubChat client library.

### Features Added

- Added synchronous and asynchronous clients for managing Chat users, rooms, room members, roles, and message history.
- Added connection string, access key, and Microsoft Entra ID authentication.
- Added client access token generation with local access-key signing and service delegation for Microsoft Entra ID.
- Added reverse proxy support and built-in Chat role and permission values.
