# Azure IoT Models Repository client library for Java

This library provides functionality for interacting with the [Azure IoT Models Repository][modelsrepository_iot_endpoint]. It also aims to provide a consistent experience working with digital twin model repositories following Azure IoT conventions.

[Source code][source]

## Getting started

The complete Microsoft Azure SDK can be downloaded from the [Microsoft Azure downloads][microsoft_sdk_download] page, and it ships with support for building deployment packages, integrating with tooling, rich command line tooling, and more.

For the best development experience, developers should use the official Microsoft Maven packages for libraries. Maven packages will be regularly updated with new functionality and hotfixes.

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-iot-modelsrepository;current})

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-iot-modelsrepository</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Prerequisites

- A models repository following [Azure IoT conventions][modelsrepository_conventions]
  - The models repository can be located on the local filesystem or hosted on a webserver.
  - Azure IoT hosts the global [Azure IoT Models Repository][modelsrepository_iot_endpoint] which the client will point to by default if no URI is provided.

### Authenticate the Client

Currently no authentication mechanisms are supported in the client. The global endpoint is not tied to an Azure subscription and does not support auth. All models published are meant for anonymous public consumption.

## Key concepts

The Azure IoT Models Repository enables builders to manage and share digital twin models. The models are [JSON-LD][json_ld_reference] documents defined using the Digital Twins Definition Language ([DTDL][dtdlv2_reference]).

The repository defines a pattern to store DTDL interfaces in a directory structure based on the Digital Twin Model Identifier (DTMI). You can locate an interface in the repository by converting the DTMI to a relative path. For example, the DTMI "`dtmi:com:example:Thermostat;1`" translates to `/dtmi/com/example/thermostat-1.json`.

## Examples

You can familiarize yourself with the client using [samples for IoT Models Repository][modelsrepository_samples].

## Troubleshooting

All events and errors that surface from the service calls will be logged with the ClientLogger.

## Next steps

See implementation examples with our [code samples][modelsrepository_samples].

## Contributing

This project welcomes contributions and suggestions.
Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution.
For details, visit <https://cla.microsoft.com.>.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment).
Simply follow the instructions provided by the bot.
You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct].
For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[microsoft_sdk_download]: https://azure.microsoft.com/downloads/?sdk=net
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[modelsrepository_iot_endpoint]: https://devicemodels.azure.com/
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/modelsrepository/azure-iot-modelsrepository/src
[modelsrepository_conventions]: https://github.com/Azure/iot-plugandplay-models-tools/wiki
[json_ld_reference]: https://json-ld.org
[dtdlv2_reference]: https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md
[modelsrepository_samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/modelsrepository/azure-iot-modelsrepository/src/samples
