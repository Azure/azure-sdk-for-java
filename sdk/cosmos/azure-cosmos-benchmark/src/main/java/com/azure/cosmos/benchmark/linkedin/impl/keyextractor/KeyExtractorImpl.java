// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.keyextractor;

import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import javax.annotation.concurrent.ThreadSafe;


/**
 * Extractor for cases where the key is modeling using the Key class
 */
@ThreadSafe
public class KeyExtractorImpl implements KeyExtractor<Key> {

    public KeyExtractorImpl() {
    }

    @Override
    public String getId(final Key key) {
        Preconditions.checkNotNull(key, "The key can't be null");
        return key.getId();
    }

    @Override
    public String getPartitioningKey(final Key key) {
        Preconditions.checkNotNull(key, "The key can't be null");
        return key.getPartitioningKey();
    }

    @Override
    public Key getKey(final ObjectNode document) {
        Preconditions.checkNotNull(document, "The Document from CosmosDB can't be null");
        return getKey(getId(document), getPartitioningKey(document));
    }

    @Override
    public Key getKey(String id, String partitioningKey) {
        Preconditions.checkNotNull(id, "The id from CosmosDB can't be null");
        Preconditions.checkNotNull(partitioningKey, "The partitioningKey from CosmosDB can't be null");
        return new Key(id, partitioningKey);
    }
}
