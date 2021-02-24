// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;


/**
 * Encapsulates common data source attributes for any Entity
 */
public class EntityAttributes {

    private static final EntityAttributes DEFAULT_ENTITY_ATRIBUTES = new Builder().build();

    /**
     * Entity tag
     */
    private final String _etag;

    /**
     * The TTL in seconds for the entity
     */
    private final Duration _ttl;

    /**
     * The created epoch(unix time) time stamp for the entity in seconds
     */
    private final Long _ts;

    /**
     * Flag indicating if the entity has been soft-deleted/tombstone'd [Default: false]
     */
    private final boolean _isTombstoned;

    private EntityAttributes(final String etag, final Duration ttl, final Long ts, final boolean isTombstoned) {
        _etag = etag;
        _ttl = ttl;
        _ts = ts;
        _isTombstoned = isTombstoned;
    }

    public static EntityAttributes defaultEntityAttributes() {
        return DEFAULT_ENTITY_ATRIBUTES;
    }

    public Optional<String> getEtag() {
        return Optional.ofNullable(_etag);
    }

    public Optional<Duration> getTtl() {
        return Optional.ofNullable(_ttl);
    }

    public Optional<Long> getTs() {
        return Optional.ofNullable(_ts);
    }

    public boolean isTombstoned() {
        return _isTombstoned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityAttributes that = (EntityAttributes) o;
        return Objects.equals(_etag, that._etag) && Objects.equals(_ttl, that._ttl) && Objects.equals(_ts, that._ts)
            && Objects.equals(_isTombstoned, that._isTombstoned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_etag, _ttl, _ts, _isTombstoned);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .omitNullValues()
            .add("_etag", _etag)
            .add("_ttl", _ttl)
            .add("_ts", _ts)
            .add("_isTombstoned", _isTombstoned)
            .toString();
    }

    public static class Builder {
        private String _etag;
        private Duration _ttl;
        private Long _ts;
        private boolean _isTombstoned = false;

        public Builder setEtag(final String etag) {
            Preconditions.checkArgument(StringUtils.isNotEmpty(etag), "etag can't be empty or null");
            _etag = etag;

            return this;
        }

        public Builder setTtl(final Duration ttl) {
            Preconditions.checkNotNull(ttl, "ttl can't be null");
            Preconditions.checkArgument(ttl.toMillis() > 0, "ttl can't be <= 0ms");
            _ttl = ttl;

            return this;
        }

        public Builder setTs(final Long ts) {
            Preconditions.checkNotNull(ts, "ts can't be null");
            Preconditions.checkArgument(ts > 0, "ts can't be <= 0ms");
            _ts = ts;

            return this;
        }

        public Builder setIsTombstoned(final boolean isTombstoned) {
            _isTombstoned = isTombstoned;
            return this;
        }

        /**
         * There are no Preconditions for the EntityAttributes [etag and ttl are optional parameters]
         */
        public EntityAttributes build() {
            return new EntityAttributes(_etag, _ttl, _ts, _isTombstoned);
        }
    }
}
