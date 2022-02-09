// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data.entity;

import com.azure.cosmos.benchmark.linkedin.data.CollectionAttributes;
import com.azure.cosmos.benchmark.linkedin.impl.Constants;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.google.common.collect.ImmutableList;


/**
 * Encapsulates the Indexing policy specific to the Invitations collections
 */
public class InvitationsCollectionAttributes implements CollectionAttributes {
    private static final IncludedPath INCLUDED_PATH = new IncludedPath(Constants.PARTITIONING_KEY_INDEXING_INCLUDE_PATH);
    private static final ExcludedPath EXCLUDED_PATH = new ExcludedPath(Constants.WILDCARD_INDEXING_EXCLUDE_PATH);

    private final IndexingPolicy _indexingPolicy;

    public InvitationsCollectionAttributes() {
        _indexingPolicy = new IndexingPolicy()
            .setAutomatic(true)
            .setIncludedPaths(ImmutableList.of(INCLUDED_PATH))
            .setExcludedPaths(ImmutableList.of(EXCLUDED_PATH))
            .setIndexingMode(IndexingMode.CONSISTENT);
    }

    @Override
    public IndexingPolicy indexingPolicy() {
        return _indexingPolicy;
    }
}
