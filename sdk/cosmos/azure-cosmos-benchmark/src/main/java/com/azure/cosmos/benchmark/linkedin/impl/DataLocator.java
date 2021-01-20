// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;


/**
 * Interface for locating data stored in Cosmos/DocumentDB. It facilitates switching the source of the
 * data without clients having to know how the accounts/databases/collections etc are managed.
 */
public interface DataLocator {
    /**
     * @return the Collection information where data is stored for that entity
     */
    CollectionKey getCollection();

    /**
     *
     * @param collectionKey used to construct the CosmosAsyncContainer
     @return A CosmosAsyncContainer instance that facilitates accessing data for that entity from CosmosDB.
     */
    CosmosAsyncContainer getAsyncContainer(CollectionKey collectionKey);
}
