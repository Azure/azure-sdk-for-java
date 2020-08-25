# Azure IoT Digital Twins client library for Java

This library provides access to the Azure Digital Twins service for managing twins, models, relationships, etc.

  [Source code][source] | Package (maven) (TODO: Add package information)

## Getting started

The complete Microsoft Azure SDK can be downloaded from the [Microsoft Azure downloads][microsoft_sdk_download] page, and it ships with support for building deployment packages, integrating with tooling, rich command line tooling, and more.

For the best development experience, developers should use the official Microsoft Maven packages for libraries. Maven packages are regularly updated with new functionality and hotfixes.

### Install the package

Install the Azure Digital Twins client library for java
TODO: Fill in details after first publish

### Prerequisites

- A Microsoft Azure Subscription
  - To call Microsoft Azure services, create an [Azure subscription][azure_sub].
- An Azure Digital Twins instance
  - In order to use the Azure Digital Twins SDK, first create a Digital Twins instance using one of options:
    - Using [Azure portal][azure_portal]
    - Using [Azure Management APIs][azure_rest_api]
    - Using [Azure CLI][azure_cli]
      - You will need to install azure cli and the [Azure IoT extension][iot_cli_extension] for Azure CLI.
      - Refer to [IoT CLI documentation][iot_cli_doc] for more information on how to create and interact with your Digital Twins instance.

### Authenticate the Client

In order to interact with the Azure Digital Twins service, you will need to create an instance of a [TokenCredential class][token_credential] and pass it to the constructor of your DigitalTwinsClientBuilder (TODO: Reference the file once checked in).

## Key concepts

Azure Digital Twins Preview is an Azure IoT service that creates comprehensive models of the physical environment.
It can create spatial intelligence graphs to model the relationships and interactions between people, spaces, and devices.

You can learn more about Azure Digital Twins by visiting [Azure Digital Twins Documentation][digital_twins_documentation]

## Examples

You can familiarize yourself with different APIs using [samples for Digital Twins](TODO: Point to sampels once available).

## Source code folder structure

### /src

TODO: Describe source and link to the path.

### /src/swagger

A local copy of the swagger file that defines the structure of the REST APIs supported by the Azure Digital Twins service.

To regenerate the code, run the powershell script [generate.ps1](./generate.ps1).

Any time the client library code is updated, the following scripts need to be run:

TODO: Add extra information here.

## Troubleshooting

TODO: Add troubleshoooting guide

## Next steps

## Contributing

This project welcomes contributions and suggestions.
Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution.
For details, visit <https://cla.microsoft.com.>

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment).
Simply follow the instructions provided by the bot.
You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct].
For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[microsoft_sdk_download]: https://azure.microsoft.com/en-us/downloads/?sdk=java
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_sub]: https://azure.microsoft.com/free/
[source]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[nuget]: https://www.nuget.org/
[azure_portal]: https://portal.azure.com/
[azure_rest_api]: https://docs.microsoft.com/en-us/rest/api/azure/
[azure_core_library]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core
[token_credential]: https://docs.microsoft.com/en-us/java/api/com.azure.core.credential.tokencredential?view=azure-java-stable
[digital_twins_documentation]: https://docs.microsoft.com/en-us/azure/digital-twins/
[azure_cli]: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest
[iot_cli_extension]: https://docs.microsoft.com/en-us/azure/iot-pnp/howto-use-iot-pnp-cli
[iot_cli_doc]: https://docs.microsoft.com/en-us/cli/azure/ext/azure-iot/dt?view=azure-cli-latest
