package com.azure.cosmos;

public abstract class CosmosItemSerializerNoExceptionWrapping extends CosmosItemSerializer {
    public CosmosItemSerializerNoExceptionWrapping() {
        super(false);
    }
}
