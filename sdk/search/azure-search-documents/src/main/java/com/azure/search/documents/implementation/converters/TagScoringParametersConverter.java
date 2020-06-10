// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.TagScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TagScoringParameters} and
 * {@link TagScoringParameters}.
 */
public final class TagScoringParametersConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.TagScoringParameters} to
     * {@link TagScoringParameters}.
     */
    public static TagScoringParameters map(com.azure.search.documents.indexes.implementation.models.TagScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        TagScoringParameters tagScoringParameters = new TagScoringParameters();

        String tagsParameter = obj.getTagsParameter();
        tagScoringParameters.setTagsParameter(tagsParameter);
        return tagScoringParameters;
    }

    /**
     * Maps from {@link TagScoringParameters} to
     * {@link com.azure.search.documents.indexes.implementation.models.TagScoringParameters}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TagScoringParameters map(TagScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.TagScoringParameters tagScoringParameters =
            new com.azure.search.documents.indexes.implementation.models.TagScoringParameters();

        String tagsParameter = obj.getTagsParameter();
        tagScoringParameters.setTagsParameter(tagsParameter);
        return tagScoringParameters;
    }

    private TagScoringParametersConverter() {
    }
}
