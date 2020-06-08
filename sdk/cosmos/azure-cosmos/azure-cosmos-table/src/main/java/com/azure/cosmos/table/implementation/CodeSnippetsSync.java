package com.azure.cosmos.table.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.cosmos.table.implementation.models.TableServiceErrorException;
import com.azure.cosmos.table.implementation.models.TablesQueryEntitiesResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeSnippetsSync {

    public static void methods(){

        //client-builder pattern
        TableClientBuilder tableClientBuilder = new TableClientBuilder();
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .build();


        //create a table
        try {
            tableClient.createTable("Office Supplies;");
        } catch (TableServiceErrorException e) {
            System.out.println("Create Table Unsuccessful. Error: " + e);
        }

        //delete  table
        try {
            tableClient.deleteTable("Office Supplies");
        } catch (TableServiceErrorException e) {
            System.out.println("Delete Table Unsuccessful. Error: " + e);
        }

        //query tables
        String filterString = "$filter= name eq 'Office Supplies'";
        String selectString = "$select= Product, Price";
        try {
            List<String> responseTables = tableClient.queryTables(selectString, filterString);
        } catch (HttpResponseException e){
            System.out.println("Table Query Unsuccessful. Error: " + e);
        }


        //insert entity
        String tableName = "Office Supplies";
        String row = "crayola markers";
        String partitionKey = "markers";
        HashMap<String, Object> tableEntityProperties = new HashMap<>();
        TableEntity tableEntity = new TableEntity(tableName, row, partitionKey, tableEntityProperties);
        try {
           tableEntity = tableClient.insertEntity(tableEntity);
        } catch (HttpResponseException e){
            System.out.println("Insert Entity Unsuccessful. Error: " + e);
        }


        //update entity
        tableEntity.addProperty("Seller","Crayola");
        try {
            tableClient.updateEntity(tableEntity);
        } catch (HttpResponseException e){
            System.out.println("Update Entity Unsuccessful. Error: " + e);
        }


        //upsert entity
        tableEntity.addProperty("Price","$5");
        try {
            tableEntity = tableClient.upsertEntity(tableEntity);
        } catch (HttpResponseException e){
            System.out.println("Upsert Entity Unsuccessful. Error: " + e);
        }


        //delete entity
        try {
            tableClient.deleteEntity(tableName, tableEntity);
        } catch (HttpResponseException e){
            System.out.println("Delete Entity Unsuccessful. Error: " + e);
        }


        //query a table
        String filterString2 = "$filter = Product eq 'markers'";
        String selectString2 = "$select = Seller eq 'crayola'";
        try {
            List<TableEntity> list= tableClient.queryEntity(tableName, filterString2, selectString2);
        } catch (HttpResponseException e){
            System.out.println("Query Table Entities Unsuccessful. Error: " + e);
        }
    }

}
