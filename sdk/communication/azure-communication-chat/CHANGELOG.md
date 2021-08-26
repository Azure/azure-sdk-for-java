# Release History

## 1.1.0-beta.3 (2021-08-27)
- Using released version of azure-communication-common.

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
