package com.azure.data.tables.implementation;

public class TableAsyncClientBuilder {

    String connectionString;


    public TableAsyncClientBuilder connectionString(String connectionString){
        connectionString = connectionString;
        return this;
    }
    public TableAsyncClient build(){
        return new TableAsyncClient();
    }

    public void TableAysncClientBuilder(){

    }
}
