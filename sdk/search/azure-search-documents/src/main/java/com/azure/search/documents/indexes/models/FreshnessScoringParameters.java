// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

/**
 * Provides parameter values to a freshness scoring function.
 */
@Fluent
public final class FreshnessScoringParameters {
    /*
     * The expiration period after which boosting will stop for a particular
     * document.
     */
    @JsonProperty(value = "boostingDuration", required = true)
    private Duration boostingDuration;

    /**
     * Constructor of {@link FreshnessScoringParameters}.
     *
     * @param boostingDuration The expiration period after which boosting will stop for a particular document.
     */
    @JsonCreator
    public FreshnessScoringParameters(
        @JsonProperty(value = "boostingDuration", required = true) Duration boostingDuration) {
        this.boostingDuration = boostingDuration;
    }

    /**
     * Get the boostingDuration property: The expiration period after which
     * boosting will stop for a particular document.
     *
     * @return the boostingDuration value.
     */
    public Duration getBoostingDuration() {
        return this.boostingDuration;
    }
}
