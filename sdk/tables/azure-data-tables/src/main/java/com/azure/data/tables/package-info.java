// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure Tables client library provides access to the database service Azure Tables. The client library supports both Azure Storage and Azure Cosmos tables. This library provides query and CRUD
 * oprations for tables and entities within the tables. The Azure Tables serivice stores NoSQU data in the cloud, providing a key/attribute store with a schemaless design. </p>
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
 * <em>Table service clients utilize their authentication information to create table clients. Table clients created via a table service client will inherit the authentication information of the table
 *  service client.</em>
 * <p>See client builder class documentation {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder} and {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}
 * for examples of authenticating a client.</p>
 * <hr/>
 * <h3>Building a Client</h3>
 * <p>The {@link com.azure.data.tables.TableServiceClient TableServiceClient} and {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient} provide access to the tables within an 
 * Azure Storage or Azure Cosmos account. A table service client can create, list, and delete tables. It also provides access to a table client that can be used to perform CRUD operations on entities
 * within a table. You can instantiate a table service client using an instance of {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder TableServiceClientBuilder}.</p>
 * 
 * insert CodeSnippet: TablesJavaInstantiateServiceClient
 * 
 * <p>The {@link com.azure.data.tables.TableClient TableClient} and {@link com.azure.data.tables.TableAsyncClient} provide access to a specific table within an Azure Storage or Azure Cosmos account.
 * A table client can be used to perform CRUD and query operations on entities within a table. Table clients can also create* new tables and delete the table they reference from the Azure Storage or
 * Cosmos acount. An instance of a table client can be returned via a table service client or can be instantiated using an instance of
 * {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}.</p>
 * 
 * <em>* Tables created from a table client do not return a new TableClient instance. Table client instances cannot change the table they reference. To reference the newly created table, a new table
 * client instance must be instantiated referencing the table.</em>
 * 
 * <p>See client builder class documentation {@link com.azure.data.tables.TableServiceClientBuilder TableServiceClientBuilder} and {@link com.azure.data.tables.TableClientBuilder TableClientBuilder}
 * for examples of building a client.</p>
 * <hr/>
 * <h3>Client Operations</h3>
 * <p>Once you have an instance of a table client or table service client, you can perform operations on the table or tables within the account. See client class documentation
 * {@link com.azure.data.tables.TableServiceClient TableServiceClient}, {@link com.azure.data.tables.TableServiceAsyncClient TableServiceAsyncClient},
 * {@link com.azure.data.tables.TableClient TableClient}, {@link com.azure.data.tables.TableAsyncClient TableAsyncClient} for examples of operations you can perform on the client.</p>
 * 
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
