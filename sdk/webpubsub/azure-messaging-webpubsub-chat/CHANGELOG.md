# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

- Added support for service API version `2026-08-01-preview`, now the default service version.
- Added synchronous and asynchronous operations to list rooms, users, and rooms associated with a user.
- Added topic creation, replacement, retrieval, update, soft deletion, and listing.
- Added `ChatTopic`, `ChatTopicState`, and the read-only `ChatUserRoom` projection.

### Breaking Changes

### Bugs Fixed

### Other Changes

- Verified all existing and new operations end to end with `2026-08-01-preview` in Central US EUAP; removed the temporary server routing workaround and restored the complete live/playback suite.
- Tracked the upstream typed asynchronous page-size defect in [microsoft/typespec#12149](https://github.com/microsoft/typespec/issues/12149). No paging customization is applied; tests temporarily use protocol overloads for asynchronous page-size checks.

## 1.0.0-beta.1 (2026-09-15)

- Azure WebPubSubChat client library for Java. This package contains Microsoft Azure WebPubSubChat client library.

### Features Added

- Added synchronous and asynchronous clients for managing Chat users, rooms, room members, roles, and message history.
- Added connection string, access key, and Microsoft Entra ID authentication.
- Added client access token generation with local access-key signing and service delegation for Microsoft Entra ID.
- Added reverse proxy support and built-in Chat role and permission values.
