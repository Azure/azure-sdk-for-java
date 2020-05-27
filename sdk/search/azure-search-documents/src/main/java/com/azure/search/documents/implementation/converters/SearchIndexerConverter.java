// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.IndexingSchedule;
import com.azure.search.documents.indexes.models.SearchIndexer;

import java.util.List;
import java.util.stream.Collectors;

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
        SearchIndexer searchIndexer = new SearchIndexer();

        if (obj.getSchedule() != null) {
            IndexingSchedule schedule = IndexingScheduleConverter.map(obj.getSchedule());
            searchIndexer.setSchedule(schedule);
        }

        String skillsetName = obj.getSkillsetName();
        searchIndexer.setSkillsetName(skillsetName);

        String name = obj.getName();
        searchIndexer.setName(name);

        String description = obj.getDescription();
        searchIndexer.setDescription(description);

        String eTag = obj.getETag();
        searchIndexer.setETag(eTag);

        String targetIndexName = obj.getTargetIndexName();
        searchIndexer.setTargetIndexName(targetIndexName);

        if (obj.getFieldMappings() != null) {
            List<FieldMapping> fieldMappings =
                obj.getFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setFieldMappings(fieldMappings);
        }

        Boolean isDisabled = obj.isDisabled();
        searchIndexer.setIsDisabled(isDisabled);

        if (obj.getParameters() != null) {
            IndexingParameters parameters = IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(parameters);
        }

        String dataSourceName = obj.getDataSourceName();
        searchIndexer.setDataSourceName(dataSourceName);

        if (obj.getOutputFieldMappings() != null) {
            List<FieldMapping> outputFieldMappings =
                obj.getOutputFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setOutputFieldMappings(outputFieldMappings);
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
        com.azure.search.documents.indexes.implementation.models.SearchIndexer searchIndexer =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexer();

        if (obj.getSchedule() != null) {
            com.azure.search.documents.indexes.implementation.models.IndexingSchedule schedule =
                IndexingScheduleConverter.map(obj.getSchedule());
            searchIndexer.setSchedule(schedule);
        }

        String skillsetName = obj.getSkillsetName();
        searchIndexer.setSkillsetName(skillsetName);

        String name = obj.getName();
        searchIndexer.setName(name);

        String description = obj.getDescription();
        searchIndexer.setDescription(description);

        String eTag = obj.getETag();
        searchIndexer.setETag(eTag);

        String targetIndexName = obj.getTargetIndexName();
        searchIndexer.setTargetIndexName(targetIndexName);

        if (obj.getFieldMappings() != null) {
            List<com.azure.search.documents.indexes.implementation.models.FieldMapping> fieldMappings =
                obj.getFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setFieldMappings(fieldMappings);
        }

        Boolean isDisabled = obj.isDisabled();
        searchIndexer.setIsDisabled(isDisabled);

        if (obj.getParameters() != null) {
            com.azure.search.documents.indexes.implementation.models.IndexingParameters parameters =
                IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(parameters);
        }

        String dataSourceName = obj.getDataSourceName();
        searchIndexer.setDataSourceName(dataSourceName);

        if (obj.getOutputFieldMappings() != null) {
            List<com.azure.search.documents.indexes.implementation.models.FieldMapping> outputFieldMappings =
                obj.getOutputFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setOutputFieldMappings(outputFieldMappings);
        }
        return searchIndexer;
    }

    private SearchIndexerConverter() {
    }
}
