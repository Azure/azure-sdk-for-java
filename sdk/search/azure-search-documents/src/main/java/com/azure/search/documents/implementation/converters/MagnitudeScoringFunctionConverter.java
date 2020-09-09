// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction} and
 * {@link MagnitudeScoringFunction}.
 */
public final class MagnitudeScoringFunctionConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction} to
     * {@link MagnitudeScoringFunction}.
     */
    public static MagnitudeScoringFunction map(com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction obj) {
        if (obj == null) {
            return null;
        }

        MagnitudeScoringFunction magnitudeScoringFunction = new MagnitudeScoringFunction(obj.getFieldName(),
            obj.getBoost(), MagnitudeScoringParametersConverter.map(obj.getParameters()));

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            magnitudeScoringFunction.setInterpolation(interpolation);
        }

        return magnitudeScoringFunction;
    }

    /**
     * Maps from {@link MagnitudeScoringFunction} to
     * {@link com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction map(MagnitudeScoringFunction obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.MagnitudeScoringParameters parameters =
            obj.getParameters() == null ? null : MagnitudeScoringParametersConverter.map(obj.getParameters());

        com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction magnitudeScoringFunction =
            new com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction(
                obj.getFieldName(), obj.getBoost(), parameters);

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            magnitudeScoringFunction.setInterpolation(interpolation);
        }

        magnitudeScoringFunction.validate();
        return magnitudeScoringFunction;
    }

    private MagnitudeScoringFunctionConverter() {
    }
}
