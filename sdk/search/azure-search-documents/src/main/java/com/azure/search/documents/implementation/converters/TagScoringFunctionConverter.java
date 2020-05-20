// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ScoringFunctionInterpolation;
import com.azure.search.documents.models.TagScoringFunction;
import com.azure.search.documents.models.TagScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.TagScoringFunction} and
 * {@link TagScoringFunction}.
 */
public final class TagScoringFunctionConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TagScoringFunctionConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.TagScoringFunction} to
     * {@link TagScoringFunction}.
     */
    public static TagScoringFunction map(com.azure.search.documents.implementation.models.TagScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        TagScoringFunction tagScoringFunction = new TagScoringFunction();

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation _interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            tagScoringFunction.setInterpolation(_interpolation);
        }

        String _fieldName = obj.getFieldName();
        tagScoringFunction.setFieldName(_fieldName);

        double _boost = obj.getBoost();
        tagScoringFunction.setBoost(_boost);

        if (obj.getParameters() != null) {
            TagScoringParameters _parameters = TagScoringParametersConverter.map(obj.getParameters());
            tagScoringFunction.setParameters(_parameters);
        }
        return tagScoringFunction;
    }

    /**
     * Maps from {@link TagScoringFunction} to
     * {@link com.azure.search.documents.implementation.models.TagScoringFunction}.
     */
    public static com.azure.search.documents.implementation.models.TagScoringFunction map(TagScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.TagScoringFunction tagScoringFunction =
            new com.azure.search.documents.implementation.models.TagScoringFunction();

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.implementation.models.ScoringFunctionInterpolation _interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            tagScoringFunction.setInterpolation(_interpolation);
        }

        String _fieldName = obj.getFieldName();
        tagScoringFunction.setFieldName(_fieldName);

        double _boost = obj.getBoost();
        tagScoringFunction.setBoost(_boost);

        if (obj.getParameters() != null) {
            com.azure.search.documents.implementation.models.TagScoringParameters _parameters =
                TagScoringParametersConverter.map(obj.getParameters());
            tagScoringFunction.setParameters(_parameters);
        }
        return tagScoringFunction;
    }
}
