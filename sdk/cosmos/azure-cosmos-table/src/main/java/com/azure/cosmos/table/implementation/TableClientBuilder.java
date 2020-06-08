package com.azure.cosmos.table.implementation;

public class TableClientBuilder {
    String connectionString;


    public TableClientBuilder connectionString(String connectionString){
        connectionString = connectionString;
        return this;
    }
    public TableClient build(){
        return new TableClient();
    }

    public TableClientBuilder(){

    }

}
