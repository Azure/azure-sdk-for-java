package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.TagScoringParameters;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.TagScoringParameters} and
 * {@link TagScoringParameters}.
 */
public final class TagScoringParametersConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TagScoringParametersConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.TagScoringParameters} to
     * {@link TagScoringParameters}.
     */
    public static TagScoringParameters map(com.azure.search.documents.implementation.models.TagScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        TagScoringParameters tagScoringParameters = new TagScoringParameters();

        String _tagsParameter = obj.getTagsParameter();
        tagScoringParameters.setTagsParameter(_tagsParameter);
        return tagScoringParameters;
    }

    /**
     * Maps from {@link TagScoringParameters} to
     * {@link com.azure.search.documents.implementation.models.TagScoringParameters}.
     */
    public static com.azure.search.documents.implementation.models.TagScoringParameters map(TagScoringParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.TagScoringParameters tagScoringParameters =
            new com.azure.search.documents.implementation.models.TagScoringParameters();

        String _tagsParameter = obj.getTagsParameter();
        tagScoringParameters.setTagsParameter(_tagsParameter);
        return tagScoringParameters;
    }
}
