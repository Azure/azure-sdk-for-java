// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.MappingCharFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.MappingCharFilter} and
 * {@link MappingCharFilter}.
 */
public final class MappingCharFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.MappingCharFilter} to
     * {@link MappingCharFilter}.
     */
    public static MappingCharFilter map(com.azure.search.documents.indexes.implementation.models.MappingCharFilter obj) {
        if (obj == null) {
            return null;
        }
        MappingCharFilter mappingCharFilter = new MappingCharFilter();

        String name = obj.getName();
        mappingCharFilter.setName(name);

        if (obj.getMappings() != null) {
            List<String> mappings = new ArrayList<>(obj.getMappings());
            mappingCharFilter.setMappings(mappings);
        }
        return mappingCharFilter;
    }

    /**
     * Maps from {@link MappingCharFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.MappingCharFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MappingCharFilter map(MappingCharFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.MappingCharFilter mappingCharFilter =
            new com.azure.search.documents.indexes.implementation.models.MappingCharFilter();

        String name = obj.getName();
        mappingCharFilter.setName(name);

        if (obj.getMappings() != null) {
            List<String> mappings = new ArrayList<>(obj.getMappings());
            mappingCharFilter.setMappings(mappings);
        }
        return mappingCharFilter;
    }

    private MappingCharFilterConverter() {
    }
}
