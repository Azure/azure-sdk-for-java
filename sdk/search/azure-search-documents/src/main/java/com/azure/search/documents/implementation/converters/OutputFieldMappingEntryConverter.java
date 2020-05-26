// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.OutputFieldMappingEntry;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.OutputFieldMappingEntry} and
 * {@link OutputFieldMappingEntry}.
 */
public final class OutputFieldMappingEntryConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.OutputFieldMappingEntry} to
     * {@link OutputFieldMappingEntry}.
     */
    public static OutputFieldMappingEntry map(com.azure.search.documents.implementation.models.OutputFieldMappingEntry obj) {
        if (obj == null) {
            return null;
        }
        OutputFieldMappingEntry outputFieldMappingEntry = new OutputFieldMappingEntry();

        String targetName = obj.getTargetName();
        outputFieldMappingEntry.setTargetName(targetName);

        String name = obj.getName();
        outputFieldMappingEntry.setName(name);
        return outputFieldMappingEntry;
    }

    /**
     * Maps from {@link OutputFieldMappingEntry} to
     * {@link com.azure.search.documents.implementation.models.OutputFieldMappingEntry}.
     */
    public static com.azure.search.documents.implementation.models.OutputFieldMappingEntry map(OutputFieldMappingEntry obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.OutputFieldMappingEntry outputFieldMappingEntry =
            new com.azure.search.documents.implementation.models.OutputFieldMappingEntry();

        String targetName = obj.getTargetName();
        outputFieldMappingEntry.setTargetName(targetName);

        String name = obj.getName();
        outputFieldMappingEntry.setName(name);
        return outputFieldMappingEntry;
    }

    private OutputFieldMappingEntryConverter() {
    }
}
