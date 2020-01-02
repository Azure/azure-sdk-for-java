# Release History

## 1.0.0-beta.8 (12-03-2019)

## Version 1.0.0-preview.7 (2019-11-04)

## Version 1.0.0-preview.6 (2019-10-10)

## Version 1.0.0-preview.5 (2019-10-07)
- Getters and setters were updated to use Java Bean notation.
- Added `MessageSerializer` to azure-core-amqp.
- Moved Reactor handlers into azure-core-amqp.
- Moved implementation specific classes to azure-core-amqp.
- Moved ReactorDispatcher, AmqpErrorCode to azure-core-amqp.
- Renamed `getIdentifier` to `getId`.
- Renamed `getHost` to `getHostName`.
- Cleanup and introduced OpenCensus Tracing plugin.

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
