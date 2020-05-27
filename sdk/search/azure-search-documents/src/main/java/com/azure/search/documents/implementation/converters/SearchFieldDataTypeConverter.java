// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchFieldDataType;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchFieldDataType} and
 * {@link SearchFieldDataType}.
 */
public final class SearchFieldDataTypeConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.SearchFieldDataType} to enum
     * {@link SearchFieldDataType}.
     */
    public static SearchFieldDataType map(com.azure.search.documents.indexes.implementation.models.SearchFieldDataType obj) {
        if (obj == null) {
            return null;
        }
        return SearchFieldDataType.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link SearchFieldDataType} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.SearchFieldDataType}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchFieldDataType map(SearchFieldDataType obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.SearchFieldDataType.fromString(obj.toString());
    }

    private SearchFieldDataTypeConverter() {
    }
}
