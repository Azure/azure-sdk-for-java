package com.azure.data.tables;

public class TableAsyncServiceClientBuilder {

    String connectionString;


    public TableAsyncServiceClientBuilder connectionString(String connectionString){
        connectionString = connectionString;
        return this;
    }
    public TableAsyncServiceClient build(){
        return new TableAsyncServiceClient();
    }

    public void TableAysncClientBuilder(){

    }
}
