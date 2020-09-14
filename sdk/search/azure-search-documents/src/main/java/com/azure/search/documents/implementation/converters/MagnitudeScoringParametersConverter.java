// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters} and
 * {@link MagnitudeScoringParameters}.
 */
public final class MagnitudeScoringParametersConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters} to
     * {@link MagnitudeScoringParameters}.
     */
    public static MagnitudeScoringParameters map(com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        MagnitudeScoringParameters magnitudeScoringParameters = new MagnitudeScoringParameters(
            obj.getBoostingRangeStart(), obj.getBoostingRangeEnd());

        Boolean shouldBoostBeyondRangeByConstant = obj.isShouldBoostBeyondRangeByConstant();
        magnitudeScoringParameters.setShouldBoostBeyondRangeByConstant(shouldBoostBeyondRangeByConstant);
        return magnitudeScoringParameters;
    }

    /**
     * Maps from {@link MagnitudeScoringParameters} to
     * {@link com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters map(MagnitudeScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters magnitudeScoringParameters =
            new com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters(
                obj.getBoostingRangeStart(), obj.getBoostingRangeEnd());

        Boolean shouldBoostBeyondRangeByConstant = obj.shouldBoostBeyondRangeByConstant();
        magnitudeScoringParameters.setShouldBoostBeyondRangeByConstant(shouldBoostBeyondRangeByConstant);

        return magnitudeScoringParameters;
    }

    private MagnitudeScoringParametersConverter() {
    }
}
