# Release History

## 1.5.0-beta.1 (Unreleased)

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

## 1.0.0-beta.8 (12-03-2019)

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

