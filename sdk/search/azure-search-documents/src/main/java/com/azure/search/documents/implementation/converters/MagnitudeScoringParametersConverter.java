// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.MagnitudeScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.MagnitudeScoringParameters} and
 * {@link MagnitudeScoringParameters}.
 */
public final class MagnitudeScoringParametersConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MagnitudeScoringParametersConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.MagnitudeScoringParameters} to
     * {@link MagnitudeScoringParameters}.
     */
    public static MagnitudeScoringParameters map(com.azure.search.documents.implementation.models.MagnitudeScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        MagnitudeScoringParameters magnitudeScoringParameters = new MagnitudeScoringParameters();

        double _boostingRangeStart = obj.getBoostingRangeStart();
        magnitudeScoringParameters.setBoostingRangeStart(_boostingRangeStart);

        double _boostingRangeEnd = obj.getBoostingRangeEnd();
        magnitudeScoringParameters.setBoostingRangeEnd(_boostingRangeEnd);

        Boolean _shouldBoostBeyondRangeByConstant = obj.isShouldBoostBeyondRangeByConstant();
        magnitudeScoringParameters.setShouldBoostBeyondRangeByConstant(_shouldBoostBeyondRangeByConstant);
        return magnitudeScoringParameters;
    }

    /**
     * Maps from {@link MagnitudeScoringParameters} to
     * {@link com.azure.search.documents.implementation.models.MagnitudeScoringParameters}.
     */
    public static com.azure.search.documents.implementation.models.MagnitudeScoringParameters map(MagnitudeScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.MagnitudeScoringParameters magnitudeScoringParameters =
            new com.azure.search.documents.implementation.models.MagnitudeScoringParameters();

        double _boostingRangeStart = obj.getBoostingRangeStart();
        magnitudeScoringParameters.setBoostingRangeStart(_boostingRangeStart);

        double _boostingRangeEnd = obj.getBoostingRangeEnd();
        magnitudeScoringParameters.setBoostingRangeEnd(_boostingRangeEnd);

        Boolean _shouldBoostBeyondRangeByConstant = obj.shouldBoostBeyondRangeByConstant();
        magnitudeScoringParameters.setShouldBoostBeyondRangeByConstant(_shouldBoostBeyondRangeByConstant);
        return magnitudeScoringParameters;
    }
}
