// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sync code snippets for the table service
 */
public class TableServiceClientCodeSnippets {

    /**
     * all the functionality of the Tables SDK written sync
     */
    public static void methods() {
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
        Map<String, Object> properties = new HashMap<>();
        properties.put("RowKey", "crayolaMarkers");
        properties.put("PartitionKey", "markers");
        TableEntity tableEntity = null;
        try {
            tableEntity = tableClient.createEntity(properties);
        } catch (HttpResponseException e) {
            logger.error("Insert Entity Unsuccessful. Error: " + e);
        }

        //update entity
        tableEntity.addProperty("Seller", "Crayola");
        try {
            tableClient.updateEntity(UpdateMode.Replace, tableEntity);
        } catch (HttpResponseException e) {
            logger.error("Update Entity Unsuccessful. Error: " + e);
        }

        //upsert entity
        tableEntity.addProperty("Price", "5");
        try {
            tableClient.upsertEntity(UpdateMode.Replace, tableEntity);
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
