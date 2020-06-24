// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.exception.HttpResponseException;
import com.azure.data.tables.implementation.models.TableServiceErrorException;

import java.util.HashMap;
import java.util.List;

public class TableClientCodeSnippets {

    public static void methods() {

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
            tableServiceClient.createTable("OfficeSupplies");
        } catch (TableServiceErrorException e) {
            //use azure core errors? based on
            System.out.println("Create Table Unsuccessful. Error: " + e);
        }

        //delete  table
        try {
            tableServiceClient.deleteTable("OfficeSupplies");
        } catch (TableServiceErrorException e) {
            System.out.println("Delete Table Unsuccessful. Error: " + e);
        }

        //query tables
        String selectString = "$selectString= TableName eq 'OfficeSupplies'";

        try {
            List<AzureTable> responseTables = tableServiceClient.queryTables(selectString);
        } catch (HttpResponseException e) {
            System.out.println("Table Query Unsuccessful. Error: " + e);
        }


        //insert entity
        String row = "crayola markers";
        String partitionKey = "markers";
        HashMap<String, Object> tableEntityProperties = new HashMap<>();
        tableEntityProperties.put("RowKey", "crayolaMarkers");
        tableEntityProperties.put("ParitionKey", "markers");
        TableEntity tableEntity = new TableEntity(tableEntityProperties);
        try {
            tableEntity = tableClient.insertEntity(tableEntity);
        } catch (HttpResponseException e) {
            System.out.println("Insert Entity Unsuccessful. Error: " + e);
        }


        //update entity
        tableEntity.addProperty("Seller", "Crayola");
        try {
            tableClient.updateEntity(tableEntity);
        } catch (HttpResponseException e) {
            System.out.println("Update Entity Unsuccessful. Error: " + e);
        }


        //upsert entity (where it is an insert or replace)
        tableEntity.addProperty("Price", "5");
        try {
            tableClient.insertOrReplaceEntity(tableEntity);
        } catch (HttpResponseException e) {
            System.out.println("Upsert Entity Unsuccessful. Error: " + e);
        }

        //upsert entity (where it is an insert or merge)
        tableEntity.addProperty("Price", "5");
        try {
            tableClient.insertOrMergeEntity(tableEntity);
        } catch (HttpResponseException e) {
            System.out.println("Upsert Entity Unsuccessful. Error: " + e);
        }


        //delete entity
        try {
            tableClient.deleteEntity(tableEntity);
        } catch (HttpResponseException e) {
            System.out.println("Delete Entity Unsuccessful. Error: " + e);
        }


        //query a table
        String filterString2 = "$filter = Product eq 'markers'";
        String selectString2 = "$select = Seller, Price";
        try {
            List<TableEntity> list = tableClient.queryEntity(null, filterString2, selectString2);
        } catch (HttpResponseException e) {
            System.out.println("Query Table Entities Unsuccessful. Error: " + e);
        }
    }

}
