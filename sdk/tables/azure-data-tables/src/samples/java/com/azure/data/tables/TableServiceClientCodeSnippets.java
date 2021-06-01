// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableErrorCode;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceException;

import java.util.ArrayList;
import java.util.List;

/**
 * sync code snippets for the Tables service
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
            tableServiceClient.createTable("OfficeSupplies");
        } catch (TableServiceException e) {
            if (e.getValue().getErrorCode() == TableErrorCode.TABLE_ALREADY_EXISTS) {
                System.err.println("Create Table Unsuccessful. Table already exists.");
            } else {
                System.err.println("Create Table Unsuccessful. " + e);
            }
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
        } catch (TableServiceException e) {
            if (e.getValue().getErrorCode() == TableErrorCode.TABLE_NOT_FOUND) {
                System.err.println("Delete Table Unsuccessful. Table not found.");
            } else {
                System.err.println("Delete Table Unsuccessful. Error: " + e);
            }
        }
    }

    /**
     * query table code snippet
     */
    public void listTables() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildClient();

        ListTablesOptions options = new ListTablesOptions().setFilter("TableName eq OfficeSupplies");

        try {
            PagedIterable<TableItem> tablePagedIterable = tableServiceClient.listTables(options, null, null);
        } catch (TableServiceException e) {
            System.err.println("Table Query Unsuccessful. Error: " + e);
        }
    }

    /**
     * insert entity code snippet
     */
    private void createEntity() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        TableEntity entity = new TableEntity("markers", "crayolaMarkers");

        try {
            tableClient.createEntity(entity);
        } catch (TableServiceException e) {
            if (e.getValue().getErrorCode() == TableErrorCode.ENTITY_ALREADY_EXISTS) {
                System.err.println("Create Entity Unsuccessful. Entity already exists.");
            } else {
                System.err.println("Create Entity Unsuccessful. " + e);
            }
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

        String partitionKey = "markers";
        String rowKey = "crayolaMarkers";
        try {
            TableEntity entity = tableClient.getEntity(partitionKey, rowKey);

            // Setting ifUnchanged to true means the eTags from the entity must match that of the entity in the Table
            // service to delete successfully.
            tableClient.deleteEntity(entity);
        } catch (TableServiceException e) {
            if (e.getValue().getErrorCode() == TableErrorCode.ENTITY_NOT_FOUND) {
                System.err.println("Delete Entity Unsuccessful. Entity not found.");
            } else {
                System.err.println("Delete Entity Unsuccessful. Error: " + e);
            }
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

        String partitionKey = "markers";
        String rowKey = "crayolaMarkers";
        try {
            TableEntity entity = tableClient.getEntity(partitionKey, rowKey);

            // The default TableEntityUpdateMode for this operation is MERGE, which means it merges the entities if one
            // already exists or inserts it if it does not.
            tableClient.updateEntity(entity);
        } catch (TableServiceException e) {
            if (e.getValue().getErrorCode() == TableErrorCode.ENTITY_NOT_FOUND) {
                System.err.println("Cannot find entity. Update unsuccessful");
            } else {
                System.err.println("Update Entity Unsuccessful. Error: " + e);
            }
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

        String partitionKey = "markers";
        String rowKey = "crayolaMarkers";
        try {
            TableEntity entity = tableClient.getEntity(partitionKey, rowKey);

            // The default TableEntityUpdateMode is REPLACE, which means the entity will be replaced if it exists or
            // the request fails if not found.
            tableClient.upsertEntity(entity);
        } catch (TableServiceException e) {
            if (e.getValue().getErrorCode() == TableErrorCode.ENTITY_NOT_FOUND) {
                System.err.println("Cannot find entity. Upsert unsuccessful");
            } else {
                System.err.println("Upsert Entity Unsuccessful. Error: " + e);
            }
        }
    }

    /**
     * query entity code snippet
     */
    private void listEntity() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        List<String> propertiesToSelect = new ArrayList<>();
        propertiesToSelect.add("Seller");
        propertiesToSelect.add("Price");

        ListEntitiesOptions options = new ListEntitiesOptions()
            .setFilter("Product eq markers")
            .setSelect(propertiesToSelect);
        try {
            PagedIterable<TableEntity> tableEntities = tableClient.listEntities(options, null, null);
        } catch (TableServiceException e) {
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

        String partitionKey = "markers";
        String rowKey = "crayolaMarkers";
        try {
            TableEntity entity = tableClient.getEntity(partitionKey, rowKey);
        } catch (TableServiceException e) {
            System.err.println("Get Entity Unsuccessful. Entity may not exist: " + e);
        }
    }
}
