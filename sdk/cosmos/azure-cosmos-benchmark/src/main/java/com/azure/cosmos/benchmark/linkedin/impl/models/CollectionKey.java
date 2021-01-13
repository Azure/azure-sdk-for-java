package com.azure.cosmos.benchmark.linkedin.impl.models;

/**
 * Set of properties to uniquely identify a CosmosDB Collection. A CosmosDB Account can have multiple databases,
 * and each database can have multiple collections
 */
public class CollectionKey {

    /**
     * CosmosDB Account the collection is created in
     */
    private final String _accountName;

    /**
     * The database within the above account
     */
    private final String _databaseName;

    /**
     * The collection name
     */
    private final String _collectionName;

    public CollectionKey(final String accountName, final String databaseName, final String collectionName) {
        _accountName = accountName;
        _databaseName = databaseName;
        _collectionName = collectionName;
    }

    public String getAccountName() {
        return _accountName;
    }

    public String getDatabaseName() {
        return _databaseName;
    }

    public String getCollectionName() {
        return _collectionName;
    }
}
