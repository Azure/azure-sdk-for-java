# Encryption plugin library for Azure Cosmos DB Java SDK for Java
The Azure Cosmos Encryption Plugin is used for encrypting data with user provided key before saving into CosmosDB and decrypting it when reading back from the database.

[Source code][encryption_source_code] | [Package (Maven)][cosmos_encryption_maven] | [API reference documentation][encryption_api_documentation] | [Product documentation][cosmos_docs] |
[Samples][getting_started_encryption]

## Getting started
### Include the package

[//]: # ({x-version-update-start;com.azure:azure-cosmos-encryption;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos-encryption</artifactId>
  <version>1.0.0-beta.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

Refer to maven central for previous [releases][cosmos_encryption_maven]

Refer to [javadocs][encryption_api_documentation] for more details on the package


### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator](https://azure.microsoft.com/documentation/articles/documentdb-nosql-local-emulator) for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store as [explained here](https://docs.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) [Maven][maven]

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

The SDK provides Reactor Core based async APIs. You can read more about Reactor Core and [Flux/Mono types here](https://projectreactor.io/docs/core/release/api/)

## Key concepts

The Azure Cosmos Encryption Plugin is used for encrypting data with user provided key before saving into CosmosDB and decrypting it when reading back from the database. Underneath it uses Azure Cosmos DB Java SDK which provides client-side logical representation to access the Azure Cosmos DB SQL API.
A Cosmos DB account contains zero or more databases, a database (DB) contains zero or more containers, and a container contains zero or more items.
You may read more about databases, containers and items [here](https://docs.microsoft.com/azure/cosmos-db/databases-containers-items).
A few important properties defined at the level of the container, among them are provisioned throughput and partition key.

## Examples
The following section provides several code snippets covering some of the most common Cosmos Encryption API tasks, including:
* [Create Cosmos Encryption Client](#create-cosmos-encryption-client "Create Cosmos Encryption Client")
* [Create Cosmos Encryption Database](#create-cosmos-encryption-database "Create Encryption Database")
* [Create Encryption Container](#create-cosmos-encryption-container "Create Encryption Container")
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

### Create Cosmos Encryption Database
You need to first create Database and using cosmos encryption client created in previous example, you can create cosmos encryption database proxy object like this:

```java
// This will create a database with the regular cosmosAsyncClient.
cosmosEncryptionAsyncClient.getCosmosAsyncClient().createDatabaseIfNotExists(DATABASE_NAME)
    .subscribe();

// Get a reference to the encryption database
// This will create a cosmos encryption database proxy object.
CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase = cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(<EXISTING_DATABASE_NAME>).subscribe()

```

### Create Cosmos Encryption Container
You need to first create Container with ClientEncryptionPolicy and using cosmos encryption database object created in previous example, you can create cosmos encryption container proxy object like this:

```java

//Create Client Encryption Key
EncryptionKeyWrapMetadata metadata = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key", "tempmetadata");
cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata).subscribe();
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
properties.setClientEncryptionPolicy(clientEncryptionPolicy);
cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).subscribe();

// Create a reference to the encryption container
// This will create a cosmos encryption container proxy object.
CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(<CONTAINER_NAME>);
```
### CRUD operation on Items

```java
// Create an item
Pojo pojo = new Pojo();
pojo.setSensitiveString("Sensitive Information need to be encrypted");
cosmosEncryptionAsyncContainer.createItem(pojo)
.flatMap(response -> {
        System.out.println("Created item: " + response.getItem());
        // Read that item 👓
        return cosmosEncryptionAsyncContainer.readItem(response.getItem().getId(),
                                  new PartitionKey(response.getItem().getId()),
                                  Pojo.class);
    })
    .flatMap(response -> {
        System.out.println("Read item: " + response.getItem());
        // Replace that item 🔁
        Pojo p = response.getItem();
        pojo.setSensitiveString("New Sensitive Information");
        return container.replaceItem(p,
                                     response.getItem().getId(),
                                     new PartitionKey(response.getItem().getId()));
    })
    // delete that item 💣
    .flatMap(response -> container.deleteItem(response.getItem().getId(),
                                              new PartitionKey(response.getItem().getId())))
    .subscribe();
```

We have a get started sample app available [here][getting_started_encryption].


## Troubleshooting

### General

Azure Cosmos DB is a fast and flexible distributed database that scales seamlessly with guaranteed latency and throughput.
You do not have to make major architecture changes or write complex code to scale your database with Azure Cosmos DB.
Scaling up and down is as easy as making a single API call or SDK method call.
However, because Azure Cosmos DB is accessed via network calls there are client-side optimizations you can make to achieve peak performance when using Azure Cosmos DB Java SDK v4.

- [Performance][perf_guide] guide covers these client-side optimizations.

- [Troubleshooting guide][troubleshooting] covers common issues, workarounds, diagnostic steps, and tools when you use Azure Cosmos DB Java SDK v4 with Azure Cosmos DB SQL API accounts.

### Enable Client Logging
Azure Cosmos DB Java SDK v4 uses SLF4j as the logging facade that supports logging into popular logging frameworks such as log4j and logback.

For example, if you want to use log4j as the logging framework, add the following libs in your Java classpath.

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
  <version>${slf4j.version}</version>
</dependency>
<dependency>
  <groupId>log4j</groupId>
  <artifactId>log4j</artifactId>
  <version>${log4j.version}</version>
</dependency>
```

Also add a log4j config.

```properties
# this is a sample log4j configuration

# Set root logger level to INFO and its only appender to A1.
log4j.rootLogger=INFO, A1

log4j.category.com.azure.cosmos=INFO
#log4j.category.io.netty=OFF
#log4j.category.io.projectreactor=OFF
# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%d %5X{pid} [%t] %-5p %c - %m%n
```
## Next steps

- Cosmos encryption sample program is [here][getting_started_encryption]
- Quick start of CosmosDB core java sdk [quickstart][quickstart] - Building a java app to manage CosmosDB SQL API data
- [Read more about Azure CosmosDB Service][cosmos_docs]

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
[encryption_source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-encryption/src
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[api_documentation]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-cosmos/latest/index.html
[encryption_api_documentation]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-cosmos-encryption/latest/index.html
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[maven]: https://maven.apache.org/
[cosmos_encryption_maven]: https://search.maven.org/artifact/com.azure/azure-cosmos-encryption
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[troubleshooting]: https://docs.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://docs.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[getting_started_encryption]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos-encryption/src/samples/java/com/azure/cosmos
[quickstart]: https://docs.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2Fazure-cosmos-encryption%2FREADME.png)
