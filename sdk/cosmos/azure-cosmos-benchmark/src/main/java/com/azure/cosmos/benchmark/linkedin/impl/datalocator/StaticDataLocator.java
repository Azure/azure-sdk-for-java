// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.datalocator;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.benchmark.linkedin.impl.DataLocator;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.google.common.base.Preconditions;


public class StaticDataLocator implements DataLocator {

    /**
     * Information regarding the collection
     */
    private final CollectionKey _collectionKey;

    /**
     * Reference to the CosmosAsyncContainer for this CollectionKey
     */
    private final CosmosAsyncContainer _cosmosAsyncContainer;

    public StaticDataLocator(final CollectionKey collectionKey, final CosmosAsyncContainer cosmosAsyncContainer) {
        _collectionKey = Preconditions.checkNotNull(collectionKey, "The DataSource collectionKey is null!");
        _cosmosAsyncContainer = Preconditions.checkNotNull(cosmosAsyncContainer, "The CosmosAsyncContainer is null!");
    }

    @Override
    public CollectionKey getCollection() {
        return _collectionKey;
    }

    @Override
    public CosmosAsyncContainer getAsyncContainer(CollectionKey collectionKey) {
        return _cosmosAsyncContainer;
    }
}
