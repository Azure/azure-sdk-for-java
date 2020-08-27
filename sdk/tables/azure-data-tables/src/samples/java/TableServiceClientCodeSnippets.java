// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.models.Entity;
import com.azure.data.tables.models.QueryParams;
import com.azure.data.tables.models.Table;
import com.azure.data.tables.models.TableErrorCode;
import com.azure.data.tables.models.TableStorageException;
import com.azure.data.tables.models.UpdateMode;

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
            Table officeSuppliesTable = tableServiceClient.createTable("OfficeSupplies");
        } catch (TableStorageException e) {
            if (e.getErrorCode() == TableErrorCode.TABLE_ALREADY_EXISTS) {
                System.err.println("Create Table Unsuccessful. Table already exists.");
            } else if (e.getErrorCode() == TableErrorCode.INVALID_TABLE_NAME) {
                System.err.println("Create Table Unsuccessful. Table name invalid");
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
        } catch (TableStorageException e) {
            if (e.getErrorCode() == TableErrorCode.TABLE_NOT_FOUND) {
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

        QueryParams queryParams = new QueryParams().setFilter("TableName eq OfficeSupplies");

        try {
            PagedIterable<Table> tablePagedIterable = tableServiceClient.listTables(queryParams);
        } catch (TableStorageException e) {
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

        Entity entity = new Entity("markers", "crayolaMarkers");

        try {
            entity = tableClient.createEntity(entity);
        } catch (TableStorageException e) {
            if (e.getErrorCode() == TableErrorCode.ENTITY_ALREADY_EXISTS) {
                System.err.println("Create Entity Unsuccessful. Entity already exists.");
            } else if (e.getErrorCode() == TableErrorCode.INVALID_PK_OR_RK_NAME) {
                System.err.println("Create Table Unsuccessful. Row key or Partition key is invalid.");
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
            Entity entity = tableClient.getEntity(partitionKey, rowKey);

            //ifUnchanged being true means the eTags must match to delete
            tableClient.deleteEntity(entity, true);
        } catch (TableStorageException e) {
            if (e.getErrorCode() == TableErrorCode.ENTITY_NOT_FOUND) {
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
            Entity entity = tableClient.getEntity(partitionKey, rowKey);

            //default is for UpdateMode is UpdateMode.MERGE, which means it merges if exists; fails if not
            //ifUnchanged being false means the eTags must not match
            tableClient.updateEntity(entity);
        } catch (TableStorageException e) {
            if (e.getErrorCode() == TableErrorCode.ENTITY_NOT_FOUND) {
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
            Entity entity = tableClient.getEntity(partitionKey, rowKey);

            //default is for UpdateMode is UpdateMode.REPLACE, which means it replaces if exists; inserts if not
            //always upsert because if no ifUnchanged boolean present the "*" in request.
            tableClient.upsertEntity(entity, UpdateMode.REPLACE);
        } catch (TableStorageException e) {
            if (e.getErrorCode() == TableErrorCode.ENTITY_NOT_FOUND) {
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

        QueryParams queryParams = new QueryParams()
            .setFilter("Product eq markers")
            .setSelect("Seller, Price");
        try {
            PagedIterable<Entity> tableEntities = tableClient.listEntities(queryParams);
        } catch (TableStorageException e) {
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
            Entity entity = tableClient.getEntity(partitionKey, rowKey);
        } catch (TableStorageException e) {
            System.err.println("Get Entity Unsuccessful. Entity may not exist: " + e);
        }
    }
}
