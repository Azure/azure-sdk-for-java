// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.FieldMapping;
import com.azure.search.documents.models.FieldMappingFunction;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.FieldMapping} and {@link FieldMapping}.
 */
public final class FieldMappingConverter {
    private static final ClientLogger LOGGER = new ClientLogger(FieldMappingConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.FieldMapping} to {@link FieldMapping}.
     */
    public static FieldMapping map(com.azure.search.documents.implementation.models.FieldMapping obj) {
        if (obj == null) {
            return null;
        }
        FieldMapping fieldMapping = new FieldMapping();

        String _sourceFieldName = obj.getSourceFieldName();
        fieldMapping.setSourceFieldName(_sourceFieldName);

        String _targetFieldName = obj.getTargetFieldName();
        fieldMapping.setTargetFieldName(_targetFieldName);

        if (obj.getMappingFunction() != null) {
            FieldMappingFunction _mappingFunction = FieldMappingFunctionConverter.map(obj.getMappingFunction());
            fieldMapping.setMappingFunction(_mappingFunction);
        }
        return fieldMapping;
    }

    /**
     * Maps from {@link FieldMapping} to {@link com.azure.search.documents.implementation.models.FieldMapping}.
     */
    public static com.azure.search.documents.implementation.models.FieldMapping map(FieldMapping obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.FieldMapping fieldMapping =
            new com.azure.search.documents.implementation.models.FieldMapping();

        String _sourceFieldName = obj.getSourceFieldName();
        fieldMapping.setSourceFieldName(_sourceFieldName);

        String _targetFieldName = obj.getTargetFieldName();
        fieldMapping.setTargetFieldName(_targetFieldName);

        if (obj.getMappingFunction() != null) {
            com.azure.search.documents.implementation.models.FieldMappingFunction _mappingFunction =
                FieldMappingFunctionConverter.map(obj.getMappingFunction());
            fieldMapping.setMappingFunction(_mappingFunction);
        }
        return fieldMapping;
    }
}
