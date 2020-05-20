// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.FreshnessScoringFunction;
import com.azure.search.documents.models.FreshnessScoringParameters;
import com.azure.search.documents.models.ScoringFunctionInterpolation;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.FreshnessScoringFunction} and
 * {@link FreshnessScoringFunction}.
 */
public final class FreshnessScoringFunctionConverter {
    private static final ClientLogger LOGGER = new ClientLogger(FreshnessScoringFunctionConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.FreshnessScoringFunction} to
     * {@link FreshnessScoringFunction}.
     */
    public static FreshnessScoringFunction map(com.azure.search.documents.implementation.models.FreshnessScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        FreshnessScoringFunction freshnessScoringFunction = new FreshnessScoringFunction();

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation _interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            freshnessScoringFunction.setInterpolation(_interpolation);
        }

        String _fieldName = obj.getFieldName();
        freshnessScoringFunction.setFieldName(_fieldName);

        double _boost = obj.getBoost();
        freshnessScoringFunction.setBoost(_boost);

        if (obj.getParameters() != null) {
            FreshnessScoringParameters _parameters = FreshnessScoringParametersConverter.map(obj.getParameters());
            freshnessScoringFunction.setParameters(_parameters);
        }
        return freshnessScoringFunction;
    }

    /**
     * Maps from {@link FreshnessScoringFunction} to
     * {@link com.azure.search.documents.implementation.models.FreshnessScoringFunction}.
     */
    public static com.azure.search.documents.implementation.models.FreshnessScoringFunction map(FreshnessScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.FreshnessScoringFunction freshnessScoringFunction =
            new com.azure.search.documents.implementation.models.FreshnessScoringFunction();

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.implementation.models.ScoringFunctionInterpolation _interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            freshnessScoringFunction.setInterpolation(_interpolation);
        }

        String _fieldName = obj.getFieldName();
        freshnessScoringFunction.setFieldName(_fieldName);

        double _boost = obj.getBoost();
        freshnessScoringFunction.setBoost(_boost);

        if (obj.getParameters() != null) {
            com.azure.search.documents.implementation.models.FreshnessScoringParameters _parameters =
                FreshnessScoringParametersConverter.map(obj.getParameters());
            freshnessScoringFunction.setParameters(_parameters);
        }
        return freshnessScoringFunction;
    }
}
