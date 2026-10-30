// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.codesnippets;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;


public class TableClientBuilderJavaDocSnippets {
    /**
     * Generates a code sample for using {@link TableClientBuilder#buildClient()} using a connection string.
     */
    public void buildClientWithConnectionString() {
        // BEGIN: com.azure.data.tables.tableClientBuilder.connectionString#string
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("tableName")
            .buildClient();
        // END: com.azure.data.tables.tableClientBuilder.connectionString#string
    }

    /**
     * Generates a code sample for using {@link TableClientBuilder#buildClient()} using a SharedKeyCredential.
     */
    public void buildClientWithSharedKeyCredential() {
        // BEGIN: com.azure.data.tables.tableClientBuilder.credential#sharedKeyCredential
        TableClient tableClient = new TableClientBuilder()
            .credential(new AzureNamedKeyCredential("name", "key"))
            .tableName("tableName")
            .endpoint("endpoint")
            .buildClient();
        // END: com.azure.data.tables.tableClientBuilder.credential#sharedKeyCredential
    }

    /**
     * Generates a code sample for using {@link TableClientBuilder#buildClient()} using AzureSasCredential.
     */
    public void buildClientWithAzureSasCredential() {
        // BEGIN: com.azure.data.tables.tableClientBuilder.credential#azureSasCredential
        TableClient tableClient = new TableClientBuilder()
            .credential(new AzureSasCredential("sasToken"))
            .endpoint("endpoint")
            .tableName("tableName")
            .buildClient();
        // END: com.azure.data.tables.tableClientBuilder.credential#azureSasCredential
    }

    /**
     * Generates a code sample for using {@link TableClientBuilder#buildClient()} using a sas tokwn
     */
    public void buildClientWithSasToken() {
        // BEGIN: com.azure.data.tables.tableClientBuilder.sasToken#string
        TableClient tableClient = new TableClientBuilder()
            .sasToken("sasToken")
            .endpoint("endpoint")
            .tableName("tableName")
            .buildClient();
        // END: com.azure.data.tables.tableClientBuilder.sasToken#string
    }

    /**
     * Generates a code sample for using {@link TableClientBuilder#buildClient()} using a token credential.
     */
    public void buildClientWithTokenCredential() {
        // BEGIN: com.azure.data.tables.tableClientBuilder.tokenCredential#tokenCredential
        TableClient tableClient = new TableClientBuilder()
            .endpoint("endpoint")
            .credential(new DefaultAzureCredentialBuilder().build())
            .tableName("tableName")
            .buildClient();
        // END: com.azure.data.tables.tableClientBuilder.tokenCredential#tokenCredential
    }

    /**
     * Generates a code sample for using {@link TableClientBuilder#buildAsyncClient()} to build an asynchraonous client.
     */
    public void buildAsyncClient() {
        // BEGIN: com.azure.data.tables.tableClientBuilder.buildAsyncClient
        TableAsyncClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("tableName")
            .buildAsyncClient();
        // END: com.azure.data.tables.tableClientBuilder.buildAsyncClient
    }

    /**
     * Generates a code sample for using {@link TableClientBuilder#buildClient()} to build a synchronous client.
     */
    public void buildSyncClient() {
        // BEGIN: com.azure.data.tables.tableClientBuilder.buildClient
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("tableName")
            .buildClient();
        // END: com.azure.data.tables.tableClientBuilder.buildClient
    }
}
