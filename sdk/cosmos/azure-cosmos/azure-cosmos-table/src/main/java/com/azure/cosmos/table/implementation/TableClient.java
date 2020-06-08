package com.azure.cosmos.table.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableClient {
    String name;


    public AzureTable createTable(String name){
        return new AzureTable(name);
    }


    public TableClient(){
        name = "Hi";
    }

    public String getName() {
        return name;
    }

    public AzureTable createTableIfNotExist(String name) {
        return new AzureTable(name);
    }
    public void deleteTable(AzureTable az) {

    }
    public void deleteTable(String name) {

    }
    public List<String> queryTables(String selectString, String filterString){
        return null;
    }
    public List<TableEntity> queryEntity(String az, String selectString, String filterString){
        return null;
    }

    public TableEntity insertEntity(String tableName, String row, String partition, Map<String, Object> tableEntityProperties){
        return new TableEntity();
    }
    public TableEntity insertEntity(TableEntity te){
        return te;
    }
    public void deleteEntity(String tableName, TableEntity tableEntity){

    }
    public void updateEntity(TableEntity te){

    }
    public TableEntity upsertEntity(TableEntity te){
        return new TableEntity();
    }
}
