// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.DistanceScoringFunction;
import com.azure.search.documents.indexes.models.DistanceScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction} and
 * {@link DistanceScoringFunction}.
 */
public final class DistanceScoringFunctionConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction} to
     * {@link DistanceScoringFunction}.
     */
    public static DistanceScoringFunction map(com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        DistanceScoringFunction distanceScoringFunction = new DistanceScoringFunction();

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            distanceScoringFunction.setInterpolation(interpolation);
        }

        String fieldName = obj.getFieldName();
        distanceScoringFunction.setFieldName(fieldName);

        double boost = obj.getBoost();
        distanceScoringFunction.setBoost(boost);

        if (obj.getParameters() != null) {
            DistanceScoringParameters parameters = DistanceScoringParametersConverter.map(obj.getParameters());
            distanceScoringFunction.setParameters(parameters);
        }
        return distanceScoringFunction;
    }

    /**
     * Maps from {@link DistanceScoringFunction} to
     * {@link com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction}.
     */
    public static com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction map(DistanceScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction distanceScoringFunction =
            new com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction();

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            distanceScoringFunction.setInterpolation(interpolation);
        }

        String fieldName = obj.getFieldName();
        distanceScoringFunction.setFieldName(fieldName);

        double boost = obj.getBoost();
        distanceScoringFunction.setBoost(boost);

        if (obj.getParameters() != null) {
            com.azure.search.documents.indexes.implementation.models.DistanceScoringParameters parameters =
                DistanceScoringParametersConverter.map(obj.getParameters());
            distanceScoringFunction.setParameters(parameters);
        }
        return distanceScoringFunction;
    }

    private DistanceScoringFunctionConverter() {
    }
}
