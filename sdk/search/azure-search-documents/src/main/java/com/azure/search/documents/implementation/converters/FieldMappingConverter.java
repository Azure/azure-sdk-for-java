// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.FieldMappingFunction;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.FieldMapping} and {@link FieldMapping}.
 */
public final class FieldMappingConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.FieldMapping} to {@link FieldMapping}.
     */
    public static FieldMapping map(com.azure.search.documents.indexes.implementation.models.FieldMapping obj) {
        if (obj == null) {
            return null;
        }
        FieldMapping fieldMapping = new FieldMapping();

        String sourceFieldName = obj.getSourceFieldName();
        fieldMapping.setSourceFieldName(sourceFieldName);

        String targetFieldName = obj.getTargetFieldName();
        fieldMapping.setTargetFieldName(targetFieldName);

        if (obj.getMappingFunction() != null) {
            FieldMappingFunction mappingFunction = FieldMappingFunctionConverter.map(obj.getMappingFunction());
            fieldMapping.setMappingFunction(mappingFunction);
        }
        return fieldMapping;
    }

    /**
     * Maps from {@link FieldMapping} to {@link com.azure.search.documents.indexes.implementation.models.FieldMapping}.
     */
    public static com.azure.search.documents.indexes.implementation.models.FieldMapping map(FieldMapping obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.FieldMapping fieldMapping =
            new com.azure.search.documents.indexes.implementation.models.FieldMapping();

        String sourceFieldName = obj.getSourceFieldName();
        fieldMapping.setSourceFieldName(sourceFieldName);

        String targetFieldName = obj.getTargetFieldName();
        fieldMapping.setTargetFieldName(targetFieldName);

        if (obj.getMappingFunction() != null) {
            com.azure.search.documents.indexes.implementation.models.FieldMappingFunction mappingFunction =
                FieldMappingFunctionConverter.map(obj.getMappingFunction());
            fieldMapping.setMappingFunction(mappingFunction);
        }
        return fieldMapping;
    }

    private FieldMappingConverter() {
    }
}
