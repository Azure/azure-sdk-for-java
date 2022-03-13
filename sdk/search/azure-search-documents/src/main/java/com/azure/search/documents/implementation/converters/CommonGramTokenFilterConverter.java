// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CommonGramTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CommonGramTokenFilter} and
 * {@link CommonGramTokenFilter}.
 */
public final class CommonGramTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.CommonGramTokenFilter} to
     * {@link CommonGramTokenFilter}.
     */
    public static CommonGramTokenFilter map(com.azure.search.documents.indexes.implementation.models.CommonGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new CommonGramTokenFilter(obj.getName(), obj.getCommonWords())
            .setCaseIgnored(obj.isIgnoreCase())
            .setQueryModeUsed(obj.isUseQueryMode());
    }

    /**
     * Maps from {@link CommonGramTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.CommonGramTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CommonGramTokenFilter map(CommonGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.CommonGramTokenFilter(obj.getName(),
                obj.getCommonWords())
            .setIgnoreCase(obj.isCaseIgnored())
            .setUseQueryMode(obj.isQueryModeUsed());
    }

    private CommonGramTokenFilterConverter() {
    }
}
