# Azure SDK for Java

[![Build Status](https://dev.azure.com/azure-sdk/public/_apis/build/status/17?branchName=master)](https://dev.azure.com/azure-sdk/public/_build/latest?definitionId=17) [![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html) [![Dependencies](https://img.shields.io/badge/dependencies-analyzed-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/dependencies.html) [![SpotBugs](https://img.shields.io/badge/SpotBugs-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/spotbugsXml.html) [![CheckStyle](https://img.shields.io/badge/CheckStyle-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/checkstyle-aggregate.html)


This repository contains official Java libraries for Azure services. For reference documentation go to [Azure SDK for Java documentation](http://aka.ms/java-docs), and tutorials, samples, quick starts and other documentation, go to [Azure for Java Developers](https://docs.microsoft.com/java/azure/).

You can find a complete list of all the packages for these libraries [here](packages.md).

## Getting started

To get started with a specific library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk` directory.

For tutorials, samples, quick starts and other documentation, visit [Azure for Java Developers](https://docs.microsoft.com/java/azure/).

### Prerequisites
Java 8 or later is required to use the July 2019 client preview libraries, otherwise Java 7 or later is required.

## Packages available
Each service might have a number of libraries available from each of the following categories:

* [Client - July 2019 Preview](#Client-July-2019-Preview)
* [Client - Stable](#Client-Stable)
* [Management](#Management)

### Client: July 2019 Preview
New wave of packages that we are currently releasing in **preview**. These libraries follow the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html) and share a number of core features such as HTTP retries, logging, transport protocols, authentication protocols, etc., so that once you learn how to use these features in one client library, you will know how to use them in other client libraries. You can learn about these shared features [here](core).

These preview libraries can be easily identified by their folder, package, and namespaces names starting with `azure-`, e.g. `azure-keyvault`.

The libraries released in the July 2019 preview:
- [App Configuration](sdk/appconfiguration/azure-data-appconfiguration/README.md)
- [Event Hubs](sdk/eventhubs/azure-messaging-eventhubs/README.md)
- [Identity](sdk/identity/azure-identity/README.md)
- [Key Vault Keys](sdk/keyvault/azure-keyvault-keys/README.md)
- [Key Vault Secrets](sdk/keyvault/client/azure-keyvault-secrets/README.md)
- [Storage Blobs](sdk/storage/azure-storage-blob/README.md)

>NOTE: If you need to ensure your code is ready for production, use one of the stable libraries.

### Client: Stable
Last stable versions of packages that have been provided for usage with Azure and are production-ready. These libraries provide similar functionalities to the preview libraries, as they allow you to use and consume existing resources and interact with them, for example: upload a blob. Stable library directories start with `microsoft-azure-`, e.g. `microsoft-azure-keyvault`.

### Management
Libraries which enable you to provision specific resources. They are responsible for directly mirroring and consuming Azure service's REST endpoints. Management library directories contain `-mgmt-`, e.g. `azure-mgmt-keyvault`.

## Need help?
* For reference documentation visit the [Azure SDK for Java documentation](http://aka.ms/java-docs).
* For tutorials, samples, quick starts and other documentation, visit [Azure for Java Developers](https://docs.microsoft.com/java/azure/).
* For build reports on code quality, test coverage, etc, visit [Azure Java SDK](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html).
* File an issue via [Github Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose).
* Check [previous questions](https://stackoverflow.com/questions/tagged/azure-java-sdk) or ask new ones on StackOverflow using `azure-java-sdk` tag.

## Contributing
For details on contributing to this repository, see the [contributing guide](CONTRIBUTING.md).

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2FREADME.png)
