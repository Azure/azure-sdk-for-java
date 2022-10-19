---
page_type: sample
languages:
  - java
products:
  - azure
urlFragment: servicelinker-resourcemanager-samples
---

# Azure Service Linker Create Connection Samples for Java

This contains Java samples for create service connection using Service Linker Provider.

## Getting started

Getting started explained in detail [here][SERVICELINKER_README_GETTING_STARTED].
Please refer it to add dependency and configure authentication environment variables.

## Samples Details

All samples are located in [CreateServiceLinker][SERVICELINKER_SAMPLE_CODE] file, you can use `main()` entrypoint to run it directly.

### CreateSpringCloudAndSQLConnection

1. Create Spring Cloud App and Deployment
2. Create SQL Database
3. Setup connection between Spring Cloud App and SQL Database using username and password by creating Service Linker

### CreateWebAppAndKeyVaultConnectionWithUserIdentity

1. Create Web App
2. Create Key Vault
3. Create User Assigned Identity
4. Setup connection between Web App and Key Vault using User Assigned Identity by creating Service Linker

### Special Case

The Service Linker provider need user token in a separated header in the following scenarios.

* The target resource is Key Vault
* SecretStore is used to store secret in Key Vault
* VNetSolutionInfo is specified

## Next steps

Start using Service Linker Java SDK in your solutions.

For more information about Service Linker, refer [here][SERVICELINKER_DOCS].

For more information about other Azure Management SDK, refer [here][MGMT_SDK_LINK].

<!-- LINKS -->
[SERVICELINKER_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicelinker/azure-resourcemanager-servicelinker#getting-started
[SERVICELINKER_SAMPLE_CODE]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicelinker/azure-resourcemanager-servicelinker/src/samples/java/com/azure/resourcemanager/servicelinker/CreateServiceLinker.java
[SERVICELINKER_DOCS]: https://docs.microsoft.com/azure/service-connector
[MGMT_SDK_LINK]: https://aka.ms/azsdk/java/mgmt
