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
        FreshnessScoringFunction freshnessScoringFunction = new FreshnessScoringFunction();

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            freshnessScoringFunction.setInterpolation(interpolation);
        }

        String fieldName = obj.getFieldName();
        freshnessScoringFunction.setFieldName(fieldName);

        double boost = obj.getBoost();
        freshnessScoringFunction.setBoost(boost);

        if (obj.getParameters() != null) {
            FreshnessScoringParameters parameters = FreshnessScoringParametersConverter.map(obj.getParameters());
            freshnessScoringFunction.setParameters(parameters);
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
            new com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction();

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            freshnessScoringFunction.setInterpolation(interpolation);
        }

        String fieldName = obj.getFieldName();
        freshnessScoringFunction.setFieldName(fieldName);

        double boost = obj.getBoost();
        freshnessScoringFunction.setBoost(boost);

        if (obj.getParameters() != null) {
            com.azure.search.documents.indexes.implementation.models.FreshnessScoringParameters parameters =
                FreshnessScoringParametersConverter.map(obj.getParameters());
            freshnessScoringFunction.setParameters(parameters);
        }
        return freshnessScoringFunction;
    }

    private FreshnessScoringFunctionConverter() {
    }
}
