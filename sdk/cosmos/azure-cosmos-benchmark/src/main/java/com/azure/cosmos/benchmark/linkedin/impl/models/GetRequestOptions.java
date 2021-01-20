// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;


/**
 * RequestOptions specific to GET requests
 */
public class GetRequestOptions {
    public static final GetRequestOptions EMPTY_REQUEST_OPTIONS = new Builder().build();
    /**
     * Unique ID associated with an entity. Will change every time the entity is updated.
     */
    private final String _etag;
    /**
     * Option to fetch a document that has been soft-deleted [default: false]
     */
    private final boolean _fetchTombstone;
    private String _stringRepresentation;

    private GetRequestOptions(final String etag, final boolean fetchTombstone) {
        _etag = etag;
        _fetchTombstone = fetchTombstone;
    }

    public Optional<String> getEtag() {
        return Optional.ofNullable(_etag);
    }

    public boolean shouldFetchTombstone() {
        return _fetchTombstone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetRequestOptions that = (GetRequestOptions) o;
        return Objects.equals(_etag, that._etag) && Objects.equals(_fetchTombstone, that._fetchTombstone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_etag, _fetchTombstone);
    }

    @Override
    public String toString() {
        if (Objects.isNull(_stringRepresentation)) {
            _stringRepresentation = MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("etag", _etag)
                .add("fetchTombstone", _fetchTombstone)
                .toString();
        }

        return _stringRepresentation;
    }

    /**
     * Builder class to ensure an invalid RequestOptions can never be created/passed around
     */
    public static class Builder {
        //  Unique ID associated with an entity.
        private String _etag;

        // Fetch a document that has been soft-deleted
        private boolean _fetchTombstone = false;

        public Builder setEtag(final String etag) {
            Preconditions.checkArgument(StringUtils.isNotBlank(etag), "etag can't be empty or null");
            _etag = etag;
            return this;
        }

        public Builder setFetchTombstone(final boolean fetchTombstone) {
            _fetchTombstone = fetchTombstone;
            return this;
        }

        public GetRequestOptions build() {
            return new GetRequestOptions(_etag, _fetchTombstone);
        }
    }
}
