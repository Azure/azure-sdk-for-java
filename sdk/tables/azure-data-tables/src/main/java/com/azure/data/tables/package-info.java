// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>Azure Tables is a NoSQL key-value storage service offered by Microsoft Azure, which provides a highly
 * scalable and cost-effective solution for storing structured data. It is a fully managed service that is designed to
 * handle large volumes of structured data in a distributed environment, with low latency and high availability.
 * Azure Tables stores data in tables, which are schema-less and can contain any type of data. Each table can have
 * a partition key and a row key, which together form a unique primary key that can be used to retrieve individual
 * records. This enables fast, efficient querying of data, especially when combined with Azure's indexing and query
 * features.</p>
 *
 * <p>The Azure Tables client library enables Java developers to easily interact with Azure Tables Storage Service
 * from their Java applications. This library provides a set of APIs that abstract the low-level details of working with
 * the Azure Tables Storage Service and allows developers to perform common operations such as creating tables,
 * inserting, updating and deleting entities, querying data, and managing access control on tables and entities.
 * The library supports both Azure Storage and Azure Cosmos tables. It offers both
 * synchronous and asynchronous programming models. It also provides features such as retries, automatic
 * pagination, and parallelism to enable efficient and reliable interactions with Azure Tables Storage Service.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <h3>Prerequisites</h3>
 *
 * <p>The client library package requires the following:</p>
 *
 * <ul>
 *  <li>Java 8 or later</li>
 *  <li>An Azure subscription</li>
 *  <li>An existing Azure Storage or Azure Cosmos account</li>
 * </ul>
 *
 * <hr/>
 *
 * <h3>Authenticate a Client</h3>
 *
 * <p>In order to build a valid table client or table service client, you will need to authenticate the client using an accepted method of authentication. The supported forms of authentication are:
 *
 * <ul>
 *  <li><a href="https://learn.microsoft.com/java/api/overview/azure/data-tables-readme?view=azure-java-stable#connection-string">Connection String</a></li>
 *  <li><a href="https://learn.microsoft.com/java/api/com.azure.core.credential.azurenamedkeycredential?view=azure-java-stable">Shared Key</a></li>
 *  <li><a href="https://learn.microsoft.com/java/api/com.azure.core.credential.azuresascredential?view=azure-java-stable">Shared Access Signature (SAS)</a></li>
 *  <li><a href="https://learn.microsoft.com/java/api/com.azure.core.credential.tokencredential?view=azure-java-stable">Token Credential</a></li>
 * </ul>
 *
 * <p>See client builder class documentation {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder} and {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}
 * for examples of authenticating a client.</p>
 *
 * <p>For more information on authentication types, see <a href=https://learn.microsoft.com/azure/developer/java/sdk/identity>the identity documentation</a>.</p>
 *
 * <em>Table service clients utilize their authentication information to create table clients. Table clients created via a table service client will inherit the authentication information of the table
 *  service client.</em>
 *
 * <hr/>
 *
 * <h3>Overview</h3>
 *
 * <p>The {@link com.azure.data.tables.TableServiceClient TableServiceClient} and {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient} provide access to the tables within an
 * Azure Storage or Azure Cosmos account. A table service client can create, list, and delete tables. It also provides access to a table client that can be used to perform CRUD operations on entities
 * within a table. You can instantiate a table service client using an instance of {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder}.</p>
 *
 * <p>The {@link com.azure.data.tables.TableClient TableClient} and {@link com.azure.data.tables.TableAsyncClient} provide access to a specific table within an Azure Storage or Azure Cosmos account.
 * A table client can be used to perform CRUD and query operations on entities within a table. Table clients can also create* new tables and delete the table they reference from the Azure Storage or
 * Cosmos acount. An instance of a table client can be returned via a table service client or can be instantiated using an instance of
 * {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}.</p>
 *
 * <em>* Tables created from a table client do not return a new TableClient instance. Table client instances cannot change the table they reference. To reference the newly created table, a new table
 * client instance must be instantiated referencing the table.</em>
 *
 *  <p>See methods in client level class below to explore all capabilities the library provides.</p>
 *
 * <hr/>
 *
 * <h2>Table Service Client Usage</h2>
 *
 * <h3>Create a {@link com.azure.data.tables.TableServiceClient TableServiceClient} using a {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableServiceClient TableServiceClient} and {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient} both provide access to the tables within an
 * Azure Storage or Azure Cosmos account. A table service client can create, list, and delete tables. It also provides access to a table client that can be used to perform CRUD operations on entities
 * within a table. You can instantiate a table service client using an instance of {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder}.</p>
 *
 * <p>Here's an example of creating a synchronous table service client using the {@link com.azure.data.tables.TableServiceClientBuilder#buildClient()} TableServiceClientBuilder buildClient} method:</p>
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.instantiation.package -->
 * <pre>
 * TableServiceClient tableServiceClient = new TableServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.instantiation.package -->
 *
 * <p><strong>Note:</strong> To create an asynchronous table service client, call {@link com.azure.data.tables.TableServiceClientBuilder#buildAsyncClient() buildAsyncClient()} instead of {@link com.azure.data.tables.TableServiceClientBuilder#buildClient() buildClient()}.</p>
 *
 * <hr/>
 *
 *
 * <h3>Create a Table using {@link com.azure.data.tables.TableServiceClient TableServiceClient}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableServiceClient#createTable(java.lang.String) TableServiceClient createTable} method can be used to create an new table within your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example creates a table with the name "tableName".</p>
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.createTable.package#String -->
 * <pre>
 * tableServiceClient.createTable&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.createTable.package#String -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient}</p>
 *
 * <hr/>
 *
 * <h3>List Tables using {@link com.azure.data.tables.TableServiceClient TableServiceClient}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableServiceClient#listTables() TableServiceClient listTables} method can be used to list the tables that are within an Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example lists all the tables in the account without any filtering of Tables.</p>
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.listTables.package -->
 * <pre>
 * tableServiceClient.listTables&#40;&#41;.forEach&#40;table -&gt; &#123;
 *     String tableName = table.getName&#40;&#41;;
 *     System.out.println&#40;&quot;Table name: &quot; + tableName&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.listTables.package -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient}</p>
 *
 * <hr/>
 *
 * <h3>Delete a Table using {@link com.azure.data.tables.TableServiceClient TableServiceClient}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableServiceClient#deleteTable(java.lang.String) TableServiceClient deleteTable} method can be used to delete a table that is within your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example deletes a table with the name "tableName".</p>
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.deleteTable.package#String -->
 * <pre>
 * tableServiceClient.deleteTable&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.deleteTable.package#String -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient}</p>
 *
 * <hr/>
 *
 * <h2>Table Client Usage</h2>
 *
 * <p>The {@link com.azure.data.tables.TableClient TableClient} and {@link com.azure.data.tables.TableAsyncClient} provide access to a specific table within an Azure Storage or Azure Cosmos account.
 * A table client can be used to perform CRUD and query operations on entities within a table. Table clients can also create* new tables and delete the table they reference from the Azure Storage or
 * Cosmos acount. An instance of a table client can be returned via a table service client or can be instantiated using an instance of
 * {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}.</p>
 *
 * <h3>Retrieve a {@link com.azure.data.tables.TableClient TableClient} from a {@link com.azure.data.tables.TableServiceClient TableServiceClient}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableServiceClient#getTableClient(java.lang.String)}  TableServiceClient getTableClient} method can be used to retrieve a TableClient for a specified table that is stored within your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example returns a table client for a table with the name "tableName".</p>
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.getTableClient.package#String -->
 * <pre>
 * TableClient tableClient = tableServiceClient.getTableClient&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.getTableClient.package#String -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient}</p>
 *
 * <hr/>
 *
 * <h3>Create a {@link com.azure.data.tables.TableClient TableClient} using a {@link  com.azure.data.tables.TableClientBuilder TableClientBuilder}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableClient TableClient} and {@link com.azure.data.tables.TableAsyncClient} provide access to a specific table within an Azure Storage or Azure Cosmos account.
 * The {@link com.azure.data.tables.TableClientBuilder#buildClient() TableClientBuilder buildClient} method can be used to create a {@link com.azure.data.tables.TableClient TableClient}</p>
 *
 * <p>Here's an example of creating a TableClient using a builder:</p>
 *
 * <!-- src_embed com.azure.data.tables.TableClient.instantiationFromBuilder.package -->
 * <pre>
 * TableClient tableClient = new TableClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .tableName&#40;&quot;tableName&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.instantiationFromBuilder.package -->
 *
 * <p><strong>Note:</strong> To create an TableAsyncClient, call {@link com.azure.data.tables.TableClientBuilder#buildAsyncClient() buildAsyncClient()} instead of {@link com.azure.data.tables.TableClientBuilder#buildClient() buildClient()}.</p>
 *
 * <hr/>
 *
 * <h3>Create an Entity using {@link com.azure.data.tables.TableClient TableClient}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableClient#createEntity(com.azure.data.tables.models.TableEntity) TableClient createEntity} method can be used to create a table entity within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example creates an entity with a partition key of "partitionKey" and a row key of "rowKey".</p>
 *
 * <!-- src_embed com.azure.data.tables.TableClient.createEntity.package#Map -->
 * <pre>
 * tableClient.createEntity&#40;new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .addProperty&#40;&quot;property&quot;, &quot;value&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.createEntity.package#Map -->
 *
 * <p><strong>Note:</strong> for asynchronous sample, refer to {@link com.azure.data.tables.TableAsyncClient TableAsyncClient}</p>
 *
 * <hr/>
 *
 * <h3>Update an Entity using {@link com.azure.data.tables.TableClient TableClient}</h3>
 *
 * <p>The {@link com.azure.data.tables.TableClient#updateEntity(com.azure.data.tables.models.TableEntity) TableClient updateEntity} method can be used to update a table entity within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example updates an entity with a partition key of "partitionKey" and a row key of "rowKey", adding an additional property with the name "newProperty" and the value "newValue".</p>
 *
 * <!-- src_embed com.azure.data.tables.TableClient.updateEntity.package#TableEntity -->
 * <pre>
 * TableEntity entity = tableClient.getEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * entity.addProperty&#40;&quot;newProperty&quot;, &quot;newValue&quot;&#41;;
 * tableClient.updateEntity&#40;entity&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.updateEntity.package#TableEntity -->
 *
 * <p><strong>Note:</strong> for asynchronous sample, refer to {@link com.azure.data.tables.TableAsyncClient TableAsyncClient}</p>
 *
 * <hr/>
 *
 * <h3>List Entities</h3>
 *
 * <p>The {@link com.azure.data.tables.TableClient#listEntities()  TableClient listEntities} method can be used to list the entities that are within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example lists all entities in a table without any filtering.</p>
 *
 * <!-- src_embed com.azure.data.tables.TableClient.listEntities.package -->
 * <pre>
 * tableClient.listEntities&#40;&#41;.forEach&#40;entity -&gt; &#123;
 *     String partitionKey = entity.getPartitionKey&#40;&#41;;
 *     String rowKey = entity.getRowKey&#40;&#41;;
 *     System.out.println&#40;&quot;Partition key: &quot; + partitionKey + &quot;, Row key: &quot; + rowKey&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.listEntities.package -->
 *
 * <p><strong>Note:</strong> for asynchronous sample, refer to {@link com.azure.data.tables.TableAsyncClient TableAsyncClient}</p>
 *
 * <hr/>
 *
 * <h3>Delete an Entity</h3>
 *
 * <p>The {@link com.azure.data.tables.TableClient#deleteEntity(com.azure.data.tables.models.TableEntity) TableClient deleteEntity} method can be used to delete an entity that is within a table in your Azure Storage or Azure Cosmos account.</p>
 *
 * <p>The following example deletes an entity with a partition key of "partitionKey" and a row key of "rowKey".</p>
 *
 * <!-- src_embed com.azure.data.tables.TableClient.deleteEntity.package#TableEntity -->
 * <pre>
 * tableClient.deleteEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.deleteEntity.package#TableEntity -->
 *
 * <p><strong>Note:</strong> for asynchronous sample, refer to {@link com.azure.data.tables.TableAsyncClient TableAsyncClient}</p>
 *
 * @see com.azure.data.tables.TableServiceClient
 * @see com.azure.data.tables.TableServiceAsyncClient
 * @see com.azure.data.tables.TableServiceClientBuilder
 * @see com.azure.data.tables.TableClient
 * @see com.azure.data.tables.TableAsyncClient
 * @see com.azure.data.tables.TableClientBuilder
 */
package com.azure.data.tables;
