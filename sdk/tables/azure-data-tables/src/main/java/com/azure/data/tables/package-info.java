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
 * <p>The client library package requires the following:</p>
 * <ul>
 * <li>Java 8 or later</li>
 * <li>An Azure subscription</li>
 * <li>An existing Azure Storage or Azure Cosmos account</li>
 * </ul>
 * <hr/>
 * <h3>Authenticate the Client</h3>
 * <p>In order to build a valid table client or table service client, you will need to authenticate the client using an accepted method of authentication. The supported forms of authentication are:
 * <ul>
 * <li>Connection String</li>
 * <li>Shared Key</li>
 * <li>Shared Access Signature (SAS)</li>
 * <li>Token Credential</li>
 * </ul>
 * <p>For more information on authentication types, see <a href=https://learn.microsoft.com/azure/developer/java/sdk/identity></a></p>
 * <em>Table service clients utilize their authentication information to create table clients. Table clients created via a table service client will inherit the authentication information of the table
 *  service client.</em>
 * <p>See client builder class documentation {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder} and {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}
 * for examples of authenticating a client.</p>
 * <hr/>
 * <h3>Building Clients</h3>
 * <h4>Table Service Clients</h4>
 * <p>The {@link com.azure.data.tables.TableServiceClient TableServiceClient} and {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient} provide access to the tables within an 
 * Azure Storage or Azure Cosmos account. A table service client can create, list, and delete tables. It also provides access to a table client that can be used to perform CRUD operations on entities
 * within a table. You can instantiate a table service client using an instance of {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder TableServiceClientBuilder}.</p>
 * <h5>Examples</h5>
 * <p>Here's an example of creating a synchronous table service client:</p> 
 * <!-- src_embed com.azure.data.tables.TableServiceClient.instantiation.package -->
 * <pre>
 * TableServiceClient tableServiceClient = new TableServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.instantiation.package -->
 *  
 * <p>Here's an example of creating an asynchronous table service client:</p>
 * <!-- src_embed com.azure.data.tables.TableServiceAsyncClient.instantiation.package -->
 * <pre>
 * TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceAsyncClient.instantiation.package -->
 * 
 * <h4>Table Clients</h4>
 * <p>The {@link com.azure.data.tables.TableClient TableClient} and {@link com.azure.data.tables.TableAsyncClient} provide access to a specific table within an Azure Storage or Azure Cosmos account.
 * A table client can be used to perform CRUD and query operations on entities within a table. Table clients can also create* new tables and delete the table they reference from the Azure Storage or
 * Cosmos acount. An instance of a table client can be returned via a table service client or can be instantiated using an instance of
 * {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}.</p>
 * 
 * <em>* Tables created from a table client do not return a new TableClient instance. Table client instances cannot change the table they reference. To reference the newly created table, a new table
 * client instance must be instantiated referencing the table.</em>
 * <h5>Examples</h5>
 * <p>Heres an example of creating a synchronous table client from a service client:</p>
 * <!-- src_embed com.azure.data.tables.TableClient.instantiationFromServiceClient.package -->
 * <pre>
 * TableClient tableClient = tableServiceClient.getTableClient&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.instantiationFromServiceClient.package -->
 * 
 * <p>Here's an example of creating a synchronous table client using a builder:</p>
 * <!-- src_embed com.azure.data.tables.TableClient.instantiationFromBuilder.package -->
 * <pre>
 * TableClient tableClient = new TableClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .tableName&#40;&quot;tableName&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.instantiationFromBuilder.package -->
 * 
 * <p>Here's an example of creating an asynchronous table client from a service client:</p>
 * <!-- src_embed com.azure.data.tables.TableAsyncClient.instantiationFromServiceAsyncClient.package -->
 * <pre>
 * TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableAsyncClient.instantiationFromServiceAsyncClient.package -->
 * 
 * <p>Here's an example of creating an asynchronous table client from a builder:</p>
 * <!-- src_embed com.azure.data.tables.TableClient.instantiationFromBuilder.package#async -->
 * <pre>
 * TableAsyncClient tableClient = new TableClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .tableName&#40;&quot;tableName&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.instantiationFromBuilder.package#async -->
 * 
 * <hr/>
 * <h3>Client Operation Examples</h3>
 * <p>Once you have an instance of a table client or table service client, you can perform operations on the table or tables within the account.</p>
 * 
 * <h4><u>Table Operations</u></h4>
 * 
 * <p>Here are some examples of some common table operations:</p>
 * 
 * <strong>Create a Table</strong>
 * 
 * <!-- src_embed com.azure.data.tables.TableServiceClient.createTable.package#String -->
 * <pre>
 * tableServiceClient.createTable&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.createTable.package#String -->
 * 
 * <strong>List Tables</strong>
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
 * <strong>Delete a Table</strong>
 * 
 * <!-- src_embed com.azure.data.tables.TableServiceClient.deleteTable.package#String -->
 * <pre>
 * tableServiceClient.deleteTable&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.deleteTable.package#String -->
 * 
 * <strong>Return a TableClient</strong>
 *
 * <!-- src_embed com.azure.data.tables.TableServiceClient.getTableClient.package#String -->
 * <pre>
 * TableClient tableClient = tableServiceClient.getTableClient&#40;&quot;tableName&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableServiceClient.getTableClient.package#String --> 
 * 
 * 
 * <h4><u>Entity Operations</u></h4>
 * 
 * <p>Here are some examples of some common entity operations:</p>
 * 
 * <strong>Create an Entity</strong>
 * 
 * <!-- src_embed com.azure.data.tables.TableClient.createEntity.package#Map -->
 * <pre>
 * tableClient.createEntity&#40;new TableEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;
 *     .addProperty&#40;&quot;property&quot;, &quot;value&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.createEntity.package#Map -->
 * 
 * <strong>Update an Entity</strong>
 * 
 * <!-- src_embed com.azure.data.tables.TableClient.updateEntity.package#TableEntity -->
 * <pre>
 * TableEntity entity = tableClient.getEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * entity.addProperty&#40;&quot;newProperty&quot;, &quot;newValue&quot;&#41;;
 * tableClient.updateEntity&#40;entity&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.updateEntity.package#TableEntity -->
 * 
 * <strong>List Entities</strong>
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
 * <strong>Delete an Entity</strong>
 * 
 * <!-- src_embed com.azure.data.tables.TableClient.deleteEntity.package#TableEntity -->
 * <pre>
 * tableClient.deleteEntity&#40;&quot;partitionKey&quot;, &quot;rowKey&quot;&#41;;
 * </pre>
 * <!-- end com.azure.data.tables.TableClient.deleteEntity.package#TableEntity -->
 * 
 * @see com.azure.data.tables.TableServiceClient
 * @see com.azure.data.tables.TableServiceAsyncClient
 * @see com.azure.data.tables.TableServiceClientBuilder
 * @see com.azure.data.tables.TableClient
 * @see com.azure.data.tables.TableAsyncClient
 * @see com.azure.data.tables.TableClientBuilder
 * @see com.azure.data.tables.sas.TableSasSignatureValues
 * @see com.azure.core.credential.AzureNamedKeyCredential
 * @see com.azure.core.credential.TokenCredential
 */
package com.azure.data.tables;
