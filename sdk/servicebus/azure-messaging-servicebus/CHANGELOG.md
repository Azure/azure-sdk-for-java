# Release History

## 7.9.2 (2022-07-12)

### Features Added
- Exposing `ServiceBusClientBuilder.customEndpointAddress` to support connecting to an intermediary before Azure ServiceBus.

### Other Changes
- Moved message state key to `azure-core-amqp` constants. ([#26898](https://github.com/Azure/azure-sdk-for-java/issues/26898))
#### Dependency Updates
- Upgraded `azure-core` from `1.29.1` to `1.30.0`.
- Upgraded `azure-core-amqp` from `2.5.2` to `2.6.0`.
- Upgraded `azure-identity` from `1.5.2` to `1.5.3`.
## 7.9.1 (2022-06-16)

### Bugs Fixed
- Removed the thread hopping overhead with default max-concurrent-calls; this saves allocations incurred by the parallel operator and addresses the undesired prefetching when prefetch is disabled and max-concurrent-calls is set to default. ([#29011](https://github.com/Azure/azure-sdk-for-java/pull/29011))
- Added default constructor For AuthRulesWrapper to cover corner case when payload returned empty
### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.28.0` to `1.29.1`.
- Upgraded `azure-core-amqp` from `2.5.0` to `2.5.2`.

## 7.9.0 (2022-05-18)

### Features Added
- Replaced creating single thread executor with `Schedulers.boundedElastic()`. ([#27805](https://github.com/Azure/azure-sdk-for-java/issues/27805))
- Added new method `getSessionId()` in `ServiceBusReceiverAsyncClient` . ([#28338](https://github.com/Azure/azure-sdk-for-java/issues/28338))

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.27.0` to `1.28.0`.
- Upgraded `azure-core-amqp` from `2.4.2` to `2.5.0`.

## 7.8.0 (2022-04-11)

### Features Added
- Implemented equals and hashCode methods for CorrelationRuleFilter and SqlRuleFilter objects
### Bugs Fixed
- Entity Name is handled properly when passed in as part of connection string in `ServiceBusClientBuilder`. ([#27509](https://github.com/azure/azure-sdk-for-java/issues/27509))
- Fixed a bug that when the current `SynchronousReceiveWork` is completed, the queued `SynchronousReceiveWork` is not updated. ([#27578](https://github.com/Azure/azure-sdk-for-java/issues/27578))
### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.26.0` to `1.27.0`.
- Upgraded `azure-core-amqp` from `2.4.1` to `2.4.2`.

## 7.7.0 (2022-03-17)
### Features Added
- Added support for sending/receiving messages with `Duration`, `OffsetDateTime` and `URI` in `applicationProperties`.
- Updated the receiver to retry to obtain a new connection if the RequestResponseChannel in the current connection is disposed.
- Added getter methods to `ServiceBusProcessorClient` to get the queue, topic and subscription names associated with
  the processor.

### Bugs Fixed
- Removed the incorrect use of lock primitives from `ServiceBusMessageBatch.tryAddMessage()` implementation and documented that this API is not thread-safe. ([#25910](https://github.com/Azure/azure-sdk-for-java/issues/25910))
- Fixed incorrect ordering of message when sent as a batch. ([#25112](https://github.com/Azure/azure-sdk-for-java/issues/25112), [#25599](https://github.com/Azure/azure-sdk-for-java/issues/25599))
- Fixed a bug that messages come from azure-sdk-for-net with `DescribedType` cannot be deserialized. ([#26065](https://github.com/Azure/azure-sdk-for-java/issues/26065))

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.25.0` to `1.26.0`.
- Upgraded `azure-core-amqp` from `2.4.0` to `2.4.1`.

## 7.6.0 (2022-02-14)

### Features Added
- Added `ServiceBusMessageState` property to received messages which indicates whether the message is active, scheduled or deferred. It is exposed in `ServiceBusReceivedMessage.getMessageState()`. ([#25217](https://github.com/Azure/azure-sdk-for-java/issues/25217))
- `ServiceBusReceiverClient` if the prefetch is disabled and there is no active receive call, release any messages received. ([#26632](https://github.com/Azure/azure-sdk-for-java/issues/26632))

### Bugs Fixed
- Fixed a bug that when received message does not have trace context, span is not created. ([#25182](https://github.com/Azure/azure-sdk-for-java/issues/25182))

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.24.1` to `1.25.0`.
- Upgraded `azure-core-amqp` from `2.3.7` to `2.4.0`.

## 7.5.2 (2022-01-14)

### Bugs Fixed
- Fixed lock renewal delay behavior, renewal delay is more relaxed, with it happening at half the remaining time now. Issue [25259](https://github.com/Azure/azure-sdk-for-java/issues/25259).

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.23.1` to `1.24.1`.
- Upgraded `azure-core-amqp` from `2.3.5` to `2.3.7`.

## 7.5.1 (2021-12-08)
### Bugs Fixed
- Fixed a bug where Synchronous Receiver client stops receiving messages if MaxMessages is greater than 1. Issue [25063](https://github.com/Azure/azure-sdk-for-java/issues/25063).

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.22.0` to `1.23.1`.
- Upgraded `azure-core-amqp` from `2.3.4` to `2.3.5`.

## 7.5.0 (2021-11-16)
### Features Added
- Moved to service API version `2021-05`
- Added support for specifying the `MaxMessageSizeInKilobytes` for entities in Premium namespaces.

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.21.0` to `1.22.0`.
- Upgraded `azure-core-amqp` from `2.3.3` to `2.3.4`.

## 7.4.2 (2021-10-15)
### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.20.0` to `1.21.0`.
- Upgraded `azure-core-amqp` from `2.3.2` to `2.3.3`.

## 7.4.1 (2021-09-13)
### Bugs Fixed
- Fixed a bug that was causing leaking of boundedElastic-evictor threads associated with the `ServiceBusSessionManager` objects. Issue [23539](https://github.com/Azure/azure-sdk-for-java/issues/23539).

### Other Changes
#### Dependency Updates
- Update `azure-core` dependency to `1.20.0`.
- Update `azure-core-amqp` dependency to `2.3.2`.

## 7.4.0 (2021-08-20)
### Features Added
- Updated ServiceBusAdministrationClientBuilder to Support HttpPipelinePosition.

### Other Changes
#### Dependency Updates
- Update `azure-core` dependency to `1.19.0`.
- Update `azure-core-amqp` dependency to `2.3.1`.

## 7.3.0 (2021-07-08)
### Features Added
- Added support for receiving dead-letter queue (DLQ) for `ServiceBusProcessorClient` and session receiver using `subQueue`
- Added support for configuring `maxAutoLockRenewDuration` for `ServiceBusProcessorClient` in `ServiceBusClientBuilder`.
- Added support for using `AzureSasCredential` and `AzureNamedKeyCredential` to access a service bus in `ServiceBusClientBuilder`.
- Exposing `ServiceBusClientBuilder.crossEntityTransaction()` for cross entity transactions support.
- Exposing `AmqpMessageBody#value` and `AmqpMessageBody#sequence` to support value and sequence amqp types.

### Bugs Fixed
- Fixed a bug that does not create respect properties when creating Subscription Rule with CorrelationFilter. Issue [21299](https://github.com/Azure/azure-sdk-for-java/issues/21299).
- Fixed a but that causes the settlement API not to return or throw error if retry timeout is long enough. Issue [22299](https://github.com/Azure/azure-sdk-for-java/issues/22299).

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`.
- Upgraded `azure-core-amqp` dependency to `2.3.0`.
- Upgraded `azure-core-http-netty` to `1.10.1`.

## 7.2.3 (2021-06-14)
### Fixed
Fixed the issue that the second call of `ServiceBusReceiverClient.complete` is stuck when connection is broken.

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to `1.17.0`.
- Upgraded `azure-core-amqp` dependency to `2.2.0`.
- Upgraded `azure-core-http-netty` to `1.10.0`.

## 7.2.2 (2021-05-26)
### Fixed
- Fixed some connection retry issues when network errors happen.
- Fixed an issue that caused `ServiceBusSenderClient` to keep running after it's already closed.

### Other Changes
#### Dependency Updates
- Upgraded `azure-core-amqp` dependency to `2.0.6`.

## 7.2.1 (2021-05-12)
### Fixed
- Fixed an issue: When 'ServiceBusProcessorClient:maxConcurrentCalls' is set, this will result in SDK cache more 
  messages that are not delivered to the client in time and sometime the client is not able to settle these messages as
  the message lock might expire.

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to `1.16.0`.
- Upgraded `azure-core-amqp` dependency to `2.0.5`.

## 7.3.0-beta.1 (2021-04-14)
### New Features
- Adding support for AMQP Data types SEQUENCE and VALUE. It support sending and receiving of only one AMQP Sequence at 
  present. Issue [17614](https://github.com/Azure/azure-sdk-for-java/issues/17614).
- Adding support for `maxAutoLockRenewDuration()` on `ServiceBusProcessorClientBuilder`.

## 7.2.0 (2021-04-12)
### Bug Fixes
- Fix issue [19923](https://github.com/Azure/azure-sdk-for-java/issues/19923) for session receiver only: Fix a silent 
  error 'java.lang.ArithmeticException: long overflow' by not starting 'LockRenewOperation' for each received message.
- Upgrade to `azure-core-amqp:2.0.4` improves recovery of connection to Service Bus.

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to `1.15.0`.
- Upgraded `azure-core-amqp` dependency to `2.0.4`.

## 7.2.0-beta.1 (2021-03-18)
### New Features
- Added support for distributed transactions across entities via API 'ServiceBusClientBuilder.enableCrossEntityTransactions()'.

## 7.1.0 (2021-03-10)
### Bug Fixes
- Continue to receive messages regardless of user not settling the received message in PEEK_LOCK mode [#19247](https://github.com/Azure/azure-sdk-for-java/issues/19247).
- Update to end the trace span regardless of the scope instance type for process operation tracing spans.
- Removed logs that leaked secrets when connection string is invalid. [#19249](https://github.com/Azure/azure-sdk-for-java/issues/19249)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to `1.14.0`.
- Upgraded `azure-core-amqp` dependency to `2.0.3`.

## 7.0.2 (2021-02-10)
### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`.
- Upgraded `azure-core-amqp` dependency to `2.0.2`.

## 7.0.1 (2021-01-15)
### New Features
- Improve performance because by upgrading `azure-core-amqp` dependency to `2.0.1`. It Changes AMQP connections from 
  sharing the global `Schedulers.single()` to having a `Scheduler.newSingle()` per connection.

### Bug Fixes
- Fix issue [18351](https://github.com/Azure/azure-sdk-for-java/issues/18351): Getting 'NullPointerException' When calling 
  'ServiceBusAdministrationAsyncClient#getSubscriptionRuntimeProperties()' for the topic where user has only listen 
  (and not manage) permission.
- Fix issue [18122](https://github.com/Azure/azure-sdk-for-java/issues/18435): A session-based Message receiver does not 
  receive messages sent after 60s gap from the last message sent. This happens if there is only one active session in
  Service Bus entity.
- Fix issue [18536](https://github.com/Azure/azure-sdk-for-java/issues/18536): The 'ServiceBusAdministrationClient.deleteSubscription()'
  is not synchronous.

### Other Changes
#### Dependency Updates   
- Upgraded `azure-core` dependency to `1.12.0`.
- Upgraded `azure-core-amqp` dependency to `2.0.1`.

## 7.0.0 (2020-11-30)

### New Features
- Exposing enum 'ServiceBusFailureReason' in 'ServiceBusException' which contains a set of well-known reasons for an
  Service Bus operation failure.
- Added 'BinaryData' support to  'ServiceBusReceivedMessage' and 'ServiceBusMessage'. It provides an easy abstraction 
  over many different ways that binary data can be represented. It also provides support for serialize and deserialize
  Object.
- Introducing 'ServiceBusProcessorClient': It provides a push-based mechanism that invokes the message processing 
  callback when a message is received or the error handler when an error occurs when receiving messages. It supports 
  auto-settlement of messages by default.

### Breaking Changes
- Renamed all the 'peekMessageAt()' API to 'peekMessage()' in 'ServiceBusReceiverAsyncClient' and 
  'ServiceBusReceiverClient'.
- Rename 'getAmqpAnnotatedMessage()' to 'getRawAmqpMessage()' in 'ServiceBusReceivedMessage' and 'ServiceBusMessage'.

### Bug Fixes
- Set the default 'prefetch' to 0 instead of 1 in both 'RECEIVE_AND_DELETE' and 'PEEK_LOCK' mode. User can set this 
  value in builder.

### Known issues
- Can not resolve `BinaryData` or `NoClassDefFoundError` 
  NoClassDefFoundError When using `azure-messaging-servicebus:7.0.0` and other Azure SDKs in the same pom.xml file.
  Check [here][known-issue-binarydata-notfound] for more details.

### Other Changes
#### Dependency Updates   
- Upgraded `azure-core` dependency to `1.11.0`.
- Upgraded `azure-core-amqp` dependency to `2.0.0`.
  
## 7.0.0-beta.7 (2020-11-06)
### New Features
- Added automatic message and session lock renewal feature on the receiver clients. By default, this will be done 
  for 5 minutes.
- Added auto complete feature to the async receiver clients. Once the client completes executing the user provided 
  callback for a message, the message will be completed. If the user provided callback throws an error, the message 
  will be abandoned. This feature is enabled by default and can be disabled by calling `disableAutoComplete()` on 
  builder. 
- An intermediate `ServiceBusSessionReceiverClient` is introduced to act as the factory which can then be used to accept 
  sessions from the service. Accepting a session would give you the familiar receiver client tied to a single session.
- Added `ServiceBusProcessorClient` which takes your callbacks to process messages and errors in an infinite loop. This 
  also supports working with sessions where you can provide the maximum number of sessions to work with concurrently. 
  When the client no longer receives any messages from one session, it rolls over to the next available session.
- Added `BinaryData` in `ServiceBusReceivedMessage` and `ServiceBusMessage`. `BinaryData` is convenience wrapper over
  byte array and provides object serialization functionality.
- Added `ServicebusReceiverException` and `ServiceBusErrorSource` to provide better handling of errors while receiving 
  messages.

### Breaking Changes
- Changed `receiveMessages` API to return `ServiceBusReceivedMessage` instead of ServiceBusReceivedMessageContext in 
  `ServiceBusReceiverAsynClient` and `ServiceBusReceiverClient`.
- Removed `SendVia` option from `ServiceBusClientBuilder`. See issue for more detail 
  [16942](https://github.com/Azure/azure-sdk-for-java/pull/16942).
- Removed `sessionId` setting from `ServiceBusSessionReceiverClientBuilder` as creating receiver clients bound to a 
  single session is now a feature in the new intermediate clients `ServiceBusSessionReceiverClient` and 
  `ServiceBusSessionReceiverAsyncClient`.
- Moved the `maxConcurrentSessions` setting from `ServiceBusSessionReceiverClientBuilder` to 
  `ServiceBusSessionProcessorClientBuilder` as the feature of receiving messages from multiple sessions is moved from 
  the receiver client to the new `ServiceBusSessionProcessorClient`.
- Renamed `tryAdd` to `tryAddMessage` in `ServiceBusMessageBatch`.
- Removed `sessionId` specific methods from `ServiceBusReceiverAsynClient` and `ServiceBusReceiverClient` because now 
  receiver client is always tied to one session. 
  
### Bug Fixes
- `ServiceBusAdministrationClient`: Fixes serialization bug for creating and deserializing rules.

### Other Changes
#### Dependency Updates
- Added new `azure-core-experimental` dependency with version `1.0.0-beta.8`.
- Upgraded `azure-core` dependency to `1.10.0`.
- Upgraded `azure-core-amqp` dependency to `1.7.0-beta.1`.

## 7.0.0-beta.6 (2020-09-11)
- Add Amqp Message envelope in form of `AmqpAnnotatedMessage` as a property of `ServiceBusReceivedMessage` and
  `ServiceBusMessage`.
- Remove `ServiceBusReceiverClientBuilder.maxAutoLockRenewalDuration`. Use method `renewMessageLock` and
  `renewSessionLock` of classes `ServiceBusReceiverClient` and `ServiceBusReceiverAsyncClient` to lock messages and
  sessions.
- Update datetime related APIs to use `java.time.OffsetDateTime` instead of `java.time.Instant`.
- Remove `scheduledMessageCount` from `SubscriptionRuntimeInfo` and added it to `TopicRuntimeInfo`.
- Change `QueueRuntimeInfo`, `TopicRuntimeInfo` and `SubscriptionRuntimeInfo` to `QueueRuntimeProperties`,
  `TopicRuntimeProperties` and `SubscriptionRuntimeProperties` respectively.
- Add ability to authenticate using SAS.
- Add support for `AuthorizationRules` during management operations.
- Rename `ServiceBusManagementClient` to `ServiceBusAdministrationClient`.
- Remove `ServiceBusDeadletterReceiverBuilder` and replaced with `SubQueue` type to access transfer deadletter and 
  deadletter queue.
- Remove settlement operations that take `String lockToken`, replaced with `ServiceBusReceivedMessage`.

## 7.0.0-beta.5 (2020-08-11)
- Remove public constructor for QueueDescription, TopicDescription, SubscriptionDescription.
- Expose CreateQueueOptions, CreateTopicOptions, CreateSubscriptionOptions to create entities.
- Flatten and remove MessageCountDetails in QueueRuntimeInfo, TopicRuntimeInfo, and SubscriptionRuntimeInfo.
- Limiting visibility of properties on QueueDescription, TopicDescription, SubscriptionDescription to only those that
  can be updated.
- Added a short timeout of 1 second in between messages for sync receive only.

## 7.0.0-beta.4 (2020-07-10)
- Add support for send messages via another entity.
- Add support for management operations on a topic, subscription, or namespace.
- Add support for receiving messages from the dead letter queue.
- Change suffixes for receive methods by adding `Message` or `Messages`.
- Remove `MessageLockToken` interface in favour of passing a lock token string.

## 7.0.0-beta.3 (2020-06-08)
- Add support for transaction feature in all the clients.
- Add support for management operations on a Queue.

## 7.0.0-beta.2 (2020-05-07)

- Add support for receiving messages from specific sessions
- Add support for receiving messages from multiple sessions
- Add missing schedule and cancel APIs in ServiceBusSenderClient
- Add support to send a collection of messages at once without needing to create a `ServiceBusMessageBatch` first. This
  will throw an error/exception will the messages cannot fit as per batch size restrictions
- Change return type from `ServiceBusReceivedMessage` to `ServiceBusReceivedMessageContext` when calling `receive()` so
  users can distinguish between transient failure scenarios where receiving continues and an actual terminal signal that
  is signaled through the downstream `onError`.
- Fix message settlement to occur on receive link
- Fix issue where backpressure is not properly supported

## 7.0.0-beta.1 (2020-04-06)

Version 7.0.0-beta.1 is a beta of our efforts in creating a client library that is developer-friendly, idiomatic
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for
.Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

### Features

- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Send messages to an Azure Service Bus Topic or Queue.
- Receive messages from an Azure Service Bus Queue or Subscriber.

[known-issue-binarydata-notfound]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/known-issues.md#can-not-resolve-binarydata-or-noclassdeffounderror-version-700

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FCHANGELOG.png)
