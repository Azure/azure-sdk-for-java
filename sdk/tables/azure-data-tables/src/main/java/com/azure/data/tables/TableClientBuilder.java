package com.azure.data.tables;

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
