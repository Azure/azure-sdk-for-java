package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerSkillset;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerSkillset} and
 * {@link SearchIndexerSkillset} mismatch.
 */
public final class SearchIndexerSkillsetConverter {
    public static SearchIndexerSkillset convert(com.azure.search.documents.models.SearchIndexerSkillset obj) {
        return DefaultConverter.convert(obj, SearchIndexerSkillset.class);
    }

    public static com.azure.search.documents.models.SearchIndexerSkillset convert(SearchIndexerSkillset obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerSkillset.class);
    }
}
