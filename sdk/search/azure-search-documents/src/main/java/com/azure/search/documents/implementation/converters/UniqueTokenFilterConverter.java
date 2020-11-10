// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.UniqueTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter} and
 * {@link UniqueTokenFilter}.
 */
public final class UniqueTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter} to
     * {@link UniqueTokenFilter}.
     */
    public static UniqueTokenFilter map(com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        UniqueTokenFilter uniqueTokenFilter = new UniqueTokenFilter(obj.getName());

        Boolean onlyOnSamePosition = obj.isOnlyOnSamePosition();
        uniqueTokenFilter.setOnlyOnSamePosition(onlyOnSamePosition);
        return uniqueTokenFilter;
    }

    /**
     * Maps from {@link UniqueTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter map(UniqueTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter uniqueTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter(obj.getName());

        Boolean onlyOnSamePosition = obj.isOnlyOnSamePosition();
        uniqueTokenFilter.setOnlyOnSamePosition(onlyOnSamePosition);

        return uniqueTokenFilter;
    }

    private UniqueTokenFilterConverter() {
    }
}
