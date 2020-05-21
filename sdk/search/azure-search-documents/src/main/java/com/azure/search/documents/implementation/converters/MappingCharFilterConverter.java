// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.MappingCharFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.MappingCharFilter} and
 * {@link MappingCharFilter}.
 */
public final class MappingCharFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MappingCharFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.MappingCharFilter} to
     * {@link MappingCharFilter}.
     */
    public static MappingCharFilter map(com.azure.search.documents.implementation.models.MappingCharFilter obj) {
        if (obj == null) {
            return null;
        }
        MappingCharFilter mappingCharFilter = new MappingCharFilter();

        String _name = obj.getName();
        mappingCharFilter.setName(_name);

        if (obj.getMappings() != null) {
            List<String> _mappings = new ArrayList<>(obj.getMappings());
            mappingCharFilter.setMappings(_mappings);
        }
        return mappingCharFilter;
    }

    /**
     * Maps from {@link MappingCharFilter} to
     * {@link com.azure.search.documents.implementation.models.MappingCharFilter}.
     */
    public static com.azure.search.documents.implementation.models.MappingCharFilter map(MappingCharFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.MappingCharFilter mappingCharFilter =
            new com.azure.search.documents.implementation.models.MappingCharFilter();

        String _name = obj.getName();
        mappingCharFilter.setName(_name);

        if (obj.getMappings() != null) {
            List<String> _mappings = new ArrayList<>(obj.getMappings());
            mappingCharFilter.setMappings(_mappings);
        }
        return mappingCharFilter;
    }
}
