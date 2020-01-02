# Release History

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
- Renamed from getProperties() -> getEventHubProperties().
- Exposed getMaxSizeInBytes API.
- Removed fromOffset(bool) overload public method.
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

## Version 1.0.0-preview.7 (2019-11-04)

## Version 1.0.0-preview.6 (2019-10-10)
- Added more error messages for checking null.

## Version 1.0.0-preview.5 (2019-10-07)
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

## Version 1.0.0-preview.4 (2019-09-09)
- Support tracing for azure-core-amqp.

## Version 1.0.0-preview.3 (2019-08-05)
- Make ClientLogger thread-safe.
- Retry implements Cloneable.
- Rename `Retry` to `RetryPolicy`.
- `RetryOptions` implements Cloneable.

## Version 1.0.0-preview.2 (2019-07-02)

## Version 1.0.0-preview.1 (2019-06-28)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-amqp_1.0.0-preview.1/core/azure-core-amqp/README.md)

- Initial release. Please see the README and wiki for information on the new design.
