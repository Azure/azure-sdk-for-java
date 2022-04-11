# Azure Core Jackson SerializerAdapter plugin library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure Core Jackson SerializerAdapter is a plugin for the `azure-core` `SerializerAdapter` API. It is meant to replicate
the functionality of `JacksonAdapter` that used to exist in `azure-core` and provide library authors and consumers a
way to transition to stream-style at their own pace.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-core;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-serializeradapter-jackson</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

## Examples

## Next steps

Get started with Azure libraries that are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

### Enabling Logging

Azure SDKs for Java provide a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- links -->
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-serializeradapter-jackson%2FREADME.png)
