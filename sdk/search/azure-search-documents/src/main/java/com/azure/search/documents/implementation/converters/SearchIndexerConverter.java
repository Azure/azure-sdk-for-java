// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.FieldMapping;
import com.azure.search.documents.models.IndexingParameters;
import com.azure.search.documents.models.IndexingSchedule;
import com.azure.search.documents.models.SearchIndexer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexer} and {@link SearchIndexer}.
 */
public final class SearchIndexerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndexer} to {@link SearchIndexer}.
     */
    public static SearchIndexer map(com.azure.search.documents.implementation.models.SearchIndexer obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexer searchIndexer = new SearchIndexer();

        if (obj.getSchedule() != null) {
            IndexingSchedule _schedule = IndexingScheduleConverter.map(obj.getSchedule());
            searchIndexer.setSchedule(_schedule);
        }

        String _skillsetName = obj.getSkillsetName();
        searchIndexer.setSkillsetName(_skillsetName);

        String _name = obj.getName();
        searchIndexer.setName(_name);

        String _description = obj.getDescription();
        searchIndexer.setDescription(_description);

        String _eTag = obj.getETag();
        searchIndexer.setETag(_eTag);

        String _targetIndexName = obj.getTargetIndexName();
        searchIndexer.setTargetIndexName(_targetIndexName);

        if (obj.getFieldMappings() != null) {
            List<FieldMapping> _fieldMappings =
                obj.getFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setFieldMappings(_fieldMappings);
        }

        Boolean _isDisabled = obj.isDisabled();
        searchIndexer.setIsDisabled(_isDisabled);

        if (obj.getParameters() != null) {
            IndexingParameters _parameters = IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(_parameters);
        }

        String _dataSourceName = obj.getDataSourceName();
        searchIndexer.setDataSourceName(_dataSourceName);

        if (obj.getOutputFieldMappings() != null) {
            List<FieldMapping> _outputFieldMappings =
                obj.getOutputFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setOutputFieldMappings(_outputFieldMappings);
        }
        return searchIndexer;
    }

    /**
     * Maps from {@link SearchIndexer} to {@link com.azure.search.documents.implementation.models.SearchIndexer}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexer map(SearchIndexer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexer searchIndexer =
            new com.azure.search.documents.implementation.models.SearchIndexer();

        if (obj.getSchedule() != null) {
            com.azure.search.documents.implementation.models.IndexingSchedule _schedule =
                IndexingScheduleConverter.map(obj.getSchedule());
            searchIndexer.setSchedule(_schedule);
        }

        String _skillsetName = obj.getSkillsetName();
        searchIndexer.setSkillsetName(_skillsetName);

        String _name = obj.getName();
        searchIndexer.setName(_name);

        String _description = obj.getDescription();
        searchIndexer.setDescription(_description);

        String _eTag = obj.getETag();
        searchIndexer.setETag(_eTag);

        String _targetIndexName = obj.getTargetIndexName();
        searchIndexer.setTargetIndexName(_targetIndexName);

        if (obj.getFieldMappings() != null) {
            List<com.azure.search.documents.implementation.models.FieldMapping> _fieldMappings =
                obj.getFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setFieldMappings(_fieldMappings);
        }

        Boolean _isDisabled = obj.isDisabled();
        searchIndexer.setIsDisabled(_isDisabled);

        if (obj.getParameters() != null) {
            com.azure.search.documents.implementation.models.IndexingParameters _parameters =
                IndexingParametersConverter.map(obj.getParameters());
            searchIndexer.setParameters(_parameters);
        }

        String _dataSourceName = obj.getDataSourceName();
        searchIndexer.setDataSourceName(_dataSourceName);

        if (obj.getOutputFieldMappings() != null) {
            List<com.azure.search.documents.implementation.models.FieldMapping> _outputFieldMappings =
                obj.getOutputFieldMappings().stream().map(FieldMappingConverter::map).collect(Collectors.toList());
            searchIndexer.setOutputFieldMappings(_outputFieldMappings);
        }
        return searchIndexer;
    }
}
