# Release History

## 2.6.0-beta.1 (Unreleased)

### Features Added

- Added `AmqpClientOptions` to enable set identifier for AMQP client and link.

### Breaking Changes

### Bugs Fixed

### Other Changes

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
- Fixed a bug that caused amqp connection not to retry when network error happened.

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
- Move to use Schedulers.single() because pushing to Qpid's reactor is not thread-safe.

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
