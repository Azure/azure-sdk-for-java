// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.SearchIndexer;

import java.util.Objects;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexer} and {@link SearchIndexer}.
 */
public final class SearchIndexerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexer} to {@link SearchIndexer}.
     */
    public static SearchIndexer map(com.azure.search.documents.indexes.implementation.models.SearchIndexer obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexer searchIndexer = new SearchIndexer(obj.getName(), obj.getDataSourceName(),
            obj.getTargetIndexName());

        if (obj.getSchedule() != null) {
            searchIndexer.setSchedule(obj.getSchedule());
        }

        String skillsetName = obj.getSkillsetName();
        searchIndexer.setSkillsetName(skillsetName);

        String description = obj.getDescription();
        searchIndexer.setDescription(description);

        String eTag = obj.getETag();
        searchIndexer.setETag(eTag);

        if (obj.getFieldMappings() != null) {
            searchIndexer.setFieldMappings(obj.getFieldMappings());
        }

        Boolean isDisabled = obj.isDisabled();
        searchIndexer.setIsDisabled(isDisabled);

        if (obj.getParameters() != null) {
            IndexingParameters parameters = IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(parameters);
        }

        if (obj.getOutputFieldMappings() != null) {
            searchIndexer.setOutputFieldMappings(obj.getOutputFieldMappings());
        }

        if (obj.getEncryptionKey() != null) {
            searchIndexer.setEncryptionKey(SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey()));
        }
        return searchIndexer;
    }

    /**
     * Maps from {@link SearchIndexer} to {@link com.azure.search.documents.indexes.implementation.models.SearchIndexer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexer map(SearchIndexer obj) {
        if (obj == null) {
            return null;
        }
        Objects.requireNonNull(obj.getName(), "The SearchIndexer name cannot be null");
        com.azure.search.documents.indexes.implementation.models.SearchIndexer searchIndexer =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexer()
                .setName(obj.getName())
                .setDataSourceName(obj.getDataSourceName())
                .setTargetIndexName(obj.getTargetIndexName());

        if (obj.getSchedule() != null) {
            searchIndexer.setSchedule(obj.getSchedule());
        }

        String skillsetName = obj.getSkillsetName();
        searchIndexer.setSkillsetName(skillsetName);

        String description = obj.getDescription();
        searchIndexer.setDescription(description);

        String eTag = obj.getETag();
        searchIndexer.setETag(eTag);

        if (obj.getFieldMappings() != null) {
            searchIndexer.setFieldMappings(obj.getFieldMappings());
        }

        Boolean isDisabled = obj.isDisabled();
        searchIndexer.setIsDisabled(isDisabled);

        if (obj.getParameters() != null) {
            com.azure.search.documents.indexes.implementation.models.IndexingParameters parameters =
                IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(parameters);
        }

        if (obj.getOutputFieldMappings() != null) {
            searchIndexer.setOutputFieldMappings(obj.getOutputFieldMappings());
        }

        if (obj.getEncryptionKey() != null) {
            searchIndexer.setEncryptionKey(SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey()));
        }
        return searchIndexer;
    }

    private SearchIndexerConverter() {
    }
}
