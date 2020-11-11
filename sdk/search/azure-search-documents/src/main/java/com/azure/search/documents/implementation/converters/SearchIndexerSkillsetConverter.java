// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CognitiveServicesAccount;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        List<SearchIndexerSkill> skills = obj.getSkills() == null ? null
            : obj.getSkills().stream().map(SearchIndexerSkillConverter::map).collect(Collectors.toList());
        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset(obj.getName(), skills);

        if (obj.getCognitiveServicesAccount() != null) {
            CognitiveServicesAccount cognitiveServicesAccount =
                CognitiveServicesAccountConverter.map(obj.getCognitiveServicesAccount());
            searchIndexerSkillset.setCognitiveServicesAccount(cognitiveServicesAccount);
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
        List<com.azure.search.documents.indexes.implementation.models.SearchIndexerSkill> skills =
            obj.getSkills() == null ? null
                : obj.getSkills().stream().map(SearchIndexerSkillConverter::map).collect(Collectors.toList());
        com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset searchIndexerSkillset =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexerSkillset()
                .setName(obj.getName())
                .setSkills(skills);

        if (obj.getCognitiveServicesAccount() != null) {
            com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccount cognitiveServicesAccount =
                CognitiveServicesAccountConverter.map(obj.getCognitiveServicesAccount());
            searchIndexerSkillset.setCognitiveServicesAccount(cognitiveServicesAccount);
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
