// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

import com.google.common.base.MoreObjects;
import java.util.Objects;


/**
 * Data struct to ensure the unique-ness constraint for id + partitioningKey for
 * any CosmosDBRecord is preserved when we generate the data
 */
public class Key {

    private final String _id;
    private final String _partitioningKey;

    public Key(final String id, final String partitioningKey) {
        _id = id;
        _partitioningKey = partitioningKey;
    }

    public String getId() {
        return _id;
    }

    public String getPartitioningKey() {
        return _partitioningKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Key that = (Key) o;
        return Objects.equals(_id, that._id)
            && Objects.equals(_partitioningKey, that._partitioningKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, _partitioningKey);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", getId())
                .add("partitioningKey", getPartitioningKey())
                .toString();
        }
    }
