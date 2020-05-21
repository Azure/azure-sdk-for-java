// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SearchIndexerDataSourceType;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerDataSourceType} and
 * {@link SearchIndexerDataSourceType}.
 */
public final class SearchIndexerDataSourceTypeConverter {


    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.SearchIndexerDataSourceType} to enum
     * {@link SearchIndexerDataSourceType}.
     */
    public static SearchIndexerDataSourceType map(com.azure.search.documents.implementation.models.SearchIndexerDataSourceType obj) {
        if (obj == null) {
            return null;
        }
        return SearchIndexerDataSourceType.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link SearchIndexerDataSourceType} to enum
     * {@link com.azure.search.documents.implementation.models.SearchIndexerDataSourceType}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerDataSourceType map(SearchIndexerDataSourceType obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.SearchIndexerDataSourceType.fromString(obj.toString());
    }

    private SearchIndexerDataSourceTypeConverter() {
    }
}
