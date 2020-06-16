package com.azure.data.tables;

public class TableServiceAsyncClientBuilder {

    String connectionString;


    public TableServiceAsyncClientBuilder connectionString(String connectionString){
        connectionString = connectionString;
        return this;
    }
    public TableServiceAsyncClient build(){
        return new TableServiceAsyncClient();
    }

    public void TableAysncClientBuilder(){

    }
}
