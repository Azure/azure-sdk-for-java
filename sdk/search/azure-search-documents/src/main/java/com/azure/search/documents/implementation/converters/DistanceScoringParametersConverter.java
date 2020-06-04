// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.DistanceScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters} and
 * {@link DistanceScoringParameters}.
 */
public final class DistanceScoringParametersConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters} to
     * {@link DistanceScoringParameters}.
     */
    public static DistanceScoringParameters map(com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        DistanceScoringParameters distanceScoringParameters = new DistanceScoringParameters();

        String referencePointParameter = obj.getReferencePointParameter();
        distanceScoringParameters.setReferencePointParameter(referencePointParameter);

        double boostingDistance = obj.getBoostingDistance();
        distanceScoringParameters.setBoostingDistance(boostingDistance);
        return distanceScoringParameters;
    }

    /**
     * Maps from {@link DistanceScoringParameters} to
     * {@link com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters}.
     */
    public static com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters map(DistanceScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters distanceScoringParameters =
            new com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters();

        String referencePointParameter = obj.getReferencePointParameter();
        distanceScoringParameters.setReferencePointParameter(referencePointParameter);

        double boostingDistance = obj.getBoostingDistance();
        distanceScoringParameters.setBoostingDistance(boostingDistance);
        return distanceScoringParameters;
    }

    private DistanceScoringParametersConverter() {
    }
}
