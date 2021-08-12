# Azure Tables client library for Java
Azure Tables is a service that stores structured NoSQL data in the cloud, providing a key/attribute store with a schemaless design. Azure Tables gives developers flexibility and scalability with all the best parts of Azure cloud.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_documentation] | [Samples][samples]

## Getting started

### Include the Package

[//]: # ({x-version-update-start;com.azure:tables:azure-data-tables;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-tables</artifactId>
  <version>12.2.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Azure storage account or Azure Cosmos DB Table API account

#### Create a Storage Account
To create a Storage Account you can use the [Azure Portal][storage_account_create_portal] or [Azure CLI][storage_account_create_cli].

```bash
az storage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
```

Your storage account URL, subsequently identified as `<your-table-account-url>`, would be formatted as follows
`http(s)://<storage-account-name>.table.core.windows.net`.

#### Create a Cosmos DB Table API account
To create a Cosmos DB Table API account you can use the [Azure Portal][cosmosdb_create_portal] or [Azure CLI][cosmosdb_create_cli].

```bash
az cosmosdb create \
    --resource-group <resource-group-name> \
    --name <cosmosdb-account-name> \
    --capabilities EnableTable
```

Your Table API account URL, subsequently identified as `<your-table-account-url>`, would be formatted as follows
`http(s)://<cosmosdb-account-name>.table.cosmosdb.azure.com`.

### Authenticate the client
Every request made to the Tables service must be authorized using a connection string, named key credential, Shared Access Signature, or token credentials. The samples below demonstrate the usage of these methods.

Note: Only Azure Storage API endpoints currently support AAD authorization via token credentials.

#### Connection string
A connection string includes the authentication information required for your application to access data in an Azure table at runtime using Shared Key authorization. See [Authenticate with a Connection String](#authenticate-with-a-connection-string) for an example of how to use a connection string with a `TableServiceClient`.

You can obtain your connection string from the Azure Portal (click **Access keys** under **Settings** in the Portal Storage account blade, or **Connection String** under **Settings** in the Portal Cosmos DB account blade) or using the Azure CLI:

```bash
# Storage account
az storage account show-connection-string \
    --resource-group <resource-group-name> \
    --name <storage-account-name>

# Cosmos DB Table API account
az cosmosdb list-connection-strings \
    --resource-group <resource-group-name> \
    --name <cosmosdb-account-name>
```

#### Shared Key credential
Shared Key authorization relies on your account access keys and other parameters to produce an encrypted signature string that is passed on the request in the Authorization header. See [Authenticate with a Shared Key credential](#authenticate-with-a-shared-key) for an example of how to use Named Key authorization with a `TableServiceClient`.

To use Named Key authorization you'll need your account name and URL, as well as an account access key. You can obtain your primary access key from the Azure Portal (click **Access keys** under **Settings** in the Portal Storage account blade, or **Connection String** under **Settings** in the Portal Cosmos DB account blade) or using the Azure CLI:

```bash
# Storage account
az storage account keys list \
    --resource-group <resource-group-name> \
    --account-name <storage-account-name>

# Cosmos DB Table API account
az cosmosdb list-keys \
    --resource-group <resource-group-name> \
    --name <cosmosdb-account-name>
```

#### Shared Access Signature (SAS)
A Shared Access Signature allows administrators to delegate granular access to an Azure table without sharing the access key directly. You can control what resources the client may access, what permissions it has on those resources, and how long the SAS is valid, among other parameters. It relies on your account access keys and other parameters to produce an encrypted signature string that is passed on the request in the query string. See [Authenticate with a Shared Access Signature (SAS)](#authenticate-with-a-shared-access-signature-sas) for an example of how to use shared access signatures with a `TableServiceClient`.

To use SAS token authorization you'll need your account name and URL, as well as the SAS. You can obtain your SAS from the Azure Portal (click **Shared access signature** under **Settings** in the Portal Storage account blade) or using the Azure CLI:

```bash
# Account-level SAS
az storage account generate-sas \
    --account-name <storage-or-cosmosdb-account-name> \
    --services t \
    --resource-types <resource-types> \
    --permissions <permissions> \
    --expiry <expiry-date>

# Table-level SAS
az storage table generate-sas \
    --name <table-name>
```

## TokenCredential
Azure Tables provides integration with Azure Active Directory (AAD) for identity-based authentication of requests to the Table service when targeting a Storage endpoint. With AAD, you can use role-based access control (RBAC) to grant access to your Azure Table resources to users, groups, or applications.

To access a table resource with a `TokenCredential`, the authenticated identity should have either the "Storage Table Data Contributor" or "Storage Table Data Reader" role.

With the `azure-identity` package, you can seamlessly authorize requests in both development and production environments.
To learn more about Azure AD integration in Azure Storage, see the [Azure Identity README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md).

## Key concepts

- **TableServiceClient** - A `TableServiceClient` is a client object that enables you to interact with the Table Service in order to create, list, and delete tables.
- **TableClient** - A `TableClient` is a client object that enables you to interact with a specific table in order to create, upsert, update, get, list, and delete entities within it.
- **Table** - A table is a collection of entities. Tables don't enforce a schema on entities, which means a single table can contain entities that have different sets of properties.
- **Entity** - An entity is a set of properties, similar to a database row. An entity in Azure Storage can be up to 1MB in size. An entity in Azure Cosmos DB can be up to 2MB in size. An entity has a partition key and a row key which together uniquely identify the entity within the table.
- **Properties** - A property is a name-value pair. Each entity can include up to 252 properties to store data. Each entity also has three system properties that specify a partition key, a row key, and a timestamp.
- **Partition Key** - An entity's partition key identifies the partition within the table to which the entity belongs. Entities with the same partition key can be queried more quickly, and inserted/updated in atomic operations.
- **Row Key** - An entity's row key is its unique identifier within a partition.

Common uses of the Tables service include:

- Storing TBs of structured data capable of serving web scale applications
- Storing datasets that don't require complex joins, foreign keys, or stored procedures and can be de-normalized for fast access
- Quickly querying data using a clustered index
- Accessing data using the OData protocol

## Examples

- [Authenticate a client](#authenticate-a-client)
  - [Authenticate with a Connection String](#authenticate-with-a-connection-string)
  - [Authenticate with a Named Key](#authenticate-with-a-named-key)
  - [Authenticate with a Shared Access Signature (SAS)](#authenticate-with-a-shared-access-signature-sas)
- [Create, List, and Delete Azure tables](#create-list-and-delete-azure-tables)
  - [Construct a `TableServiceClient`](#construct-a-tableserviceclient)
  - [Create a table](#create-a-table)
  - [List tables](#list-tables)
  - [Delete a table](#delete-a-table)
- [Create, List, and Delete table entities](#create-list-and-delete-table-entities)
  - [Construct a `TableClient`](#construct-a-tableclient)
  - [Create an entity](#create-an-entity)
  - [List entities](#list-entities)
  - [Delete an entity](#delete-an-entity)

### Authenticate a client

#### Authenticate with a connection string
To use a connection string to authorize your client, call the builder's `connectionString` method with your connection string.

<!-- embedme src/samples/java/ReadmeSamples.java#L38-L40 -->
```java
TableServiceClient tableServiceClient = new TableServiceClientBuilder()
    .connectionString("<your-connection-string>")
    .buildClient();
```

#### Authenticate with a Shared Key
To use a Shared Key to authorize your client, create an instance of `AzureNamedKeyCredential` with your account name and access key. Call the builder's `endpoint` method with your account URL and the `credential` method with the `AzureNamedKeyCredential` object you created.

<!-- embedme src/samples/java/ReadmeSamples.java#L47-L51 -->
```java
AzureNamedKeyCredential credential = new AzureNamedKeyCredential("<your-account-name>", "<account-access-key>");
TableServiceClient tableServiceClient = new TableServiceClientBuilder()
    .endpoint("<your-table-account-url>")
    .credential(credential)
    .buildClient();
```

#### Authenticate with a Shared Access Signature (SAS)
To use a SAS to authorize your client, call the builder's `endpoint` method with your account URL and the `sasToken` method with your SAS.

<!-- embedme src/samples/java/ReadmeSamples.java#L58-L61 -->
```java
TableServiceClient tableServiceClient = new TableServiceClientBuilder()
    .endpoint("<your-table-account-url>")
    .sasToken("<sas-token-string>")
    .buildClient();
```

#### Authenticate with a Token Credentials
To authorize your client via AAD, create an instance of a credentials class that implements `TokenCredential`. Call the builder's `endpoint` method with your account URL and the `credential` method with the `TokenCredential` object you created.

<!-- embedme src/samples/java/ReadmeSamples.java#L68-L72 -->
```java
TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
TableServiceClient tableServiceClient = new TableServiceClientBuilder()
    .endpoint("<your-table-account-url>")
    .credential(tokenCredential)
    .buildClient();
```

### Create, List, and Delete Azure tables

#### Construct a `TableServiceClient`
Construct a `TableServiceClient` by creating an instance of `TableServiceClientBuilder` and then calling the builder's `buildClient` or `buildAsyncClient` methods.

<!-- embedme src/samples/java/ReadmeSamples.java#L79-L81 -->
```java
TableServiceClient tableServiceClient = new TableServiceClientBuilder()
    .connectionString("<your-connection-string>") // or use any of the other authentication methods
    .buildClient();
```

#### Create a table
Create a table by calling the `TableServiceClient`'s `createTable` method. A `TableClient` will be returned, this client allows to perform operations on the table. An exception will be thrown if a table with the provided name exists.

<!-- embedme src/samples/java/ReadmeSamples.java#L90-L90 -->
```java
TableClient tableClient = tableServiceClient.createTable(tableName);
```

Alternatively, you can call the `createTableIfNotExists` method which will create the table only if no such table exists, and does not throw an exception. A `TableClient` will be returned as well.

<!-- embedme src/samples/java/ReadmeSamples.java#L97-L97 -->
```java
TableClient tableClient = tableServiceClient.createTableIfNotExists(tableName);
```

#### List tables
List or query the set of existing tables by calling the `TableServiceClient`'s `listTables` method, optionally passing in a `ListTablesOptions` instance to filter or limit the query results. See [Supported Query Options][query_options] for details about supported query options.

<!-- embedme src/samples/java/ReadmeSamples.java#L104-L109 -->
```java
ListTablesOptions options = new ListTablesOptions()
    .setFilter(String.format("TableName eq '%s'", tableName));

for (TableItem tableItem : tableServiceClient.listTables(options, null, null)) {
    System.out.println(tableItem.getName());
}
```

#### Delete a table
Delete a table by calling the `TableServiceClient`'s `deleteTable` method.

<!-- embedme src/samples/java/ReadmeSamples.java#L116-L116 -->
```java
tableServiceClient.deleteTable(tableName);
```

### Create, List, and Delete table entities

#### Construct a `TableClient`
Construct a `TableClient` by creating an instance of `TableClientBuilder`, calling the builder's `tableName` method with the name of the table, and then calling its `buildClient` or `buildAsyncClient` methods.

<!-- embedme src/samples/java/ReadmeSamples.java#L123-L126 -->
```java
TableClient tableClient = new TableClientBuilder()
    .connectionString("<your-connection-string>") // or use any of the other authentication methods
    .tableName(tableName)
    .buildClient();
```

Alternatively, a `TableClient` can be retrieved from an existing `TableServiceClient` by calling its `getTableClient` method.

<!-- embedme src/samples/java/ReadmeSamples.java#L133-L133 -->
```java
TableClient tableClient = tableServiceClient.getTableClient(tableName);
```

#### Create an entity
Create a new `TableEntity` instance, providing the partition key and row key of the entity to create, optionally adding properties to the created object. Then pass the object to the `TableClient`'s `createEntity` method. An exception will be thrown if an entity with the provided partition key and row key exists within the table.

<!-- embedme src/samples/java/ReadmeSamples.java#L142-L147 -->
```java
TableEntity entity = new TableEntity(partitionKey, rowKey)
    .addProperty("Product", "Marker Set")
    .addProperty("Price", 5.00)
    .addProperty("Quantity", 21);

tableClient.createEntity(entity);
```

#### List entities
List or query the set of entities within the table by calling the `TableClient`'s `listEntities` method, optionally passing in a `ListEntitiesOptions` instance to filter, select, or limit the query results. See [Supported Query Options][query_options] for details about supported query options.

<!-- embedme src/samples/java/ReadmeSamples.java#L154-L165 -->
```java
List<String> propertiesToSelect = new ArrayList<>();
propertiesToSelect.add("Product");
propertiesToSelect.add("Price");

ListEntitiesOptions options = new ListEntitiesOptions()
    .setFilter(String.format("PartitionKey eq '%s'", partitionKey))
    .setSelect(propertiesToSelect);

for (TableEntity entity : tableClient.listEntities(options, null, null)) {
    Map<String, Object> properties = entity.getProperties();
    System.out.println(String.format("%s: %.2f", properties.get("Product"), properties.get("Price")));
}
```

#### Delete an entity
Delete an entity by calling the `TableClient`'s `deleteEntity` method.

<!-- embedme src/samples/java/ReadmeSamples.java#L172-L172 -->
```java
tableClient.deleteEntity(partitionKey, rowKey);
```

## Troubleshooting

### General
When you interact with Tables service using the Azure Tables library for Java, errors returned by the service correspond to the same HTTP status codes returned for [REST API][rest_api] requests.

For example, if you try to create a table that already exists, a `409` error is returned, indicating "Conflict".

<!-- embedme src/samples/java/ReadmeSamples.java#L179-L187 -->
```java
// Create the table if it doesn't already exist.
tableServiceClient.createTableIfNotExists(tableName);

// Now attempt to create the same table unconditionally.
try {
    tableServiceClient.createTable(tableName);
} catch (TableServiceException e) {
    System.out.println(e.getResponse().getStatusCode()); // 409
}
```

### Logging
Enabling logging may help uncover useful information about failures. In order to see a log of HTTP requests and responses, set the `AZURE_LOG_LEVEL` environment variable to the desired verbosity. See [LogLevel][log_level] for a description of available log levels.

## Next steps

Get started with our [Table samples][samples].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- Links -->
[api_documentation]: https://aka.ms/java-docs
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc_contact]: mailto:opencode@microsoft.com
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc]: https://opensource.microsoft.com/codeofconduct/
[cosmosdb_create_cli]: https://docs.microsoft.com/azure/cosmos-db/scripts/cli/table/create
[cosmosdb_create_portal]: https://docs.microsoft.com/azure/cosmos-db/create-table-java#create-a-database-account
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[log_level]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/LogLevel.java
[package]: https://search.maven.org/artifact/com.azure/azure-data-tables
[product_documentation]: https://docs.microsoft.com/azure/cosmos-db/table-storage-overview
[query_options]: https://docs.microsoft.com/rest/api/storageservices/querying-tables-and-entities#supported-query-options
[rest_api]: https://docs.microsoft.com/rest/api/storageservices/table-service-rest-api
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/src/samples/java/
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/src
[storage_account_create_cli]: https://docs.microsoft.com/azure/storage/common/storage-account-create?tabs=azure-cli
[storage_account_create_portal]: https://docs.microsoft.com/azure/storage/common/storage-account-create?tabs=azure-portal

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftables%2Fazure-data-tables%2FREADME.png)
