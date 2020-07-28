// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.TruncateTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter} and
 * {@link TruncateTokenFilter}.
 */
public final class TruncateTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter} to
     * {@link TruncateTokenFilter}.
     */
    public static TruncateTokenFilter map(com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        TruncateTokenFilter truncateTokenFilter = new TruncateTokenFilter(obj.getName());

        Integer length = obj.getLength();
        truncateTokenFilter.setLength(length);
        return truncateTokenFilter;
    }

    /**
     * Maps from {@link TruncateTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter map(TruncateTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter truncateTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter(obj.getName());

        Integer length = obj.getLength();
        truncateTokenFilter.setLength(length);
        return truncateTokenFilter;
    }

    private TruncateTokenFilterConverter() {
    }
}
