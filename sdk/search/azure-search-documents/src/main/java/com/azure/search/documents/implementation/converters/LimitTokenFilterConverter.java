// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LimitTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LimitTokenFilter} and
 * {@link LimitTokenFilter}.
 */
public final class LimitTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.LimitTokenFilter} to {@link LimitTokenFilter}.
     */
    public static LimitTokenFilter map(com.azure.search.documents.indexes.implementation.models.LimitTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        LimitTokenFilter limitTokenFilter = new LimitTokenFilter(obj.getName());

        Integer maxTokenCount = obj.getMaxTokenCount();
        limitTokenFilter.setMaxTokenCount(maxTokenCount);

        Boolean consumeAllTokens = obj.isConsumeAllTokens();
        limitTokenFilter.setAllTokensConsumed(consumeAllTokens);
        return limitTokenFilter;
    }

    /**
     * Maps from {@link LimitTokenFilter} to {@link com.azure.search.documents.indexes.implementation.models.LimitTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.LimitTokenFilter map(LimitTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.LimitTokenFilter limitTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.LimitTokenFilter(obj.getName());

        Integer maxTokenCount = obj.getMaxTokenCount();
        limitTokenFilter.setMaxTokenCount(maxTokenCount);

        Boolean consumeAllTokens = obj.areAllTokensConsumed();
        limitTokenFilter.setConsumeAllTokens(consumeAllTokens);

        return limitTokenFilter;
    }

    private LimitTokenFilterConverter() {
    }
}
