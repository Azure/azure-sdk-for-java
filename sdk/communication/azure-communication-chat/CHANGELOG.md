# Release History

## 1.5.3 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-communication-common` from `1.3.3` to version `1.3.4`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.5.2 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.2` to version `1.3.3`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.


## 1.5.1 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.
- Upgraded `azure-communication-common` from `1.3.1` to version `1.3.2`.


## 1.5.0 (2024-04-15)

### Features Added

- Expanded `ChatAttachment` to include new enum `file` to enable communication users to recieve file attachments shared by Teams user in a Teams Interoperbility Chat.

## 1.4.2 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.
- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-communication-common` from `1.3.0` to version `1.3.1`.


## 1.4.1 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-communication-common` from `1.2.15` to version `1.3.0`.


## 1.4.0 (2023-12-04)

### Features Added

- Added `ChatAttachment` object that contains properties for inline images incoming from Teams Chat Threads.

## 1.3.14 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-communication-common` from `1.2.13` to version `1.2.14`.

## 1.3.13 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-communication-common` from `1.2.12` to version `1.2.13`.

## 1.3.12 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-communication-common` from `1.2.11` to version `1.2.12`.

## 1.3.11 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 1.3.10 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.9` to version `1.2.10`.
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.


## 1.3.9 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-communication-common` from `1.2.8` to version `1.2.9`.

## 1.3.8 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.6` to version `1.2.8`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.3.7 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 1.3.6 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.5` to version `1.2.6`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 1.3.5 (2023-02-14)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.2.5
- Upgraded `azure-core` to 1.36.0

## 1.3.4 (2023-01-19)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.2.4
- Upgraded `azure-core` to 1.35.0

## 1.3.3 (2022-11-10)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.2.3
- Upgraded `azure-core` to 1.34.0

## 1.3.2 (2022-10-14)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.2.2
- Upgraded `azure-core` to 1.33.0

## 1.3.1 (2022-09-12)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.2.1
- Upgraded `azure-core` to 1.32.0

## 1.3.0 (2022-08-16)
### New features
- Added `String getRawId()` and `static CommunicationIdentifier fromRawId(String rawId)` to `CommunicationIdentifier` to translate between a `CommunicationIdentifier` and its underlying canonical `rawId` representation. Developers can now use the `rawId` as an encoded format for identifiers to store in their databases or as stable keys in general.
### Dependency Updates
- Upgraded `azure-communication-common` to 1.2.0
- Upgraded `azure-core` to 1.31.0

## 1.2.4 (2022-07-14)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.5
- Upgraded `azure-core` to 1.30.0

## 1.2.3 (2022-06-15)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.4
- Upgraded `azure-core` to 1.29.1

## 1.2.2 (2022-05-13)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.3
- Upgraded `azure-core` to 1.28.0

## 1.2.1 (2022-04-12)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.2
- Upgraded `azure-communication-identity` to 1.1.8

## 1.2.0 (2022-03-11)
#### Features Added
- Added interfaces from `com.azure.core.client.traits` to `ChatClientBuilder` and `ChatThreadClientBuilder`
- Added `retryOptions` to `ChatClientBuilder` and `ChatThreadClientBuilder`

### Other Changes

#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.1
- Upgraded `azure-communication-identity` to 1.1.7

## 1.1.4 (2022-02-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.8

## 1.1.3 (2022-01-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.7
- Upgraded `azure-core` to 1.24.1

## 1.1.2 (2021-11-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.6
- Upgraded `azure-core` to 1.22.0

## 1.1.1 (2021-10-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.5
- Upgraded `azure-communication-identity` to 1.1.3
- Upgraded `azure-core` to 1.21.0

## 1.1.0 (2021-09-15)
- Added javadoc code samples
- Removed redundant overload `ChatThreadAsyncClient.sendTypingNotification(TypingNotificationOptions options)`
- Upgraded `azure-communication-common` to 1.0.4

## 1.1.0-beta.2 (2021-08-10)
- Fix version of dependency on azure-communication-common

## 1.1.0-beta.1 (2021-08-10)
- Added method `ChatThreadAsyncClient.listParticipants(ListParticipantsOptions listParticipantsOptions)`
- Added method `ChatThreadAsyncClient.listReadReceipts(ListReadReceiptOptions listReadReceiptOptions)`
- Added support for metadata in messages.
- Added options class `TypingNotificationOptions` for setting `SenderDisplayName` of the notification sender.

## 1.0.1 (2021-05-27)
- Dependency versions updated.

## 1.0.0 (2021-03-29)
### Breaking Changes

- Renamed `ChatThread` to `ChatThreadProperties`
- Renamed `ChatThreadInfo` to `ChatThreadItem`
- Renamed `repeatabilityRequestId` to `idempotencyToken`
- SendMessage returns `SendChatMessageResult` instead of string ID
- Renamed `CommunicationError` to `ChatError`
- Renamed `CommunicationErrorResponse` to `ChatErrorResponse`
- Moved `getChatThread` to `ChatThreadClient` and renamed to `getProperties`
- Removed `AddChatParticipantsOptions`
- Changed `addParticipants` to take `Iterable<ChatParticipant>` instead of `AddChatParticipantsOptions`
- Added `context` parameter to the max overloads of `listParticipants`, `listReadReceipts`
- `CreateChatThreadOptions` constructor now requires `topic`
- Removed `setTopic` from `CreateChatThreadOptions`

### Added

- Added `ChatThreadClientBuilder` 

## 1.0.0-beta.6 (2021-03-09)
Updated `azure-communication-chat` version

## 1.0.0-beta.5 (2021-03-02)
### Breaking Changes

- ChatMessage - `senderId` renamed to `senderCommunicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatMessageReadReceipt - `senderId` renamed to `senderCommunicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatParticipant - `user` renamed to `communicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatThread - `createdBy` renamed to `createdByCommunicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatMessageContent - `initiator` renamed to `initiatorCommunicationIdentifier`, changed type to `CommunicationIdentifier`.


## 1.0.0-beta.4 (2021-02-09)
### Breaking Changes

- Updated to azure-communication-common version 1.0.0-beta.4. Now uses `CommunicationUserIdentifier` and `CommunicationIdentifier` in place of `CommunicationUser`, and `CommunicationTokenCredential` instead of `CommunicationUserCredential`.
- Removed `Priority` field from `ChatMessage`.

### Added

- Added support for `CreateChatThreadResult` and `AddChatParticipantsResult` to handle partial errors in batch calls.
- Added pagination support for `listReadReceipts` and `listParticipants`.
- Added new model for messages and content types: `Text`, `Html`, `ParticipantAdded`, `ParticipantRemoved`, `TopicUpdated`.
- Added new model for errors (`CommunicationError`).
- Added notifications for 'ChatThread' level changes.

## 1.0.0-beta.3 (2020-11-16)
Updated `azure-communication-chat` version

## 1.0.0-beta.2 (2020-10-06)
Updated `azure-communication-chat` version

## 1.0.0-beta.1 (2020-09-22)
This is the initial release of Azure Communication Services for chat. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-chat/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
