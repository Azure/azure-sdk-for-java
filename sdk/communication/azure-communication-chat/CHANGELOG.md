## Release History

### 1.2.4 (2022-07-14)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.5
- Upgraded `azure-core` to 1.30.0

### 1.2.3 (2022-06-15)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.4
- Upgraded `azure-core` to 1.29.1

### 1.2.2 (2022-05-13)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.3
- Upgraded `azure-core` to 1.28.0

### 1.2.1 (2022-04-12)
### Other Changes
#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.2
- Upgraded `azure-communication-identity` to 1.1.8

### 1.2.0 (2022-03-11)
#### Features Added
- Added interfaces from `com.azure.core.client.traits` to `ChatClientBuilder` and `ChatThreadClientBuilder`
- Added `retryOptions` to `ChatClientBuilder` and `ChatThreadClientBuilder`

### Other Changes

#### Dependency Updates
- Upgraded `azure-communication-common` to 1.1.1
- Upgraded `azure-communication-identity` to 1.1.7

### 1.1.4 (2022-02-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.8

### 1.1.3 (2022-01-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.7
- Upgraded `azure-core` to 1.24.1

### 1.1.2 (2021-11-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.6
- Upgraded `azure-core` to 1.22.0

### 1.1.1 (2021-10-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.5
- Upgraded `azure-communication-identity` to 1.1.3
- Upgraded `azure-core` to 1.21.0

### 1.1.0 (2021-09-15)
- Added javadoc code samples
- Removed redundant overload `ChatThreadAsyncClient.sendTypingNotification(TypingNotificationOptions options)`
- Upgraded `azure-communication-common` to 1.0.4

### 1.1.0-beta.2 (2021-08-10)
- Fix version of dependency on azure-communication-common

### 1.1.0-beta.1 (2021-08-10)
- Added method `ChatThreadAsyncClient.listParticipants(ListParticipantsOptions listParticipantsOptions)`
- Added method `ChatThreadAsyncClient.listReadReceipts(ListReadReceiptOptions listReadReceiptOptions)`
- Added support for metadata in messages.
- Added options class `TypingNotificationOptions` for setting `SenderDisplayName` of the notification sender.

### 1.0.1 (2021-05-27)
- Dependency versions updated.

### 1.0.0 (2021-03-29)
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

### 1.0.0-beta.6 (2021-03-09)
Updated `azure-communication-chat` version

### 1.0.0-beta.5 (2021-03-02)
### Breaking Changes

- ChatMessage - `senderId` renamed to `senderCommunicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatMessageReadReceipt - `senderId` renamed to `senderCommunicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatParticipant - `user` renamed to `communicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatThread - `createdBy` renamed to `createdByCommunicationIdentifier`, changed type to `CommunicationIdentifier`.
- ChatMessageContent - `initiator` renamed to `initiatorCommunicationIdentifier`, changed type to `CommunicationIdentifier`.


### 1.0.0-beta.4 (2021-02-09)
### Breaking Changes

- Updated to azure-communication-common version 1.0.0-beta.4. Now uses `CommunicationUserIdentifier` and `CommunicationIdentifier` in place of `CommunicationUser`, and `CommunicationTokenCredential` instead of `CommunicationUserCredential`.
- Removed `Priority` field from `ChatMessage`.

### Added

- Added support for `CreateChatThreadResult` and `AddChatParticipantsResult` to handle partial errors in batch calls.
- Added pagination support for `listReadReceipts` and `listParticipants`.
- Added new model for messages and content types: `Text`, `Html`, `ParticipantAdded`, `ParticipantRemoved`, `TopicUpdated`.
- Added new model for errors (`CommunicationError`).
- Added notifications for 'ChatThread' level changes.

### 1.0.0-beta.3 (2020-11-16)
Updated `azure-communication-chat` version

### 1.0.0-beta.2 (2020-10-06)
Updated `azure-communication-chat` version

### 1.0.0-beta.1 (2020-09-22)
This is the initial release of Azure Communication Services for chat. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-chat/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
