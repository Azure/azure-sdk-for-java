# Azure Json Reflect shared library for Java

Azure Json Reflect provides implementations of `azure-json` using reflection.
It uses Jackson and Gson but has no dependencies on these libraries.
To use Azure Json Reflect a compatible version of Jackson or Gson must be present on the classpath.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Supported versions

#### Jackson
- Versions 2.10.0 and newer

#### Gson
- Versions 2.4 and newer

## Key concepts

See `azure-json` for more key concepts.

This package searches the relative classpath for Jackson or Gson.
It will then search for all the methods that are required for the `JsonReader` and `JsonWriter` implementations.
These implementations provide abstractions of the JSON libraries.

### Entry point
Use `JsonProviderFactory.getInstance()`, `JsonProviderFactory.getJacksonJsonProvider()`, or 
`JsonProviderFactory.getGsonJsonProvider()` to get a `JsonProvider`.

## Examples

See `azure-json` for examples.

## Next steps

Get started with Azure libraries that are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contributing

For details on contributing to this repository, see the <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md"> contributing guide</a>.
1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some features`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new pull Request

<!-- links -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
