// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;


/**
 * The result object that contains
 * 1. The result of the query
 * 2. Metadata about the result
 */
public class Result<K, V> {
    private final K _key;
    private final Entity<V> _result;
    private final ResultMetadata _metadata;

    private Result(final K key, final Entity<V> result, final ResultMetadata metadata) {
        _key = key;
        _result = result;
        _metadata = metadata;
    }

    public K getKey() {
        return _key;
    }

    public Optional<Entity<V>> getResult() {
        return Optional.ofNullable(_result);
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

        Result that = (Result) o;
        return Objects.equals(_key, that._key) && Objects.equals(_result, that._result) && Objects.equals(_metadata,
            that._metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_key, _result, _metadata);
    }

    /**
     * Builder class to ensure an invalid Result instance can never be created/passed around
     */
    public static class Builder<K, V> {
        private K _key;
        private Entity<V> _result;
        private ResultMetadata _metadata;

        public Builder<K, V> setResult(final K key, final Entity<V> result) {
            Preconditions.checkNotNull(key, "key cannot be null");

            _key = key;
            _result = result;
            return this;
        }

        public Builder<K, V> setMetadata(final ResultMetadata metadata) {
            Preconditions.checkNotNull(metadata, "The metadata cannot be null");
            _metadata = metadata;

            return this;
        }

        public Result<K, V> build() {
            Preconditions.checkState(Objects.nonNull(_key), "A Result instance can not be created with a null key. "
                + "The key must be set, but the value/Entity can be null");
            Preconditions.checkState(Objects.nonNull(_metadata),
                "A Result instance can not be created without the result metadata.");
            return new Result<>(_key, _result, _metadata);
        }
    }
}
