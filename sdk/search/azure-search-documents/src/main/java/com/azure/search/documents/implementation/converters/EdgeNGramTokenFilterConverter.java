// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.EdgeNGramTokenFilter;
import com.azure.search.documents.models.EdgeNGramTokenFilterSide;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.EdgeNGramTokenFilter} and
 * {@link EdgeNGramTokenFilter}.
 */
public final class EdgeNGramTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(EdgeNGramTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.EdgeNGramTokenFilter} to
     * {@link EdgeNGramTokenFilter}.
     */
    public static EdgeNGramTokenFilter map(com.azure.search.documents.implementation.models.EdgeNGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenFilter edgeNGramTokenFilter = new EdgeNGramTokenFilter();

        String _name = obj.getName();
        edgeNGramTokenFilter.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        edgeNGramTokenFilter.setMaxGram(_maxGram);

        if (obj.getSide() != null) {
            EdgeNGramTokenFilterSide _side = EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilter.setSide(_side);
        }

        Integer _minGram = obj.getMinGram();
        edgeNGramTokenFilter.setMinGram(_minGram);
        return edgeNGramTokenFilter;
    }

    /**
     * Maps from {@link EdgeNGramTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.EdgeNGramTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.EdgeNGramTokenFilter map(EdgeNGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.EdgeNGramTokenFilter edgeNGramTokenFilter =
            new com.azure.search.documents.implementation.models.EdgeNGramTokenFilter();

        String _name = obj.getName();
        edgeNGramTokenFilter.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        edgeNGramTokenFilter.setMaxGram(_maxGram);

        if (obj.getSide() != null) {
            com.azure.search.documents.implementation.models.EdgeNGramTokenFilterSide _side =
                EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilter.setSide(_side);
        }

        Integer _minGram = obj.getMinGram();
        edgeNGramTokenFilter.setMinGram(_minGram);
        return edgeNGramTokenFilter;
    }
}
