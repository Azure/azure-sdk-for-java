// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.MappingCharFilter;
import com.azure.search.documents.indexes.models.PatternReplaceCharFilter;
import com.azure.search.documents.indexes.models.CharFilter;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CharFilter} and {@link CharFilter}.
 */
public final class CharFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(CharFilterConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.CharFilter} to
     * {@link CharFilter}. Dedicate works to sub class converter.
     */
    public static CharFilter map(com.azure.search.documents.indexes.implementation.models.CharFilter obj) {
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter) {
            return PatternReplaceCharFilterConverter.map((com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.MappingCharFilter) {
            return MappingCharFilterConverter.map((com.azure.search.documents.indexes.implementation.models.MappingCharFilter) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link CharFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.CharFilter}. Dedicate works to sub class converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.CharFilter map(CharFilter obj) {
        if (obj instanceof PatternReplaceCharFilter) {
            return PatternReplaceCharFilterConverter.map((PatternReplaceCharFilter) obj);
        }
        if (obj instanceof MappingCharFilter) {
            return MappingCharFilterConverter.map((MappingCharFilter) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private CharFilterConverter() {
    }
}
