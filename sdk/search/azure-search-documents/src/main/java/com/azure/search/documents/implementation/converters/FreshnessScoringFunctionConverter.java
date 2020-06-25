// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.FreshnessScoringFunction;
import com.azure.search.documents.indexes.models.FreshnessScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;

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

        FreshnessScoringParameters parameters = FreshnessScoringParametersConverter.map(obj.getParameters());
        FreshnessScoringFunction freshnessScoringFunction = new FreshnessScoringFunction(obj.getFieldName(),
            obj.getBoost(), parameters);

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            freshnessScoringFunction.setInterpolation(interpolation);
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

        com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters parameters =
            FreshnessScoringParametersConverter.map(obj.getParameters());
        com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction freshnessScoringFunction =
            new com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction(obj.getBoost(),
                obj.getFieldName(), parameters);

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            freshnessScoringFunction.setInterpolation(interpolation);
        }

        freshnessScoringFunction.validate();
        return freshnessScoringFunction;
    }

    private FreshnessScoringFunctionConverter() {
    }
}
