# Release History

## 2.10.0-beta.1 (Unreleased)

### Features Added

- Integrated RequestResponseChannelCache (CBS, Management channel cache) and ReactorSessionCache, removing the dependency on AmqpChannelProcessor. ([39107](https://github.com/Azure/azure-sdk-for-java/pull/39107))

### Breaking Changes

### Bugs Fixed

- Fixes the endpoint state subscription to log errors using ClientLogger so that error do not reach Reactor global OnErrorDropped hook. ([41637](https://github.com/Azure/azure-sdk-for-java/issues/41637))
- Fixes thread unsafe operation in AMQP session by introducing ReactorSessionCache. ([39107](https://github.com/Azure/azure-sdk-for-java/pull/39107))

### Other Changes

## 2.9.8 (2024-07-31)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.50.0` to `1.51.0`.

## 2.9.7 (2024-07-12)

### Bugs Fixed
- Removing duplicate ReactorSession::closeAsync call in session-endpoint error-handler. ([#40667](https://github.com/Azure/azure-sdk-for-java/pull/40667))

## 2.9.6 (2024-06-11)

### Features Added

- Added feature to enable/disable SSL when initially creating connection to support AMQP calls on port 5672.

## 2.9.5 (2024-06-06)

### Bugs Fixed

- Expose and use port when parsing `ConnectionStringProperties`. ([#40415](https://github.com/Azure/azure-sdk-for-java/pull/40415))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.
- Upgraded Reactor Core from `3.4.36` to `3.4.38`.

## 2.9.4 (2024-05-01)

### Features Added

- `WindowedSubscriber` to translate the asynchronous stream of events or messages to `IterableStream` ([38705](https://github.com/Azure/azure-sdk-for-java/pull/38705)).

### Other Changes

- Improvements to logging. ([#39904](https://github.com/Azure/azure-sdk-for-java/pull/39904))

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.
- Upgraded `qpid-proton-j-extensions` from `1.2.4` to `1.2.5`.

## 2.9.3 (2024-04-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.
- Upgraded `proton-j` from `0.33.8` to `0.34.1`.
- Upgraded Reactor Core from `3.4.34` to `3.4.36`.

## 2.9.2 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.

## 2.9.1 (2024-02-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.

## 2.9.0 (2024-01-17)

### Bugs Fixed

- Retry connection on timeout ([38317](https://github.com/Azure/azure-sdk-for-java/pull/38317))

### Features Added

- The version 2.9.0 is the stable release for all the features introduced in the 2.9.0-beta.* versions.

## 2.8.14 (2023-11-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.0` to `1.45.1`.

## 2.9.0-beta.7 (2023-11-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.

## 2.8.13 (2023-11-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0` (The version 2.8.12 used earlier azure-core dependency version 1.44.1 instead of latest 1.45.0. This 2.8.13 patch release fixes it).

## 2.8.12 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.

## 2.9.0-beta.6 (2023-11-01)

### Bugs Fixed

- Fixes the ReactorReceiver to probe the monitoring meter only once to see if tracking message sequence number is needed.
- Fixes the MessageFlux to not use the doOnEach side effect operator for reacting to endpoint active and terminal states.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.44.1`.

## 2.8.11 (2023-10-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.

## 2.8.10 (2023-10-06)

### Bugs Fixed

- Fixes the potential NullPointerException in ReactorSession if the thread constructing ReactorSession ever happens to run the disposeWork (cleanup phase) synchronously. ([36916](https://github.com/Azure/azure-sdk-for-java/issues/36916))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 2.8.9 (2023-09-07)

### Bugs Fixed

- Fixes the NullPointerException in RequestResponseChannel when the thread constructing RequestResponseChannel happens to run the cleanup phase synchronously. ([36607](https://github.com/Azure/azure-sdk-for-java/issues/36607))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 2.9.0-beta.5 (2023-08-11)

### Features Added

- Update ReceiverUnsettledDeliveries::sendDisposition(,) API to return DeliveryNotOnLinkException if the link is closed (hence delivery map cleared) or the DeliveryMap has no matching delivery. This simplifies the implementation of disposition attempt on management channel.
- Adding support to turn off retry in MessageFlux hence propagating error or completion event from the first receiver to downstream, this allows using MessageFlux in the cases that need proper credit accounting but not rolling to another receiver.

### Other Changes

- Upgraded `azure-core` from `1.41.0` to `1.42.0`

## 2.8.8 (2023-08-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0`.

## 2.9.0-beta.4 (2023-07-18)

### Bugs Fixed

- Removing the unnecessary scheduling to Scheduler while waiting for receiver link activation, reducing the subscription count to receiver link endpoint states, addressing subscription leak when switching to the new receiver link.

### Features Added

- Adding common ReceiversPumpingScheduler for internal message pumping by the *ReactorReceiver types.

### Other Changes

- Beta baselined to 2.9.0-beta.2.
- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 2.8.7 (2023-07-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 2.9.0-beta.3 (2023-07-06)

### Other Changes

- This version is the same as June-2023 2.8.6 version, except the azure-core is updated to 1.41.0. I.e., this version is not baselined to 2.9.0-beta.2.

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 2.9.0-beta.2 (2023-06-13)

### Features Added

- Prefetch reliability redesign. Adds MessageFlux, new credit accounting, better connection caching that removes thread hopping, handling of disposition ack on arrival thread, and reduces the Reactor operator queues in the async pipelines.

## 2.8.6 (2023-06-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 2.8.5 (2023-05-04)

### Bugs Fixed

- Addressing the overhead of batch send allocating byte array equal to the max message size. ([34426](https://github.com/Azure/azure-sdk-for-java/issues/34426))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 2.8.4 (2023-04-07)

### Bugs Fixed

- Addressing IllegalStateException due to double free of Connection reference by the Transport.
- Fixes bug where `Message.messageId` and `Message.groupId` are not set on the uber message when sending a collection of messages.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to `1.38.0`.

## 2.8.3 (2023-03-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to `1.37.0`.

## 2.8.2 (2023-02-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to `1.36.0`.

## 2.9.0-beta.1 (2023-01-30)

### Features Added

- Added ReactorConnectionCache to simplify the Amqp Connection recovery ([#33224](https://github.com/Azure/azure-sdk-for-java/issues/33224))

## 2.8.1 (2023-01-05)

### Bugs Fixed

- Removing inactive session when it has timed out, so `ReactorConnection.getSession(String)` does not return the same session.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to `1.35.0`.

## 2.8.0 (2022-11-04)

### Bugs Fixed

- Updating the host value for the Websocket upgrade request to match with the HTTP host ([31825](https://github.com/Azure/azure-sdk-for-java/issues/31825))
- Enabling HTTP Proxy for custom endpoint and updating Proxy CONNECT request to use the actual front-end host ([31826](https://github.com/Azure/azure-sdk-for-java/issues/31826))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to `1.34.0`.

## 2.7.2 (2022-10-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to `1.33.0`.
- Upgraded Reactor from `3.4.22` to `3.4.23`.

## 2.7.1 (2022-09-01)

### Features Added

- Added AMQP-level metrics to reporting number of sent batches, duration of network call, number of received message and consumer lag as well as
  error counters. Metrics are off by default and can be enabled with [azure-core-metrics-opentelemetry](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-metrics-opentelemetry/README.md)
  plugin. ([#30583](https://github.com/Azure/azure-sdk-for-java/pull/30583))

### Other Changes

#### Dependency Updates

- Upgraded Reactor from `3.4.21` to `3.4.22`.

## 2.7.0 (2022-08-05)

### Features Added

- Added `AmqpClientOptions` to enable set identifier for AMQP client and link. ([#22981](https://github.com/Azure/azure-sdk-for-java/issues/22981))

### Other Changes

#### Dependency Updates

- Upgraded Reactor from `3.4.19` to `3.4.21`.

## 2.6.0 (2022-06-30)

### Features Added

 - Added `AmqpMessageConstant` `MESSAGE_STATE_ANNOTATION_NAME("x-opt-message-state")`.

### Bugs Fixed

- Ensure ReactorReceiver EndpointStates terminates if there is no remote-close acknowledgment ([#29212](https://github.com/Azure/azure-sdk-for-java/issues/29212))
- Fixed issue that when connection is closed, the `AmqpChannelProcessor` repeatedly requests and closes `RequestResponseChannel`. ([#24582](https://github.com/Azure/azure-sdk-for-java/issues/24582))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to `1.30.0`.

## 2.5.2 (2022-06-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.0` to `1.29.1`.

## 2.5.1 (2022-06-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to `1.29.0`.

- Added "entityPath" context to logger for ReceiveLinkHandlers, SendLinkHandler, LinkHandler, and ReactorReceiver.

## 2.5.0 (2022-05-06)

### Features Added

- Added `ProxyOptions.fromConfiguration(Configuration)` to enable creation of `ProxyOptions` from an environment
  configuration.

### Bugs Fixed

- Fixed proxy authentication type not being read from configuration. ([#28073](https://github.com/Azure/azure-sdk-for-java/issues/28073))
- Updated ProxyOptions.SYSTEM_DEFAULTS to use `ProxyAuthenticationType.NONE`

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to `1.28.0`.

## 2.4.2 (2022-04-01)

### Bugs Fixed

- Fixed an issue where error from one receiver bypassed to parent connection that resulted in taking down rest of the
  receivers. ([#27716](https://github.com/Azure/azure-sdk-for-java/issues/27716))
- Downgraded the level of a log entry in RequestResponseChannel from error to warn, the sender and receiver often
  recover from this error, but due to the log level, it generates false alerts in monitoring systems. ([26968](https://github.com/Azure/azure-sdk-for-java/issues/26968))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to `1.27.0`.

## 2.4.1 (2022-03-16)

### Bugs Fixed

- Fixed issue where EndpointStates were not emitted serially. ([#24762](https://github.com/Azure/azure-sdk-for-java/issues/24762))
- Fixed issue of not emitting the shutdown signal serially when ClosedChannelException thrown concurrently. ([#27320](https://github.com/Azure/azure-sdk-for-java/issues/27320))
- Fixed the issue of leaving downstream in an unterminated state when RequestResponseChannel is disposed after invoking sendWithAck ([27482](https://github.com/Azure/azure-sdk-for-java/issues/27482))
- Removing CustomIOHandler.onUnhandled which listens to every proton-j reactor event that could cause excessive logging. The underlying library could encounter `NullPointerException` if the selector is null.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to `1.26.0`.

## 2.4.0 (2022-02-04)

### Features Added

- Added `AmqpTrait` interface that represent common cross-cutting (and AMQP-related) aspects of functionality offered
  by libraries in the Azure SDK for Java.
- Added structured logging to generate more easily queried log messages. ([#26561](https://github.com/Azure/azure-sdk-for-java/pull/26561))

### Bugs Fixed

- Fixed a bug which resulted in higher than needed memory consumption when sending messages. ([#26373](https://github.com/Azure/azure-sdk-for-java/pull/26373))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to `1.25.0`.

## 2.3.7 (2022-01-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.0` to `1.24.1`.
- Upgraded Reactor from `3.4.12` to `3.4.13`.

## 2.3.5 (2021-12-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to `1.23.1`.

## 2.3.4 (2021-11-15)

### Bugs Fixed

- Fixed GC leak where `AmqpChannelProcessor` did not remove subscribers on success. https://github.com/Azure/azure-sdk-for-java/pull/25129
- Fixed GC leak where `TokenManager` was not closed if `authorize()` fails. https://github.com/Azure/azure-sdk-for-java/pull/25129


### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to `1.22.0`.

## 2.3.3 (2021-10-07)

### Features Added

- Defer creation of AmqpException in switchIfEmpty to decrease creation of unused exception objects.
- Updated tests to run in parallel.
- Updated log messages so they are consistent in reporting connectionId.
- Updated incorrect log messages in ReactorReceiver.

### Bugs Fixed

- Fixed issue where RequestResponseChannel did not complete pending sends on disposal. So, any downstream subscribers would wait forever for a completion or error. This results in dependent senders or receivers not recovering from a disconnect or graceful closure.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.20.0` to `1.21.0`.
- Upgraded Reactor from `3.4.9` to `3.4.10`.
- Upgraded Jackson from `2.12.4` to `2.12.5`.

## 2.3.2 (2021-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.19.0` to `1.20.0`.
- Upgraded Reactor from `3.4.8` to `3.4.9`.

## 2.3.1 (2021-08-19)

### Bug Fixes

- Fixed a bug where SendTimeout-timer thread was not being disposed of resulting in lingering
  threads when a send link was remotely closed.
- Fixed a bug where ReactorConnection waited indefinitely for CBS node to complete closing. The underlying problem is
  that the RequestResponseChannel's sender and receiver links were not active, so they would wait forever for a remote
  close signal.
- Fixed a bug where ReactorReceiver and ReactorSender would not complete their close operation if their close work could
  not be scheduled on the proton-j Reactor. This happens in the case that the connection is shutdown before the link.
- Fixed a bug where RejectedExecutionExceptions and IllegalStateExceptions would not be retried. This happens in the
  case that an IO pipe is interrupted while signalling the Reactor work queue.

## 2.3.0 (2021-07-01)

### Features Added

- Added `AmqpTransactionCoordinator` interface for transactions support.
- Added support for sequence and value AMQP types in `AmqpMessageBody`.

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 2.2.0 (2021-06-11)

### New Features

- Exposing CbsAuthorizationType.
- Exposing ManagementNode that can perform management and metadata operations on an AMQP message broker.
- AmqpConnection, AmqpSession, AmqpSendLink, and AmqpReceiveLink extend from AsyncCloseable.
- Delivery outcomes and delivery states are added.

### Bug Fixes

- Fixed a bug where connection and sessions would not be disposed when their endpoint closed.
- Fixed a bug where ReactorExecutor did not dispose of its scheduler when "IO Sink was interrupted".

### Dependency Updates

- Upgraded `azure-core` from `1.16.0` to `1.17.0`.
- Upgraded `proton-j` from `0.33.4` to `0.33.8`.
- Upgraded `qpid-proton-j-extensions` from `1.2.3` to `1.2.4`.

## 2.0.6 (2021-05-24)
### Bug Fixes
- Fixed a bug that caused AMQP connection not to retry when network error happened.

## 2.0.5 (2021-05-07)

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.
- Upgraded Reactor from `3.4.3` to `3.4.5`.

## 2.2.0-beta.1 (2021-04-14)
### New Features
- Adding support for AMQP data types SEQUENCE and VALUE.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.15.0`.

## 2.0.4 (2021-04-12)

### Bug Fixes

- Fixed recovery of AMQP connection and receiver after a disconnect or a transient error occurs.
- Closing AMQP sender/receiver when it is no longer authorized.
- Fixed bug where the same endpoint state would not be emitted.
- Decreased the number of duplicated and verbose logs.
- Fixed NullPointerExceptions where there is no connection to initialize.
- Fixed issue with contending threads trying to use the same drain loop via 'wip' in ReactorDispatcher.

## 2.1.0-beta.1 (2021-03-26)

### New Features
- Exposes 'AmqpTransactionCoordinator' via AmqpSession.
- Added API in interface 'AmqpSession.getOrCreateTransactionCoordinator()'.

## 2.0.3 (2021-03-09)

### Bug Fixes

- Fixed a bug where using a proxy the SSL peer hostname was set incorrect.
- Removed logs that leaked secrets. [#19249](https://github.com/Azure/azure-sdk-for-java/issues/19249)

### Version Updates

- Upgraded Reactor from `3.3.12.RELEASE` to `3.4.3`.

## 2.0.2 (2021-02-05)

### New Features

- Updates RetryUtil to use RetrySpec.
- Adds the ability to configure the `hostname` and `port` that will be used when connecting to a service via
  `ConnectionOptions`. The `hostname` field refers to the DNS host or IP address of the service, whereas the
  `fullyQualifiedNamespace` is the fully qualified host name of the service.
  Normally `hostname` and `fullyQualifiedNamespace` will be the same. However, if your network does not allow
  connecting to the service via the public host, you can specify a custom host (e.g. an application gateway) via the
  `hostname` field and continue using the public host as the `fullyQualifiedNamespace`.

## 2.0.1 (2021-01-11)

### New Features

- Changed connections from sharing the global `Schedulers.single()` to having a `Scheduler.newSingle()` per connection
  to improve performance.

## 2.0.0 (2020-11-30)
### New Features
- Added 'AmqpAddress' as a type to support 'AmqpMessageProperties#replyTo' and 'AmqpMessageProperties#to' properties.
- Added 'AmqpMessageId' as a type to support 'AmqpMessageProperties#correlationId' and 'AmqpMessageProperties#messageId'
  properties.
- Added static methods to instantiate 'AmqpMessageBody' for example 'AmqpMessageBody#fromData(byte[])'.

### Breaking Changes
- Changed  'AmqpMessageBody' from an interface to a class. User can use 'getBodyType()' to know what is the
  'AmqpBodyType' of the message.
- Changed type of 'correlationId' and 'messageId' in type 'AmqpMessageProperties' from 'String' to 'AmqpMessageId'.
- Changed type of 'replyTo' and 'to' in type 'AmqpMessageProperties' from 'String' to 'AmqpAddress'.
- Removed copy constructor for 'AmqpAnnotatedMessage'.
- Renamed 'AmqpBodyType' to 'AmqpMessageBodyType'.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.11.0`.

## 1.7.0-beta.2 (2020-11-10)
### New Features
- Optionally enable idempotency of a send link to send AMQP messages with producer group id, producer owner level and
  producer sequence number in the message annotations.

## 1.7.0-beta.1 (2020-11-03)
### Dependency Updates
- Upgraded `azure-core` dependency to `1.10.0`.

## 1.6.0 (2020-10-12)
### New Features
- Added peer certificate verification options when connecting to an AMQP endpoint.
### Breaking Changes
- Removed `BinaryData` type which was used for `AmqpAnnotatedMessage`.
### Dependency Updates
- Upgraded `azure-core` dependency to `1.9.0`.

## 1.5.1 (2020-09-10)
- Add support for SAS when authenticating.

## 1.5.0 (2020-09-10)
- Remove unused and duplicate logic for Handlers.getErrors().
- Close children sessions and links when a connection is disposed.
- Added AMQP Message envelope which can be accessed using `AmqpAnnotatedMessage`.

## 1.4.0 (2020-08-11)

- Settles AMQP deliveries that were received after local link state changed to CLOSED
- Add credits to AMQP link only if the receiver is not disposed.

## 1.3.0 (2020-07-02)

- Added `createProducer` constructor which takes an additional parameter for link properties.
- Fixes `User-Agent` string to follow guidelines.

## 1.2.0 (2020-06-08)
- Fixes bug where receiver would not recover after network connection loss.
- Fixes bug where multiple retries (the session would retry in addition to the link) were occurring when creating a new AMQP channel.
- Fixes bug where credits were not added to new AMQP receive links upon recreation.
- Adds AMQP framing error condition.
- Add support for AMQP transactions.

## 1.1.2 (2020-05-07)

- Fixes dependency version of `azure-core`.

## 1.1.1 (2020-05-07)
- Bug fix to get link size from service the first time before sending message from `ReactorSender`.

## 1.1.0 (2020-05-04)

- Update proton-j dependencies to support larger web socket frame and module name.
- Fixes authorization problems with ClaimsBasedSecurityNode not verifying response.
- Adds proper support for backpressure to downstream subscribers.
- Updates connection string parsing to no longer validate URL scheme.

## 1.0.2 (2020-04-03)

- Fix bug where management channel would not be re-created upon closing.

## 1.0.1 (2020-02-11)

- Client library name and version are no longer hard coded in connection properties.
- Update qpid-proton-j-extensions dependency
- Shorten tracing span names
- Fixes AMQP link handlers not to close associated sessions when they are closed.
- Move to use Schedulers.single() because pushing to qpid-proton-j reactor is not thread-safe.

## 1.0.0-beta.8 (2019-12-03)

- Changed preview to beta.
- Fixes authorization when using client credentials.
- Changed FullyQualifiedDomainName -> FullyQualifiedNamespace.
- Renamed `BatchOptions` -> `CreateBatchOptions` and added `getRetryMode`.
- Renamed `ProxyConfiguration` -> `ProxyOption`s.
- Removed cloneable from retry policies.
- Renamed `RetryOptions`, `RetryPolicy` -> `AmqpRetryOptions`, `AmqpRetryPolicy`.
- Updated `RetryMode` -> `AmqpRetryMode`.
- Updated CBS -> ClaimsBasedSecurityNode.
- Removed final from RetryPolicy.
- Updated Hostname -> FullyQualifiedNamespace.
- `AmqpConnection` implements AutoCloseable. Added `getEndpointStates` API.
- `AmqpConnection`/`Link`/`Session` implements AutoCloseable. Added `getEndpointStates` API.
- `CBSNode` implements AutoCloseable.
- Deleted EndpointStateNotifier. Added ShutdownSignals to Connection.
- Deleted EndpointStateNotifierBase.
- Updated parameter name for MessageConstant.fromValue.
- Moved AmqpExceptionHandler into implementation class.
- Updated CBS -> Cbs.
- Added `AmqpEndpointStateUtil`.
- Closed ReactorReceiver on errors or closures in link.


## 1.0.0-preview.7 (2019-11-04)

## 1.0.0-preview.6 (2019-10-10)

- Added more error messages for checking null.

## 1.0.0-preview.5 (2019-10-07)

- Getters and setters were updated to use Java Bean notation.
- Added `MessageSerializer` to azure-core-amqp.
- Moved Reactor handlers into azure-core-amqp.
- Moved implementation specific classes to azure-core-amqp.
- Moved ReactorDispatcher, AmqpErrorCode to azure-core-amqp.
- Renamed `getIdentifier` to `getId`.
- Renamed `getHost` to `getHostName`.
- Cleanup and introduced OpenCensus Tracing plugin.
- Added `PROTON_IO` in ErrorCondition.
- Added `ProxyConfiguration` for API `createConnectionHandler`.

## 1.0.0-preview.4 (2019-09-09)

- Support tracing for azure-core-amqp.

## 1.0.0-preview.3 (2019-08-05)

- Retry implements Cloneable.
- Rename `Retry` to `RetryPolicy`.
- `RetryOptions` implements Cloneable.

## 1.0.0-preview.2 (2019-07-02)

## 1.0.0-preview.1 (2019-06-28)

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-amqp_1.0.0-preview.1/core/azure-core-amqp/README.md)
