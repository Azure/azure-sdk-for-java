// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.DistanceScoringFunction;
import com.azure.search.documents.models.DistanceScoringParameters;
import com.azure.search.documents.models.ScoringFunctionInterpolation;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.DistanceScoringFunction} and
 * {@link DistanceScoringFunction}.
 */
public final class DistanceScoringFunctionConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DistanceScoringFunctionConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.DistanceScoringFunction} to
     * {@link DistanceScoringFunction}.
     */
    public static DistanceScoringFunction map(com.azure.search.documents.implementation.models.DistanceScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        DistanceScoringFunction distanceScoringFunction = new DistanceScoringFunction();

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation _interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            distanceScoringFunction.setInterpolation(_interpolation);
        }

        String _fieldName = obj.getFieldName();
        distanceScoringFunction.setFieldName(_fieldName);

        double _boost = obj.getBoost();
        distanceScoringFunction.setBoost(_boost);

        if (obj.getParameters() != null) {
            DistanceScoringParameters _parameters = DistanceScoringParametersConverter.map(obj.getParameters());
            distanceScoringFunction.setParameters(_parameters);
        }
        return distanceScoringFunction;
    }

    /**
     * Maps from {@link DistanceScoringFunction} to
     * {@link com.azure.search.documents.implementation.models.DistanceScoringFunction}.
     */
    public static com.azure.search.documents.implementation.models.DistanceScoringFunction map(DistanceScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.DistanceScoringFunction distanceScoringFunction =
            new com.azure.search.documents.implementation.models.DistanceScoringFunction();

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.implementation.models.ScoringFunctionInterpolation _interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            distanceScoringFunction.setInterpolation(_interpolation);
        }

        String _fieldName = obj.getFieldName();
        distanceScoringFunction.setFieldName(_fieldName);

        double _boost = obj.getBoost();
        distanceScoringFunction.setBoost(_boost);

        if (obj.getParameters() != null) {
            com.azure.search.documents.implementation.models.DistanceScoringParameters _parameters =
                DistanceScoringParametersConverter.map(obj.getParameters());
            distanceScoringFunction.setParameters(_parameters);
        }
        return distanceScoringFunction;
    }
}
