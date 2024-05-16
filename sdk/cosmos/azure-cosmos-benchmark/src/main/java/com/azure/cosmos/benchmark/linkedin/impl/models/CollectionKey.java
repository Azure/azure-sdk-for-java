// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.google.common.base.MoreObjects;
import java.util.Objects;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionKey that = (CollectionKey) o;
        return Objects.equals(_accountName, that._accountName)
            && Objects.equals(_databaseName, that._databaseName)
            && Objects.equals(_collectionName, that._collectionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_accountName, _databaseName, _collectionName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .omitNullValues()
            .add("Database", getDatabaseName())
            .add("Collection", getCollectionName())
            .toString();
    }
}
