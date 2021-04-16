// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchIndexerSkillset;

import java.util.Objects;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset} and
 * {@link SearchIndexerSkillset}.
 */
public final class SearchIndexerSkillsetConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset} to
     * {@link SearchIndexerSkillset}.
     */
    public static SearchIndexerSkillset map(com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset obj) {
        if (obj == null) {
            return null;
        }

        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset(obj.getName(), obj.getSkills());

        if (obj.getCognitiveServicesAccount() != null) {
            searchIndexerSkillset.setCognitiveServicesAccount(obj.getCognitiveServicesAccount());
        }

        searchIndexerSkillset.setDescription(obj.getDescription());
        searchIndexerSkillset.setETag(obj.getETag());

        if (obj.getEncryptionKey() != null) {
            searchIndexerSkillset.setEncryptionKey(SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey()));
        }

        return searchIndexerSkillset;
    }

    /**
     * Maps from {@link SearchIndexerSkillset} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset map(SearchIndexerSkillset obj) {
        if (obj == null) {
            return null;
        }
        Objects.requireNonNull(obj.getName(), "The SearchIndexerSkillset name cannot be null");
        com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset searchIndexerSkillset =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset()
                .setName(obj.getName())
                .setSkills(obj.getSkills());

        if (obj.getCognitiveServicesAccount() != null) {
            searchIndexerSkillset.setCognitiveServicesAccount(obj.getCognitiveServicesAccount());
        }

        searchIndexerSkillset.setDescription(obj.getDescription());
        searchIndexerSkillset.setETag(obj.getETag());

        if (obj.getEncryptionKey() != null) {
            searchIndexerSkillset.setEncryptionKey(SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey()));
        }

        return searchIndexerSkillset;
    }

    private SearchIndexerSkillsetConverter() {
    }
}
