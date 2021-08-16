# Azure Core Management client library for Java

Azure Core Management library is a collection of classes common to the [Azure Resource Manager (ARM)][arm] client libraries.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-core-management;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-management</artifactId>
    <version>1.4.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

- `AzureEnvironment` for Azure cloud configure, and `AzureProfile` for additional tenant ID and subscription ID configure.
- `ManagementException` and `ManagementError` for ARM error response.
- `PollerFactory` and `PollResult` for ARM long-running operation.

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
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- Links -->
[arm]: https://docs.microsoft.com/azure/azure-resource-manager/management/
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
