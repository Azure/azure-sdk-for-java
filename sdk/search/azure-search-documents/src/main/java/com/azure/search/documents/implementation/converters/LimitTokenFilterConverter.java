// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.LimitTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LimitTokenFilter} and
 * {@link LimitTokenFilter}.
 */
public final class LimitTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(LimitTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.LimitTokenFilter} to {@link LimitTokenFilter}.
     */
    public static LimitTokenFilter map(com.azure.search.documents.implementation.models.LimitTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        LimitTokenFilter limitTokenFilter = new LimitTokenFilter();

        String _name = obj.getName();
        limitTokenFilter.setName(_name);

        Integer _maxTokenCount = obj.getMaxTokenCount();
        limitTokenFilter.setMaxTokenCount(_maxTokenCount);

        Boolean _consumeAllTokens = obj.isConsumeAllTokens();
        limitTokenFilter.setConsumeAllTokens(_consumeAllTokens);
        return limitTokenFilter;
    }

    /**
     * Maps from {@link LimitTokenFilter} to {@link com.azure.search.documents.implementation.models.LimitTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.LimitTokenFilter map(LimitTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.LimitTokenFilter limitTokenFilter =
            new com.azure.search.documents.implementation.models.LimitTokenFilter();

        String _name = obj.getName();
        limitTokenFilter.setName(_name);

        Integer _maxTokenCount = obj.getMaxTokenCount();
        limitTokenFilter.setMaxTokenCount(_maxTokenCount);

        Boolean _consumeAllTokens = obj.isConsumeAllTokens();
        limitTokenFilter.setConsumeAllTokens(_consumeAllTokens);
        return limitTokenFilter;
    }
}
