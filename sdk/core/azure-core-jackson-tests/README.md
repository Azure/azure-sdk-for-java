# Azure Core Jackson Tests plugin library for Java

Azure Core Jackson Tests is a test package that tests `azure-core` library against multiple versions of Jackson libraries that users might have.

## Getting started

This package is intended to run in Live Test Azure Pipeline (`java - core - test`) under `jackson_supported_versions` and
`jackson_unsupported_versions` test name), but you can run it locally by setting `AZURE_CORE_TEST_SUPPORTED_JACKSON_VERSION` 
or `AZURE_CORE_TEST_UNSUPPORTED_JACKSON_VERSION` environment variables.

## Key concepts

## Examples

Here's how you can test arbitrary Jackson version from `azure-core-jackson-tests` folder.

Windows:

```powershell
PS> $env:AZURE_CORE_TEST_SUPPORTED_JACKSON_VERSION="2.12.1"
PS> mvn test
```

Linux:

```bash
$ export AZURE_CORE_TEST_SUPPORTED_JACKSON_VERSION="2.12.2"
$ mvn test
```

## Next steps

Get started with Azure libraries that are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- Links -->
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
