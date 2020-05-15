package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerSkill} and
 * {@link SearchIndexerSkill} mismatch.
 */
public final class SearchIndexerSkillConverter {
    public static SearchIndexerSkill convert(com.azure.search.documents.models.SearchIndexerSkill obj) {
        return DefaultConverter.convert(obj, SearchIndexerSkill.class);
    }

    public static com.azure.search.documents.models.SearchIndexerSkill convert(SearchIndexerSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerSkill.class);
    }
}
