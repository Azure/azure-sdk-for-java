# Release History

## 1.3.0-beta.2 (Unreleased)


## 1.3.0-beta.1 (2020-02-11)
- Added default logging implementation for SLF4J.
- Modified checks to determine if logging is allowed.
- Improved logging performance.
- Enhanced and extended PagedFlux implementation to cover additional use cases.
- Enabled loading proxy configuration from the environment.
- Added support for Digest proxy authentication.
- Updated 'BufferedResponse' to deep copy the response to handle scenarios where the underlying stream is reclaimed.

## 1.2.0 (2020-01-07)
- Ignore null headers and allow full url paths 
- Add missing HTTP request methods to HttpMethod enum
- Support custom header with AddHeaderPolicy
- Support custom header name in RequestIDPolicy
- Prevent HttpLoggingPolicy Consuming Body
- Hide secret info from log info 
- Ensure HTTPS is used when authenticating with tokens 
- Reduce Prefetch Limit for PagedIterable and IterableStream
- Add Iterable<T> overload for IterableStream<T>

## 1.1.0 (2019-11-26)
- Added support for creating reactor-netty-http client from an existing client.
- Added UserAgent helper methods for fetching client name and version from pom file.
- Added toReactorContext to FluxUtil.
- Logging exception at warning level, and append stack trace if log level is verbose.
- Fixed HttpLoggingPolicy to take null HttpLogOptions.
- Changed the User agent format.
- Hide the secrets from evnironment variable.
- UserAgentPolicy is using the value stored in the policy no matter what is stored in the passed request. Also, removed the service version from User agent format.
- Added Iterable<T> overload for IterableStream<T>.
- Reduce Prefetch Limit for PagedIterable and IterableStream.
- Ensure HTTPS is used when authenticating with tokens.

## 1.0.0 (2019-10-29)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core/src/samples/java/com/azure/core)

- Initial release. Please see the README and wiki for information on the new design.
