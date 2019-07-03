# Azure SDK for Java

[![Build Status](https://dev.azure.com/azure-sdk/public/_apis/build/status/17?branchName=master)](https://dev.azure.com/azure-sdk/public/_build/latest?definitionId=17) [![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html)

This repository contains official Java libraries for Azure services. For reference documentation go to [Azure SDK for Java documentation](http://aka.ms/java-docs), and tutorials, samples, quick starts and other documentation, go to [Azure for Java Developers](https://docs.microsoft.com/java/azure/).

You can find a complete list of all the packages for these libraries [here](packages.md).

## Getting started

For your convenience, each service has a separate set of libraries that you can choose to use instead of one, large Azure package. To get started with a specific library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk` directory.

### Prerequisites
Java 8 or later is required to use the July 2019 client preview libraries, otherwise Java 7 or later is required.

## Packages available
Each service might have a number of libraries available from each of the following categories:

* [Client - July 2019 Preview](#Client-July-2019-Preview)
* [Client - Stable](#Client-Stable)
* [Management](#Management)

### Client: July 2019 Preview
New wave of packages that we are currently releasing in **preview**. These libraries allow you to use and consume existing resources and interact with them, for example: upload a blob. These libraries share a number of core functionalities such as http retries, logging, transport protocols, authentication protocols, etc. that can be found in the [azure-core](core) library. You can learn more about these libraries by reading the guidelines that they follow [here](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html). Library directories start with `azure-`, e.g. `azure-keyvault`.

The libraries released in the July 2019 preview:
- [App Configuration](appconfiguration/client/README.md)
- [Event Hubs](eventhubs/client/README.md)
- [Key Vault Keys](keyvault/client/keys/README.md)
- [Key Vault Secrets](keyvault/client/secrets/README.md)
- [Storage Blobs](storage/client/README.md)

>NOTE: If you need to ensure your code is ready for production, use one of the stable libraries.


### Client: Stable
Last stable versions of packages that have been provided for usage with Azure and are production-ready. These libraries provide you with similar functionalities to the Preview ones as they allow you to use and consume existing resources and interact with them, for example: upload a blob. Library directories start with `microsoft-azure-`, e.g. `microsoft-azure-keyvault`.

### Management
Libraries which enable you to provision specific resources. They are responsible for directly mirroring and consuming Azure service's REST endpoints. Library directories contain `-mgmt-`, e.g. `azure-mgmt-keyvault`.

## Need help?
* For reference documentation visit the [Azure SDK for Java documentation](http://aka.ms/java-docs).
* For tutorials, samples, quick starts and other documentation, visit [Azure for Java Developers](https://docs.microsoft.com/java/azure/).
* For build reports on code quality, test coverage, etc, visit [Azure Java SDK](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html).
* File an issue via [Github Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose).
* Check [previous questions](https://stackoverflow.com/questions/tagged/azure-java-sdk) or ask new ones on StackOverflow using `azure-java-sdk` tag.

## Contributing
For details on contributing to this repository, see the [contributing guide](CONTRIBUTING.md).