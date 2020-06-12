package com.azure.data.tables;

import com.azure.core.exception.HttpResponseException;
import com.azure.data.tables.implementation.models.TableServiceErrorException;

import java.util.HashMap;
import java.util.List;

public class CodeSnippetsSync {

    public static void methods(){

        //client-builder pattern
        TableClientBuilder tableClientBuilder = new TableClientBuilder();
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionString")
            .build();


        //create a table
        try {
            tableClient.createTable("OfficeSupplies");
        } catch (TableServiceErrorException e) {
            //use azure core errors? based on
            System.out.println("Create Table Unsuccessful. Error: " + e);
        }

        //delete  table
        try {
            //equivilant to pass in string var vs string so each person can choose
            tableClient.deleteTable("OfficeSupplies");
        } catch (TableServiceErrorException e) {
            System.out.println("Delete Table Unsuccessful. Error: " + e);
        }

        //query tables
        String filterString = "$filter= name eq 'OfficeSupplies'";
        //TODO: remove selectString since it is not queried with tables
        String selectString = "$select= Product, Price";
        try {
            //TODO: create Table class TableName is the odata feild
            List<String> responseTables = tableClient.queryTables(selectString, filterString);
        } catch (HttpResponseException e){
            System.out.println("Table Query Unsuccessful. Error: " + e);
        }


        //insert entity
        String tableName = "OfficeSupplies";
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
        //TODO: let the customer specify if it is a replacement or a merge?
        tableEntity.addProperty("Seller","Crayola");
        try {
            tableClient.updateEntity(tableEntity);
        } catch (HttpResponseException e){
            System.out.println("Update Entity Unsuccessful. Error: " + e);
        }


        //upsert entity
        //TODO: which upsert do we want to reflect? Replace or merge? Update vs Merge as well
        tableEntity.addProperty("Price","$5");
        try {
            tableEntity = tableClient.upsertEntity(tableEntity);
        } catch (HttpResponseException e){
            System.out.println("Upsert Entity Unsuccessful. Error: " + e);
        }


        //delete entity
        try {
            tableClient.deleteEntity(tableEntity);
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
