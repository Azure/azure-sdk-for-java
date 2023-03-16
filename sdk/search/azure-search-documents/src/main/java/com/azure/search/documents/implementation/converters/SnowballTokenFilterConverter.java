// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SnowballTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter} and
 * {@link SnowballTokenFilter}.
 */
public final class SnowballTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter} to
     * {@link SnowballTokenFilter}.
     */
    public static SnowballTokenFilter map(com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new SnowballTokenFilter(obj.getName(), obj.getLanguage());
    }

    /**
     * Maps from {@link SnowballTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter map(SnowballTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter(obj.getName(),
            obj.getLanguage());
    }

    private SnowballTokenFilterConverter() {
    }
}
