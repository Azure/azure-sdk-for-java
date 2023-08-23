# Azure JSON shared library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure JSON provides shared primitives, abstractions, and helpers for JSON.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-json;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-json</artifactId>
  <version>1.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

### JsonSerializable

`JsonSerializable` is used to define how an object is JSON serialized and deserialized using stream-style serialization
where the object itself manages the logic for how it's handled. The interface provides an instance-based `toJson` API 
that handles writing the object to a `JsonWriter` and a static `fromJson` API which implementations must provide to 
define how to create an object by reading from a `JsonReader`, if an implementation isn't provided 
`UnsupportedOperationException` will be thrown.

### JsonToken

`JsonToken` is a basic enum that indicates the current state in a JSON stream.

### JsonReader

`JsonReader` provides both basic, reading primitive and boxed primitive types, and convenience, reading arrays, maps,
and objects, APIs for reading JSON. `JsonReader` is provided to allow for any underlying JSON parser to implement it,
such as Jackson or GSON, as long as the implementation passes the tests provided by this package's test-jar 
(`JsonReaderContractTests`).

`JsonReader` doesn't progress forward in the JSON stream until `nextToken` is called, meaning that `JsonReader.getInt`
could be called indefinitely returning the same integer without error until `nextToken` progresses the JSON stream 
forward.

`JsonReader` allows for type conversion between JSON types, such as trying to convert a JSON string to a number or vice
versa, and for commonly used nonstandard JSON values, such as `NaN`, `INF`, `-INF`, `Infinity`, and `-Infinity`.

`JsonReader` doesn't take ownership of the JSON input source and therefore doesn't close any resources if the JSON is 
provided using an `InputStream` or `Reader`.

#### Nesting Limits

`JsonReader`'s generic `readUntyped` API tracks how deeply nested the object being read is. If the nesting passes the 
threshold of `1000`, `IllegalStateException` is thrown to prevent `StackOverflowError`.

### JsonWriter

`JsonWriter` provides both basic, writing primitives and boxed primitive types, and convenience, writing arrays, maps,
and objects, APIs for writing JSON. `JsonWriter` is provided to allow for any underlying JSON writer to implement it,
such as Jackson or GSON, as long as the implementation passes the tests provided by the package's test-jar
(`JsonWriterContractTests`).

`JsonWriter` allows for commonly used nonstandard JSON values, such as `NaN`, `INF`, `-INF`, `Infinity`, and 
`-Infinity`, to be written using `writeNumberField` or `writeRawValue`.

`JsonWriter` doesn't write null `byte[]`, `Boolean`, `Number`, or `String` values when written as a field, 
`writeBinaryField`, `writeBooleanField`, `writeNumberField`, or `writeStringField`, if a null field needs to be written
use `writeNullField`.

`JsonWriter` must be periodically flushed to ensure content written to it's flushed to the underlying container type,
generally an `OutputStream` or `Writer`. Failing to flush may result in content being lost. Closing the `JsonWriter`
flushes content, so it's best practice to use `JsonWriter` in a try-with-resources block where the `JsonWriter` is 
closed once it's finished being used.

`JsonWriter` doesn't take ownership of the JSON output source and therefore doesn't close any resources if the JSON is
being written to an `OutputSteam` or `Writer`.

#### JSON State Management

To ensure that the JSON being written is valid, `JsonWriter` maintains the state of the JSON using `JsonWriteContext`
and on each attempt to write it validates whether the operation is valid. The implementation of `JsonWriter` must
ensure state is tracked correctly, for example when nothing has been written the JSON state must be `ROOT` and `ROOT`
doesn't allow for JSON field names to be written.

### JsonProvider

`JsonProvider` is a service provider interface that allows for `JsonReader`s and `JsonWriter`s to be created using
implementations found on the classpath. `JsonProvider` can also create the default implementations that are provided
by this package if an implementation isn't found on the classpath.

#### JsonOptions

`JsonOptions` contains configurations that must be respected by all implementations of `JsonReader`s and `JsonWriter`s. 
At this time, there's only one configuration for determining whether non-numeric numbers, `NaN`, `INF`, `-INF`, `Infinity`, 
and `-Infinity` are supported in JSON reading and writing with a default setting of `true`, that non-numeric numbers 
are allowed.

### Providing an SPI implementation

`JsonReader` and `JsonWriter` are service provider interfaces used by `JsonProvider` and `JsonProviders` to enable
implementations to be loaded from the class path. The Azure JSON package provides a default implementation that is
used if one can't be found on the class path. To provide a custom implementation, implement `JsonReader`, `JsonWriter`,
and `JsonProvider` in your own package and indicate that the package provides an instance of `JsonProvider`. To ensure
that your implementations are correct, include the `test` scoped dependency of Azure JSON and extend the
`JsonReaderContractTests`, `JsonWriterContractTests`, and `JsonProviderContractTests`. These tests outline all basic
contract requirements set forth by `JsonReader`, `JsonWriter`, and `JsonProvider` and testing a few complex scenarios 
to provide validation of any implementation.

## Examples

Check out the [samples README][samples_readme] for in-depth examples on how to use Azure JSON.

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Next steps

Get started with Azure libraries that are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- links -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/8517f855a79ea23dce94397c58e4368738016fc4/sdk/core/azure-json/src/samples/README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-json%2FREADME.png)
