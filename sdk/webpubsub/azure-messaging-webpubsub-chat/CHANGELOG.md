# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

- Added support for service API version `2026-08-01-preview`, now the default service version.
- Added synchronous and asynchronous operations to list rooms, users, and rooms associated with a user.
- Added topic creation, replacement, retrieval, update, soft deletion, and listing.
- Added `ChatTopic`, `ChatTopicState`, and the read-only `ChatUserRoom` projection.

### Breaking Changes

- Replaced the public `MessageContent` constructor and `setText`/`setBinary` methods with `MessageContent.fromText` and `MessageContent.fromBinary`. Each instance contains exactly one non-null representation; empty text and binary payloads remain supported.

### Bugs Fixed

- Corrected request and response Javadoc schemas to reflect read-only properties and partial-update optionality, and clarified that topic state is optional on creation.
- Regenerated with Java emitter 0.48.1 to preserve page-size overrides on typed asynchronous collections, including continuation requests.

### Other Changes

- Verified all existing and new operations end to end with `2026-08-01-preview` in Central US EUAP; removed the temporary server routing workaround and restored the complete live/playback suite.
- Enabled all typed asynchronous page-size regression cases and removed the temporary protocol paging workaround from tests and samples.

## 1.0.0-beta.1 (2026-09-15)

- Azure WebPubSubChat client library for Java. This package contains Microsoft Azure WebPubSubChat client library.

### Features Added

- Added synchronous and asynchronous clients for managing Chat users, rooms, room members, roles, and message history.
- Added connection string, access key, and Microsoft Entra ID authentication.
- Added client access token generation with local access-key signing and service delegation for Microsoft Entra ID.
- Added reverse proxy support and built-in Chat role and permission values.
