// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.codesnippets;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.data.tables.TableServiceAsyncClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class TableServiceClientBuilderJavaDocCodeSnippets {

    /**
     * Generates a code sample for using {@link TableServiceClientBuilder#buildClient()} using a connection string.
     */
    public void buildClientWithConnectionString() {
        // BEGIN: com.azure.data.tables.tableServiceClientBuilder.connectionString#string
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionstring")
            .buildClient();
        // END: com.azure.data.tables.tableServiceClientBuilder.connectionString#string
    }

    /**
     * Generates a code sample for using {@link TableServiceClientBuilder#buildClient()} using SharedKeyCredential.
     */
    public void buildClientWithSharedKeyCredential() {
        // BEGIN: com.azure.data.tables.tableServiceClientBuilder.credential#sharedKeyCredential
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("endpoint")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .buildClient();
        // END: com.azure.data.tables.tableServiceClientBuilder.credential#sharedKeyCredential
    }

    /**
     * Generates a code sample for using {@link TableServiceClientBuilder#buildClient()} using AzureSasCredential.
     */
    public void buildClientWithAzureSasCredential() {
        // BEGIN: com.azure.data.tables.tableServiceClientBuilder.credential#azureSasCredential
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("endpoint")
            .credential(new AzureSasCredential("sasToken"))
            .buildClient();
        // END: com.azure.data.tables.tableServiceClientBuilder.credential#azureSasCredential
    }

    /**
     * Generates a code sample for using {@link TableServiceClientBuilder#buildClient()} using a SAS token.
     */
    public void buildClientWithSasToken() {
        // BEGIN: com.azure.data.tables.tableServiceClientBuilder.sasToken#string
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("endpoint")
            .sasToken("sasToken")
            .buildClient();
        // END: com.azure.data.tables.tableServiceClientBuilder.sasToken#string
    }

    /**
     * Generates a code sample for using {@link TableServiceClientBuilder#buildClient()} using a TokenCredential.
     */
    public void buildClientWithTokenCredential() {
        // BEGIN: com.azure.data.tables.tableServiceClientBuilder.credential#tokenCredential
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("endpoint")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.data.tables.tableServiceClientBuilder.credential#tokenCredential
    }

    /**
     * Generates a code sample for using {@link TableServiceClientBuilder#buildClient()} to build a synchronous client.
     */
    public void buildSyncClient() {
        // BEGIN: com.azure.data.tables.tableServiceClientBuilder.sync
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildClient();
        // END: com.azure.data.tables.tableServiceClientBuilder.sync
    }

    /**
     * Generates a code sample for using {@link TableServiceClientBuilder#buildAsyncClient()} to build an asynchronous client.
     */
    public void buildAsyncClient() {
        // BEGIN: com.azure.data.tables.tableServiceClientBuilder.async
        TableServiceAsyncClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();
        // END: com.azure.data.tables.tableServiceClientBuilder.async
    }
}
