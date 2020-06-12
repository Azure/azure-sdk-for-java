package com.azure.data.tables;

public class TableClientBuilder {
    String connectionString;
    String tableName;


    public TableClientBuilder connectionString(String connectionString){
        this.connectionString = connectionString;
        return this;
    }
    public TableClientBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public TableClient build(){
        return new TableClient(tableName);
    }

    public TableClientBuilder(){

    }

}
