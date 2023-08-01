// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceException;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class containing code snippets that will be injected to README.md.
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private final String partitionKey = "<partition-key>";
    private final String rowKey = "<row-key>";
    private final String tableName = "<table-name>";
    private final TableServiceClient tableServiceClient = new TableServiceClientBuilder()
        .connectionString("<your-connection-string>") // or use any of the other authentication methods
        .buildClient();
    private final TableClient tableClient = new TableClientBuilder()
        .connectionString("<your-connection-string>") // or use any of the other authentication methods
        .tableName(tableName)
        .buildClient();
    private final TableEntity entity = new TableEntity(partitionKey, rowKey);

    /**
     * Code sample for authenticating with a connection string.
     */
    public void authenticateWithConnectionString() {
        // BEGIN: readme-sample-authenticateWithConnectionString
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("<your-connection-string>")
            .buildClient();
        // END: readme-sample-authenticateWithConnectionString
    }

    /**
     * Code sample for authenticating with a shared key.
     */
    public void authenticateWithNamedKey() {
        // BEGIN: readme-sample-authenticateWithNamedKey
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential("<your-account-name>", "<account-access-key>");
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("<your-table-account-url>")
            .credential(credential)
            .buildClient();
        // END: readme-sample-authenticateWithNamedKey
    }

    /**
     * Code sample for authenticating with a SAS.
     */
    public void authenticateWithSas() {
        // BEGIN: readme-sample-authenticateWithSas
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("<your-table-account-url>")
            .sasToken("<sas-token-string>")
            .buildClient();
        // END: readme-sample-authenticateWithSas
    }

    /**
     * Code sample for authenticating with a {@link TokenCredential}.
     */
    public void authenticateWithTokenCredential() {
        // BEGIN: readme-sample-authenticateWithTokenCredential
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("<your-table-account-url>")
            .credential(tokenCredential)
            .buildClient();
        // END: readme-sample-authenticateWithTokenCredential
    }

    /**
     * Code sample for constructing a service client.
     */
    public void constructServiceClient() {
        // BEGIN: readme-sample-constructServiceClient
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("<your-connection-string>") // or use any of the other authentication methods
            .buildClient();
        // END: readme-sample-constructServiceClient
    }

    /**
     * Code sample for creating a table.
     *
     * @throws TableServiceException if the table exists.
     */
    public void createTable() {
        // BEGIN: readme-sample-createTable
        TableClient tableClient = tableServiceClient.createTable(tableName);
        // END: readme-sample-createTable
    }

    /**
     * Code sample for creating a table if it doesn't exist.
     */
    public void createTableIfNotExists() {
        // BEGIN: readme-sample-createTableIfNotExists
        TableClient tableClient = tableServiceClient.createTableIfNotExists(tableName);
        // END: readme-sample-createTableIfNotExists
    }

    /**
     * Code sample for listing tables.
     */
    public void listTables() {
        // BEGIN: readme-sample-listTables
        ListTablesOptions options = new ListTablesOptions()
            .setFilter(String.format("TableName eq '%s'", tableName));

        for (TableItem tableItem : tableServiceClient.listTables(options, null, null)) {
            System.out.println(tableItem.getName());
        }
        // END: readme-sample-listTables
    }

    /**
     * Code sample for deleting a table.
     */
    public void deleteTable() {
        // BEGIN: readme-sample-deleteTable
        tableServiceClient.deleteTable(tableName);
        // END: readme-sample-deleteTable
    }

    /**
     * Code sample for constructing a table client.
     */
    public void constructTableClient() {
        // BEGIN: readme-sample-constructTableClient
        TableClient tableClient = new TableClientBuilder()
            .connectionString("<your-connection-string>") // or use any of the other authentication methods
            .tableName(tableName)
            .buildClient();
        // END: readme-sample-constructTableClient
    }

    /**
     * Code sample for retrieving a table client from a service client.
     */
    public void retrieveTableClient() {
        // BEGIN: readme-sample-retrieveTableClient
        TableClient tableClient = tableServiceClient.getTableClient(tableName);
        // END: readme-sample-retrieveTableClient
    }

    /**
     * Code sample for creating an entity.
     *
     * @throws TableServiceException if the entity exists.
     */
    public void createEntity() {
        // BEGIN: readme-sample-createEntity
        TableEntity entity = new TableEntity(partitionKey, rowKey)
            .addProperty("Product", "Marker Set")
            .addProperty("Price", 5.00)
            .addProperty("Quantity", 21);

        tableClient.createEntity(entity);
        // END: readme-sample-createEntity
    }

    /**
     * Code sample for listing entities.
     */
    public void listEntities() {
        // BEGIN: readme-sample-listEntities
        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("Product");
        propertiesToSelect.add("Price");

        ListEntitiesOptions options = new ListEntitiesOptions()
            .setFilter(String.format("PartitionKey eq '%s'", partitionKey))
            .setSelect(propertiesToSelect);

        for (TableEntity entity : tableClient.listEntities(options, null, null)) {
            Map<String, Object> properties = entity.getProperties();
            System.out.printf("%s: %.2f%n", properties.get("Product"), properties.get("Price"));
        }
        // END: readme-sample-listEntities
    }

    /**
     * Code sample for deleting an entity.
     */
    public void deleteEntity() {
        // BEGIN: readme-sample-deleteEntity
        tableClient.deleteEntity(partitionKey, rowKey);
        // END: readme-sample-deleteEntity
    }

    /**
     * Code sample for accessing information about an error.
     */
    public void accessErrorInfo() {
        // BEGIN: readme-sample-accessErrorInfo
        // Create the table if it doesn't already exist.
        tableServiceClient.createTableIfNotExists(tableName);

        // Now attempt to create the same table unconditionally.
        try {
            tableServiceClient.createTable(tableName);
        } catch (TableServiceException e) {
            System.out.println(e.getResponse().getStatusCode()); // 409
        }
        // END: readme-sample-accessErrorInfo
    }
}
