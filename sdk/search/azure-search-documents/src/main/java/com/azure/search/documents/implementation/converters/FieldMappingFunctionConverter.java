// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.FieldMappingFunction;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.FieldMappingFunction} and
 * {@link FieldMappingFunction}.
 */
public final class FieldMappingFunctionConverter {
    private static final ClientLogger LOGGER = new ClientLogger(FieldMappingFunctionConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.FieldMappingFunction} to
     * {@link FieldMappingFunction}.
     */
    public static FieldMappingFunction map(com.azure.search.documents.implementation.models.FieldMappingFunction obj) {
        if (obj == null) {
            return null;
        }
        FieldMappingFunction fieldMappingFunction = new FieldMappingFunction();

        String _name = obj.getName();
        fieldMappingFunction.setName(_name);

        if (obj.getParameters() != null) {
            Map<String, Object> _parameters =
                obj.getParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            fieldMappingFunction.setParameters(_parameters);
        }
        return fieldMappingFunction;
    }

    /**
     * Maps from {@link FieldMappingFunction} to
     * {@link com.azure.search.documents.implementation.models.FieldMappingFunction}.
     */
    public static com.azure.search.documents.implementation.models.FieldMappingFunction map(FieldMappingFunction obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.FieldMappingFunction fieldMappingFunction =
            new com.azure.search.documents.implementation.models.FieldMappingFunction();

        String _name = obj.getName();
        fieldMappingFunction.setName(_name);

        if (obj.getParameters() != null) {
            Map<String, Object> _parameters =
                obj.getParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            fieldMappingFunction.setParameters(_parameters);
        }
        return fieldMappingFunction;
    }
}
