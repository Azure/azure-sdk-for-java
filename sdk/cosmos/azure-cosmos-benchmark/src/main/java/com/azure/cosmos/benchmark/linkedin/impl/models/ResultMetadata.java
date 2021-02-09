// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;


/**
 * Metadata about the datastore operation, like the cost units corresponding to the operation
 */
public class ResultMetadata {
    private static final ResultMetadata DEFAULT_RESULT_METADATA = new Builder().build();

    private final double _costUnits;
    private final String _continuationToken;

    private ResultMetadata(double costUnits, String continuationToken) {
        _costUnits = costUnits;
        _continuationToken = continuationToken;
    }

    public static ResultMetadata defaultResultMetadata() {
        return DEFAULT_RESULT_METADATA;
    }

    public double getCostUnits() {
        return _costUnits;
    }

    public Optional<String> getContinuationToken() {
        return Optional.ofNullable(_continuationToken);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResultMetadata that = (ResultMetadata) o;
        return Objects.equals(_costUnits, that._costUnits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_costUnits);
    }

    /**
     * Builder class to ensure an invalid ResultMetadata can never be created/passed around
     */
    public static class Builder {
        private double _costUnits = 0.0f;
        private String _continuationToken;

        public Builder setCostUnits(double costUnits) {
            Preconditions.checkArgument(costUnits >= 0, "costUnits can't be < 0");
            _costUnits = costUnits;
            return this;
        }

        public Builder setContinuationToken(String continuationToken) {
            _continuationToken = Preconditions.checkNotNull(continuationToken, "continuation cannot be null");
            return this;
        }

        public Builder addCostUnits(double costUnits) {
            Preconditions.checkArgument(costUnits >= 0, "costUnits can't be < 0");
            _costUnits += costUnits;
            return this;
        }

        public Builder combine(final ResultMetadata toMerge) {
            Preconditions.checkNotNull(toMerge, "The ResultMetadata to merge can not be null");
            _costUnits += toMerge._costUnits;
            return this;
        }

        public ResultMetadata build() {
            return new ResultMetadata(_costUnits, _continuationToken);
        }
    }
}
