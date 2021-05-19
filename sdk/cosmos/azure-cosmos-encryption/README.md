# Encryption plugin library for Azure Cosmos DB Java SDK for Java
The Azure Cosmos Encryption Plugin is used for encrypting data with user provided key before saving  into CosmosDB and decrypting it when reading back from the database.

## Getting started
### Include the package

[//]: # ({x-version-update-start;com.azure:azure-cosmos-encryption;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos-encryption</artifactId>
  <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})


### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator](https://azure.microsoft.com/documentation/articles/documentdb-nosql-local-emulator) for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store as [explained here](https://docs.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) [Maven][maven]

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

The SDK provides Reactor Core based async APIs. You can read more about Reactor Core and [Flux/Mono types here](https://projectreactor.io/docs/core/release/api/)

## Key concepts

The Azure Cosmos Encryption Plugin is used for encrypting data with user provided key before saving  into CosmosDB and decrypting it when reading back from the database. Beneath it uses Azure Cosmos DB Java SDK which provides client-side logical representation to access the Azure Cosmos DB SQL API.
A Cosmos DB account contains zero or more databases, a database (DB) contains zero or more containers, and a container contains zero or more items.
You may read more about databases, containers and items [here](https://docs.microsoft.com/azure/cosmos-db/databases-containers-items).
A few important properties defined at the level of the container, among them are provisioned throughput and partition key.

## Examples
The following section provides several code snippets covering some of the most common CosmosDB SQL API tasks, including:
* [Create Cosmos Encryption Client](#create-cosmos-encryption-client "Create Cosmos Encryption Client")
* [Create Encryption Database](#create-encryption-database "Create Encryption Database")
* [Create Encryption Container](#create-encryption-container "Create Encryption Container")
* [CRUD operation on Items](#crud-operation-on-items "CRUD operation on Items")

### Create Cosmos Encryption Client
```java
// Create a new CosmosEncryptionAsyncClient
CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
    .endpoint("<YOUR ENDPOINT HERE>")
    .key("<YOUR KEY HERE>")
    .buildAsyncClient();
CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient =
            CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(cosmosAsyncClient, new AzureKeyVaultKeyStoreProvider(tokenCredentials));
```

### Gets Cosmos Encryption Database
Using cosmos encryption client created in previous example, you can create cosmos encryption database proxy object like this:

```java

// Get a reference to the encryption database
// This will create a cosmos encryption database proxy object.
CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase = cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(<EXISTING_DATABASE_NAME>)

```

### Create Cosmos Encryption Container
We need to first create Container with ClientEncryptionPolicy and using cosmos encryption database object created in previous example, you can create cosmos encryption container proxy object like this:

```java

//Create Client Encryption Key
EncryptionKeyWrapMetadata metadata = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key", "tempmetadata");
cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata1).subscribe();
//Create Encryption Container            
ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
includedPath.setClientEncryptionKeyId("key");
includedPath.setPath("/sensitiveString");
includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
paths.add(includedPath);
ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(paths);
CosmosContainerProperties properties = new CosmosContainerProperties(<CONTAINER_NAME>, "/mypk");
cosmosAsyncDatabase.createContainer(properties).subscribe();

// Create a reference to the encryption container
// This will create a cosmos encryption container proxy object.
cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(<CONTAINER_NAME>)
```

Also, we have more examples [https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples][samples].


## Troubleshooting
TODO

## Next steps
TODO

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source_code]: src
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[api_documentation]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-cosmos/latest/index.html
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/java-supported-jdk-runtime?view=azure-java-stable
[maven]: https://maven.apache.org/
[cosmos_maven]: https://search.maven.org/artifact/com.azure/azure-cosmos
[cosmos_maven_svg]: https://img.shields.io/maven-central/v/com.azure/azure-cosmos.svg
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[samples]: https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples
[samples_readme]: https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples/blob/master/README.md
[troubleshooting]: https://docs.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://docs.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[getting_started]: https://github.com/Azure-Samples/azure-cosmos-java-getting-started
[quickstart]: https://docs.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync
[project_reactor_schedulers]: https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2Fazure-cosmos-encryption%2FREADME.png)
