// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sync code snippets for the table service
 */
public class TableServiceClientCodeSnippets {
    private final ClientLogger logger = new ClientLogger("TableServiceClientCodeSnippets");

    /**
     * create table code snippet
     */
    public void createTable() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildClient();

        try {
            AzureTable officeSuppliesTable = tableServiceClient.createTable("OfficeSupplies");
        } catch (Exception e) {
            System.err.println("Create Table Unsuccessful. Error: " + e);
        }
    }

    /**
     * delete table code snippet
     */
    public void deleteTable() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildClient();

        try {
            tableServiceClient.deleteTable("OfficeSupplies");
        } catch (Exception e) {
            System.err.println("Delete Table Unsuccessful. Error: " + e);
        }
    }

    /**
     * query table code snippet
     */
    public void queryTables() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildClient();

        QueryOptions queryOptions = new QueryOptions().setFilter("TableName eq OfficeSupplies");

        try {
            List<AzureTable> responseTables = tableServiceClient.queryTables(queryOptions);
        } catch (HttpResponseException e) {
            System.err.println("Table Query Unsuccessful. Error: " + e);
        }
    }

    /**
     * insert entity code snippet
     */
    private void insertEntity() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        Map<String, Object> properties = new HashMap<>();
        properties.put("RowKey", "crayolaMarkers");
        properties.put("PartitionKey", "markers");
        try {
            TableEntity tableEntity = tableClient.createEntity(properties);
        } catch (HttpResponseException e) {
            System.err.println("Insert Entity Unsuccessful. Error: " + e);
        }
    }

    /**
     * update entity code snippet
     */
    private void updateEntity() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        String rowKey = "crayolaMarkers";
        String partitionKey = "markers";
        TableEntity tableEntity = null;
        try {
            tableEntity = tableClient.get(rowKey, partitionKey);
        } catch (HttpResponseException e) {
            System.err.println("Get Entity Unsuccessful: " + e);
        }
        try {
            tableClient.updateEntity(UpdateMode.REPLACE, tableEntity);
        } catch (HttpResponseException e) {
            System.err.println("Update Entity Unsuccessful. Error: " + e);
        }
    }

    /**
     * upsert entity code snippet
     */
    private void upsertEntity() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        String rowKey = "crayolaMarkers";
        String partitionKey = "markers";
        TableEntity tableEntity = null;
        try {
            tableEntity = tableClient.get(rowKey, partitionKey);
        } catch (HttpResponseException e) {
            System.err.println("Get Entity Unsuccessful: " + e);
        }
        try {
            tableClient.upsertEntity(UpdateMode.REPLACE, tableEntity);
        } catch (HttpResponseException e) {
            System.err.println("Upsert Entity Unsuccessful. Error: " + e);
        }
    }

    /**
     * delete entity code snippet
     */
    private void deleteEntity() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        String rowKey = "crayolaMarkers";
        String partitionKey = "markers";
        TableEntity tableEntity = null;
        try {
            tableEntity = tableClient.get(rowKey, partitionKey);
        } catch (HttpResponseException e) {
            System.err.println("Get Entity Unsuccessful: " + e);
        }
        try {
            tableClient.deleteEntity(tableEntity);
        } catch (HttpResponseException e) {
            System.err.println("Delete Entity Unsuccessful. Error: " + e);
        }
    }

    /**
     * query entity code snippet
     */
    private void queryEntity() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        QueryOptions queryOptions = new QueryOptions()
            .setFilter("Product eq markers")
            .setSelect("Seller, Price");
        try {
            List<TableEntity> tableEntities = tableClient.queryEntities(queryOptions);
        } catch (HttpResponseException e) {
            System.err.println("Query Table Entities Unsuccessful. Error: " + e);
        }
    }

    /**
     * check to see if a table entity exists
     */
    public void entityExists() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        String rowKey = "crayolaMarkers";
        String partitionKey = "markers";
        try {
            TableEntity tableEntity = tableClient.get(rowKey, partitionKey);
        } catch (HttpResponseException e) {
            System.err.println("Get Entity Unsuccessful. Entity may not exist: " + e);
        }
    }
}
