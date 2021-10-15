// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.SearchIndexer;

import java.util.Objects;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexer} and {@link
 * SearchIndexer}.
 */
public final class SearchIndexerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexer} to {@link
     * SearchIndexer}.
     */
    public static SearchIndexer map(com.azure.search.documents.indexes.implementation.models.SearchIndexer obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexer searchIndexer = new SearchIndexer(obj.getName(), obj.getDataSourceName(),
            obj.getTargetIndexName());

        searchIndexer.setSchedule(obj.getSchedule());
        searchIndexer.setSkillsetName(obj.getSkillsetName());
        searchIndexer.setDescription(obj.getDescription());
        searchIndexer.setETag(obj.getETag());
        searchIndexer.setFieldMappings(obj.getFieldMappings());
        searchIndexer.setIsDisabled(obj.isDisabled());

        if (obj.getParameters() != null) {
            IndexingParameters parameters = IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(parameters);
        }

        searchIndexer.setOutputFieldMappings(obj.getOutputFieldMappings());
        searchIndexer.setEncryptionKey(obj.getEncryptionKey());
        searchIndexer.setCache(obj.getCache());

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

        searchIndexer.setSchedule(obj.getSchedule());
        searchIndexer.setSkillsetName(obj.getSkillsetName());
        searchIndexer.setDescription(obj.getDescription());
        searchIndexer.setETag(obj.getETag());
        searchIndexer.setFieldMappings(obj.getFieldMappings());
        searchIndexer.setIsDisabled(obj.isDisabled());

        if (obj.getParameters() != null) {
            com.azure.search.documents.indexes.implementation.models.IndexingParameters parameters =
                IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(parameters);
        }

        searchIndexer.setOutputFieldMappings(obj.getOutputFieldMappings());
        searchIndexer.setEncryptionKey(obj.getEncryptionKey());
        searchIndexer.setCache(obj.getCache());

        return searchIndexer;
    }

    private SearchIndexerConverter() {
    }
}
