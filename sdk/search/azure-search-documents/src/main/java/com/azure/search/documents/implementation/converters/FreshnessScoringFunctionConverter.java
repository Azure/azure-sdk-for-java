// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.FreshnessScoringFunction;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction} and
 * {@link FreshnessScoringFunction}.
 */
public final class FreshnessScoringFunctionConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction} to
     * {@link FreshnessScoringFunction}.
     */
    public static FreshnessScoringFunction map(com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction obj) {
        if (obj == null) {
            return null;
        }

        FreshnessScoringFunction freshnessScoringFunction = new FreshnessScoringFunction(obj.getFieldName(),
            obj.getBoost(), obj.getParameters());

        if (obj.getInterpolation() != null) {
            freshnessScoringFunction.setInterpolation(obj.getInterpolation());
        }

        return freshnessScoringFunction;
    }

    /**
     * Maps from {@link FreshnessScoringFunction} to
     * {@link com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction}.
     */
    public static com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction map(FreshnessScoringFunction obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction freshnessScoringFunction =
            new com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction(
                obj.getFieldName(), obj.getBoost(), obj.getParameters());

        if (obj.getInterpolation() != null) {
            freshnessScoringFunction.setInterpolation(obj.getInterpolation());
        }

        return freshnessScoringFunction;
    }

    private FreshnessScoringFunctionConverter() {
    }
}
