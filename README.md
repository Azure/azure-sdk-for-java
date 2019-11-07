# Azure SDK for Java

[![Build Status](https://dev.azure.com/azure-sdk/public/_apis/build/status/17?branchName=master)](https://dev.azure.com/azure-sdk/public/_build/latest?definitionId=17) [![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html) [![Dependencies](https://img.shields.io/badge/dependencies-analyzed-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/dependencies.html) [![SpotBugs](https://img.shields.io/badge/SpotBugs-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/spotbugsXml.html) [![CheckStyle](https://img.shields.io/badge/CheckStyle-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/checkstyle-aggregate.html)


This repository contains official Java libraries for Azure services. For reference documentation go to [Azure SDK for Java documentation](http://aka.ms/java-docs), and tutorials, samples, quick starts and other documentation, go to [Azure for Java Developers](https://docs.microsoft.com/java/azure/).

You can find a complete list of all the packages for these libraries [here](packages.md).

## Getting started

To get started with a specific library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk` directory.

For tutorials, samples, quick starts and other documentation, visit [Azure for Java Developers](https://docs.microsoft.com/java/azure/).

### Prerequisites
Java 8 or later is required to use the November 2019 client libraries, otherwise Java 7 or later is required.

## Packages available
Each service might have a number of libraries available from each of the following categories:

- [Client - November 2019 Releases](#Client-November-2019-Releases)
- [Client - Previous Versions](#Client-Previous-Versions)
- [Management](#Management)

### Client: November 2019 Releases
New wave of packages that we are announcing as **GA** and several that are currently releasing in **preview**. These libraries follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java/guidelines/) and share a number of core features such as HTTP retries, logging, transport protocols, authentication protocols, etc., so that once you learn how to use these features in one client library, you will know how to use them in other client libraries. You can learn about these shared features [here](core). 

These libraries can be easily identified by their folder, package, and namespaces names starting with `azure-`, e.g. `azure-keyvault`.

The libraries released in the November 2019 GA release:
- [Identity](sdk/identity/azure-identity/README.md)
- [Key Vault Keys](sdk/keyvault/azure-security-keyvault-keys/README.md)
- [Key Vault Secrets](sdk/keyvault/azure-security-keyvault-secrets/README.md)
- [Storage Blobs](sdk/storage/azure-storage-blob/README.md)
- [Storage Blobs Batch](sdk/storage/azure-storage-blob-batch/README.md)
- [Storage Blobs Cryptography](sdk/storage/azure-storage-blob-cryptography/README.md)
- [Storage Queues](sdk/storage/azure-storage-queue/README.md)

The libraries released in the November 2019 preview:
- [App Configuration](sdk/appconfiguration/azure-data-appconfiguration/README.md)
- [Event Hubs](sdk/eventhubs/azure-messaging-eventhubs/README.md)
- [Event Hubs Checkpoint Store](sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md)
- [Storage File Shares](sdk/storage/azure-storage-file-share/README.md)
- [Key Vault Certificates](sdk/keyvault/azure-security-keyvault-certificates/README.md)
- [OpenCensus Tracing](sdk/core/azure-core-tracing-opencensus/README.md)

> NOTE: If you need to ensure your code is ready for production use one of the stable, non-preview libraries.

### Client: Previous Versions
Last stable versions of packages that have been provided for usage with Azure and are production-ready. These libraries provide similar functionalities to the preview libraries, as they allow you to use and consume existing resources and interact with them, for example: upload a blob. Stable library directories start with `microsoft-azure-`, e.g. `microsoft-azure-keyvault`. They might not implement the [guidelines](https://azure.github.io/azure-sdk/java_introduction.html) or have the same feature set as the Novemeber releases. They do however offer wider coverage of services. 

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

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit
https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2FREADME.png)
