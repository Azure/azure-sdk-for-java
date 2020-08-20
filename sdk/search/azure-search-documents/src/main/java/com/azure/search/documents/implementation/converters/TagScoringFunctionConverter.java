// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.TagScoringFunction;

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

        TagScoringFunction tagScoringFunction = new TagScoringFunction(obj.getFieldName(), obj.getBoost(),
            obj.getParameters());

        if (obj.getInterpolation() != null) {
            tagScoringFunction.setInterpolation(obj.getInterpolation());
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
            new com.azure.search.documents.indexes.implementation.models.TagScoringFunction(
                obj.getFieldName(), obj.getBoost(), obj.getParameters());

        if (obj.getInterpolation() != null) {
            tagScoringFunction.setInterpolation(obj.getInterpolation());
        }

        return tagScoringFunction;
    }

    private TagScoringFunctionConverter() {
    }
}
