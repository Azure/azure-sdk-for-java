// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;
import com.azure.search.documents.indexes.models.TagScoringFunction;
import com.azure.search.documents.indexes.models.TagScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TagScoringFunction} and
 * {@link TagScoringFunction}.
 */
public final class TagScoringFunctionConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.TagScoringFunction} to
     * {@link TagScoringFunction}.
     */
    public static TagScoringFunction map(com.azure.search.documents.indexes.implementation.models.TagScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        TagScoringFunction tagScoringFunction = new TagScoringFunction();

        if (obj.getInterpolation() != null) {
            ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            tagScoringFunction.setInterpolation(interpolation);
        }

        String fieldName = obj.getFieldName();
        tagScoringFunction.setFieldName(fieldName);

        double boost = obj.getBoost();
        tagScoringFunction.setBoost(boost);

        if (obj.getParameters() != null) {
            TagScoringParameters parameters = TagScoringParametersConverter.map(obj.getParameters());
            tagScoringFunction.setParameters(parameters);
        }
        return tagScoringFunction;
    }

    /**
     * Maps from {@link TagScoringFunction} to
     * {@link com.azure.search.documents.indexes.implementation.models.TagScoringFunction}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TagScoringFunction map(TagScoringFunction obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.TagScoringFunction tagScoringFunction =
            new com.azure.search.documents.indexes.implementation.models.TagScoringFunction();

        if (obj.getInterpolation() != null) {
            com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation interpolation =
                ScoringFunctionInterpolationConverter.map(obj.getInterpolation());
            tagScoringFunction.setInterpolation(interpolation);
        }

        String fieldName = obj.getFieldName();
        tagScoringFunction.setFieldName(fieldName);

        double boost = obj.getBoost();
        tagScoringFunction.setBoost(boost);

        if (obj.getParameters() != null) {
            com.azure.search.documents.indexes.implementation.models.TagScoringParameters parameters =
                TagScoringParametersConverter.map(obj.getParameters());
            tagScoringFunction.setParameters(parameters);
        }
        return tagScoringFunction;
    }

    private TagScoringFunctionConverter() {
    }
}
