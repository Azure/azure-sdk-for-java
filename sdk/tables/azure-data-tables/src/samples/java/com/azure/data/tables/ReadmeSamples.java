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
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private final String partitionKey = "<partition-key>";
    private final String rowKey = "<row-key>";
    private final String tableName = "<table-name>";
    private final TableServiceClient tableServiceClient = new TableServiceClient(null);
    private final TableClient tableClient = new TableClient(null);
    private final TableEntity entity = new TableEntity(partitionKey, rowKey);

    /**
     * Code sample for authenticating with a connection string.
     */
    public void authenticateWithConnectionString() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("<your-connection-string>")
            .buildClient();
    }

    /**
     * Code sample for authenticating with a shared key.
     */
    public void authenticateWithNamedKey() {
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential("<your-account-name>", "<account-access-key>");
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("<your-table-account-url>")
            .credential(credential)
            .buildClient();
    }

    /**
     * Code sample for authenticating with a SAS.
     */
    public void authenticateWithSas() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("<your-table-account-url>")
            .sasToken("<sas-token-string>")
            .buildClient();
    }

    /**
     * Code sample for authenticating with a {@link TokenCredential}.
     */
    public void authenticateWithTokenCredential() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("<your-table-account-url>")
            .credential(tokenCredential)
            .buildClient();
    }

    /**
     * Code sample for constructing a service client.
     */
    public void constructServiceClient() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("<your-connection-string>") // or use any of the other authentication methods
            .buildClient();
    }

    /**
     * Code sample for creating a table.
     *
     * @throws TableServiceException if the table exists.
     */
    public void createTable() {
        TableClient tableClient = tableServiceClient.createTable(tableName);
    }

    /**
     * Code sample for creating a table if it doesn't exist.
     */
    public void createTableIfNotExists() {
        TableClient tableClient = tableServiceClient.createTableIfNotExists(tableName);
    }

    /**
     * Code sample for listing tables.
     */
    public void listTables() {
        ListTablesOptions options = new ListTablesOptions()
            .setFilter(String.format("TableName eq '%s'", tableName));

        for (TableItem tableItem : tableServiceClient.listTables(options, null, null)) {
            System.out.println(tableItem.getName());
        }
    }

    /**
     * Code sample for deleting a table.
     */
    public void deleteTable() {
        tableServiceClient.deleteTable(tableName);
    }

    /**
     * Code sample for constructing a table client.
     */
    public void constructTableClient() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("<your-connection-string>") // or use any of the other authentication methods
            .tableName(tableName)
            .buildClient();
    }

    /**
     * Code sample for retrieving a table client from a service client.
     */
    public void retrieveTableClient() {
        TableClient tableClient = tableServiceClient.getTableClient(tableName);
    }

    /**
     * Code sample for creating an entity.
     *
     * @throws TableServiceException if the entity exists.
     */
    public void createEntity() {
        TableEntity entity = new TableEntity(partitionKey, rowKey)
            .addProperty("Product", "Marker Set")
            .addProperty("Price", 5.00)
            .addProperty("Quantity", 21);

        tableClient.createEntity(entity);
    }

    /**
     * Code sample for listing entities.
     */
    public void listEntities() {
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
    }

    /**
     * Code sample for deleting an entity.
     */
    public void deleteEntity() {
        tableClient.deleteEntity(partitionKey, rowKey);
    }

    /**
     * Code sample for accessing information about an error.
     */
    public void accessErrorInfo() {
        // Create the table if it doesn't already exist.
        tableServiceClient.createTableIfNotExists(tableName);

        // Now attempt to create the same table unconditionally.
        try {
            tableServiceClient.createTable(tableName);
        } catch (TableServiceException e) {
            System.out.println(e.getResponse().getStatusCode()); // 409
        }
    }
}
