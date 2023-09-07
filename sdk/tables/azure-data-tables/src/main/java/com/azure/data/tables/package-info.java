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
 * <h3>Authenticating a Client</h3>
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
 * <h3>Table Service Clients</h3>
 *
 * <p>The {@link com.azure.data.tables.TableServiceClient TableServiceClient} and {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient} provide access to the tables within an
 * Azure Storage or Azure Cosmos account. A table service client can create, list, and delete tables. It also provides access to a table client that can be used to perform CRUD operations on entities
 * within a table. You can instantiate a table service client using an instance of {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder TableServiceClientBuilder}.</p>
 *
 * <p>Here's an example of creating a synchronous table service client:</p>
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.instantiation.package -->
 * <pre>
 * TableServiceClient tableServiceClient = new TableServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.instantiation.package -->
 *
 * <em>To create an asynchronous table service client, call {@link com.azure.data.tables.TableServiceClientBuilder#buildAsyncClient() buildAsyncClient()} instead of {@link com.azure.data.tables.TableServiceClientBuilder#buildClient() buildClient()}.</em>
 *
 * <p>Here are some examples of some common table operations:</p>
 *
 * <strong>Creating a Table</strong>
 *
 * The following example creates a table with the name "tableName".
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.createTable.package#String -->
 * <pre>
 * tableServiceClient.createTable&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.createTable.package#String -->
 *
 * <strong>Listing Tables</strong>
 *
 * The following example lists all the tables in the account without any filtering.
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
 * <strong>Deleting a Table</strong>
 *
 * The following example deletes a table with the name "tableName".
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.deleteTable.package#String -->
 * <pre>
 * tableServiceClient.deleteTable&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.deleteTable.package#String -->
 *
 * <strong>Returning a TableClient</strong> The following example returns a table client for a table with the name "tableName".
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.getTableClient.package#String -->
 * <pre>
 * TableClient tableClient = tableServiceClient.getTableClient&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.getTableClient.package#String -->
 *
 * <p>For more detailed examples, view the {@link com.azure.data.tables.TableServiceClient TableServiceClient} and {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient} documentation.</p>
 *
 * <hr/>
 *
 * <h3>Table Clients</h3>
 *
 * <p>The {@link com.azure.data.tables.TableClient TableClient} and {@link com.azure.data.tables.TableAsyncClient} provide access to a specific table within an Azure Storage or Azure Cosmos account.
 * A table client can be used to perform CRUD and query operations on entities within a table. Table clients can also create* new tables and delete the table they reference from the Azure Storage or
 * Cosmos acount. An instance of a table client can be returned via a table service client or can be instantiated using an instance of
 * {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}.</p>
 *
 * <em>* Tables created from a table client do not return a new TableClient instance. Table client instances cannot change the table they reference. To reference the newly created table, a new table
 * client instance must be instantiated referencing the table.</em>
 *
 * <p>Heres an example of creating a synchronous table client from a service client:</p>
 *
 * <!-- src_embed com.azure.data.tables.TableClient.instantiationFromServiceClient.package -->
 * <pre>
 * TableClient tableClient = tableServiceClient.getTableClient&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.instantiationFromServiceClient.package -->
 *
 * <p>Here's an example of creating a synchronous table client using a builder:</p>
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
 * <em>To create an asynchronous table client, call {@link com.azure.data.tables.TableClientBuilder#buildAsyncClient() buildAsyncClient()} instead of {@link com.azure.data.tables.TableClientBuilder#buildClient() buildClient()},
 * or instantiate from a {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient}.</em>
 *
 * <p>Here are some examples of some common entity operations:</p>
 *
 * <strong>Creating an Entity</strong>
 *
 * The following example creates an entity with a partition key of "partitionKey" and a row key of "rowKey".
 *
 * <!-- src_embed com.azure.data.tables.TableClient.createEntity.package#Map -->
 * <pre>
 * tableClient.createEntity&#40;new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .addProperty&#40;&quot;property&quot;, &quot;value&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.createEntity.package#Map -->
 *
 * <strong>Updating an Entity</strong>
 *
 * The following example updates an entity with a partition key of "partitionKey" and a row key of "rowKey", adding an additional property with the name "newProperty" and the value "newValue".
 *
 * <!-- src_embed com.azure.data.tables.TableClient.updateEntity.package#TableEntity -->
 * <pre>
 * TableEntity entity = tableClient.getEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * entity.addProperty&#40;&quot;newProperty&quot;, &quot;newValue&quot;&#41;;
 * tableClient.updateEntity&#40;entity&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.updateEntity.package#TableEntity -->
 *
 * <strong>Listing Entities</strong>
 *
 * The following example lists all entities in a table witout any filtering.
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
 * <strong>Deleting an Entity</strong>
 *
 * The following example deletes an entity with a partition key of "partitionKey" and a row key of "rowKey".
 *
 * <!-- src_embed com.azure.data.tables.TableClient.deleteEntity.package#TableEntity -->
 * <pre>
 * tableClient.deleteEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.deleteEntity.package#TableEntity -->
 *
 * <p>For more detailed examples, view the {@link com.azure.data.tables.TableClient TableClient} and {@link com.azure.data.tables.TableAsyncClient TableAsyncClient} documentation.</p>
 *
 * @see com.azure.data.tables.TableServiceClient
 * @see com.azure.data.tables.TableServiceAsyncClient
 * @see com.azure.data.tables.TableServiceClientBuilder
 * @see com.azure.data.tables.TableClient
 * @see com.azure.data.tables.TableAsyncClient
 * @see com.azure.data.tables.TableClientBuilder
 */
package com.azure.data.tables;
