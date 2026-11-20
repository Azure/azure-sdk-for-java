# Client Core Netty HTTP plugin library for Java

Client Core Netty HTTP client is a plugin for the `io.clientcore.core` HTTP client API.

## Getting started

### Prerequisites

### Include the package

[//]: # ({x-version-update-start;io.clientcore:http-netty4;current})
```xml
<dependency>
    <groupId>io.clientcore</groupId>
    <artifactId>http-netty4</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

### Native transports on the module path

On JDK 11 or later, Netty 4.2's epoll/kqueue transport classes and platform-specific JNI libraries
are **separate explicit Java modules**. Putting their JARs on the module path does not resolve them
automatically. When a named-module application wants `http-netty4` to select a native transport,
add a matching Netty native classifier as a **runtime** dependency; this package only declares the
native classifiers as test dependencies. For example, on Linux x86_64:

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-transport-native-epoll</artifactId>
    <version>${netty.version}</version>
    <classifier>linux-x86_64</classifier>
</dependency>
```

Define `${netty.version}` in your POM or replace it with the Netty version aligned with this client.
The native classifier dependency also brings in
the corresponding `netty-transport-classes-*` JAR. Resolve **both** the class and native-resource
modules when launching the application:

| Platform and classifier | Launch option |
| --- | --- |
| Linux x86_64 (`netty-transport-native-epoll:linux-x86_64`) | `--add-modules=io.netty.transport.classes.epoll,io.netty.transport.epoll.linux.x86_64` |
| macOS x86_64 (`netty-transport-native-kqueue:osx-x86_64`) | `--add-modules=io.netty.transport.classes.kqueue,io.netty.transport.kqueue.osx.x86_64` |

For other architectures, supply the matching native classifier and use its module name as reported by
`jar --describe-module --file <native-JAR> --release 11`; see
[Netty's native transport guide](https://netty.io/wiki/native-transports.html#using-the-native-transports)
for classifier details. Resolving only the class module does not
expose the bundled JNI library; adding `requires static` to a library descriptor does not make
either module a runtime root. Without usable native transport the client falls back to JDK NIO.
Neither Java 8 nor classpath applications need these JPMS launch options. Module resolution by
itself does not establish that native I/O works on the current OS.

## Examples

### Create a Simple Client

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

### Enabling Logging

Client Core libraries for Java provide a consistent logging story to help aid in troubleshooting application errors and
expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state
to help locate the root issue.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request
