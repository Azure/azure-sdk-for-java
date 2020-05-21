// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.DistanceScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.DistanceScoringParameters} and
 * {@link DistanceScoringParameters}.
 */
public final class DistanceScoringParametersConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DistanceScoringParametersConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.DistanceScoringParameters} to
     * {@link DistanceScoringParameters}.
     */
    public static DistanceScoringParameters map(com.azure.search.documents.implementation.models.DistanceScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        DistanceScoringParameters distanceScoringParameters = new DistanceScoringParameters();

        String _referencePointParameter = obj.getReferencePointParameter();
        distanceScoringParameters.setReferencePointParameter(_referencePointParameter);

        double _boostingDistance = obj.getBoostingDistance();
        distanceScoringParameters.setBoostingDistance(_boostingDistance);
        return distanceScoringParameters;
    }

    /**
     * Maps from {@link DistanceScoringParameters} to
     * {@link com.azure.search.documents.implementation.models.DistanceScoringParameters}.
     */
    public static com.azure.search.documents.implementation.models.DistanceScoringParameters map(DistanceScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.DistanceScoringParameters distanceScoringParameters =
            new com.azure.search.documents.implementation.models.DistanceScoringParameters();

        String _referencePointParameter = obj.getReferencePointParameter();
        distanceScoringParameters.setReferencePointParameter(_referencePointParameter);

        double _boostingDistance = obj.getBoostingDistance();
        distanceScoringParameters.setBoostingDistance(_boostingDistance);
        return distanceScoringParameters;
    }
}
