// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


/**
 * The result object that contains
 *  1. The results of the query
 *  2. Metadata about the result
 */
public class BatchGetResult<K, V> {
    private final Map<K, Entity<V>> _results;
    private final ResultMetadata _metadata;

    private BatchGetResult(final Map<K, Entity<V>> results, final ResultMetadata metadata) {
        _results = results;
        _metadata = metadata;
    }

    public Map<K, Entity<V>> getResults() {
        return _results;
    }

    public ResultMetadata getMetadata() {
        return _metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BatchGetResult that = (BatchGetResult) o;
        return Objects.equals(_results, that._results) && Objects.equals(_metadata, that._metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_results, _metadata);
    }

    /**
     * Builder class to ensure an invalid batchGetResult instance can never be created/passed around
     */
    public static class Builder<K, V> {
        private Map<K, Entity<V>> _results = new LinkedHashMap<>();

        private ResultMetadata _metadata;

        public Builder<K, V> addResult(final K key, Entity<V> result) {
            Preconditions.checkNotNull(key, "key cannot be null");

            _results.put(key, result);
            return this;
        }

        public Builder<K, V> setMetadata(final ResultMetadata metadata) {
            Preconditions.checkNotNull(metadata, "The metadata cannot be null");
            _metadata = metadata;

            return this;
        }

        public BatchGetResult<K, V> build() {
            Preconditions.checkState(Objects.nonNull(_metadata),
                "A Result instance can not be created without the result metadata.");
            return new BatchGetResult<>(_results, _metadata);
        }
    }
}
