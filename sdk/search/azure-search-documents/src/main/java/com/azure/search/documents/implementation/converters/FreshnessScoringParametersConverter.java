// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.FreshnessScoringParameters;

import java.time.Duration;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters} and
 * {@link FreshnessScoringParameters}.
 */
public final class FreshnessScoringParametersConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters} to
     * {@link FreshnessScoringParameters}.
     */
    public static FreshnessScoringParameters map(com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        FreshnessScoringParameters freshnessScoringParameters = new FreshnessScoringParameters();

        Duration boostingDuration = obj.getBoostingDuration();
        freshnessScoringParameters.setBoostingDuration(boostingDuration);
        return freshnessScoringParameters;
    }

    /**
     * Maps from {@link FreshnessScoringParameters} to
     * {@link com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters}.
     */
    public static com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters map(FreshnessScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters freshnessScoringParameters =
            new com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters();

        Duration boostingDuration = obj.getBoostingDuration();
        freshnessScoringParameters.setBoostingDuration(boostingDuration);
        return freshnessScoringParameters;
    }

    private FreshnessScoringParametersConverter() {
    }
}
