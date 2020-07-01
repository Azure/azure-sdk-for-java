// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import java.util.List;

/**
 * sync code snippets for the table service
 */
public class TableServiceClientCodeSnippets {

    private static void methods() {
        ClientLogger logger = new ClientLogger("TableServiceClientCodeSnippets");

        //create a tableServiceClient
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildClient();

        //create TableClient
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .tableName("OfficeSupplies")
            .buildClient();

        //create a table
        try {
            AzureTable officeSuppliesTable = tableServiceClient.createTable("OfficeSupplies");
        } catch (TableServiceErrorException e) {
            logger.error("Create Table Unsuccessful. Error: " + e);
        }

        //delete  table
        try {
            tableServiceClient.deleteTable("OfficeSupplies");
        } catch (TableServiceErrorException e) {
            logger.error("Delete Table Unsuccessful. Error: " + e);
        }

        //query tables
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setFilter("TableName eq OfficeSupplies");

        try {
            List<AzureTable> responseTables = tableServiceClient.queryTables(queryOptions);
        } catch (HttpResponseException e) {
            logger.error("Table Query Unsuccessful. Error: " + e);
        }

        //insert entity
        String row = "crayola markers";
        String partitionKey = "markers";
        TableEntity tableEntity = new TableEntity(row, partitionKey, null);
        try {
            tableEntity = tableClient.insertEntity(tableEntity);
        } catch (HttpResponseException e) {
            logger.error("Insert Entity Unsuccessful. Error: " + e);
        }

        //update entity
        tableEntity.addProperty("Seller", "Crayola");
        try {
            tableClient.updateEntity(tableEntity);
        } catch (HttpResponseException e) {
            logger.error("Update Entity Unsuccessful. Error: " + e);
        }

        //upsert entity (where it is an insert or replace)
        tableEntity.addProperty("Price", "5");
        try {
            tableClient.insertOrReplaceEntity(tableEntity);
        } catch (HttpResponseException e) {
            logger.error("Upsert Entity Unsuccessful. Error: " + e);
        }

        //upsert entity (where it is an insert or merge)
        tableEntity.addProperty("Price", "5");
        try {
            tableClient.insertOrMergeEntity(tableEntity);
        } catch (HttpResponseException e) {
            logger.error("Upsert Entity Unsuccessful. Error: " + e);
        }

        //delete entity
        try {
            tableClient.deleteEntity(tableEntity);
        } catch (HttpResponseException e) {
            logger.error("Delete Entity Unsuccessful. Error: " + e);
        }

        //query a table
        queryOptions.setFilter("Product eq markers");
        queryOptions.setSelect("Seller, Price");
        try {
            List<TableEntity> list = tableClient.queryEntity(queryOptions);
        } catch (HttpResponseException e) {
            logger.error("Query Table Entities Unsuccessful. Error: " + e);
        }
    }
}
