// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.EdgeNGramTokenFilterSide;
import com.azure.search.documents.models.EdgeNGramTokenFilterV2;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2} and
 * {@link EdgeNGramTokenFilterV2}.
 */
public final class EdgeNGramTokenFilterV2Converter {
    private static final ClientLogger LOGGER = new ClientLogger(EdgeNGramTokenFilterV2Converter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2} to
     * {@link EdgeNGramTokenFilterV2}.
     */
    public static EdgeNGramTokenFilterV2 map(com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenFilterV2 edgeNGramTokenFilterV2 = new EdgeNGramTokenFilterV2();

        String _name = obj.getName();
        edgeNGramTokenFilterV2.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        edgeNGramTokenFilterV2.setMaxGram(_maxGram);

        if (obj.getSide() != null) {
            EdgeNGramTokenFilterSide _side = EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilterV2.setSide(_side);
        }

        Integer _minGram = obj.getMinGram();
        edgeNGramTokenFilterV2.setMinGram(_minGram);
        return edgeNGramTokenFilterV2;
    }

    /**
     * Maps from {@link EdgeNGramTokenFilterV2} to
     * {@link com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2}.
     */
    public static com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2 map(EdgeNGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2 edgeNGramTokenFilterV2 =
            new com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2();

        String _name = obj.getName();
        edgeNGramTokenFilterV2.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        edgeNGramTokenFilterV2.setMaxGram(_maxGram);

        if (obj.getSide() != null) {
            com.azure.search.documents.implementation.models.EdgeNGramTokenFilterSide _side =
                EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilterV2.setSide(_side);
        }

        Integer _minGram = obj.getMinGram();
        edgeNGramTokenFilterV2.setMinGram(_minGram);
        return edgeNGramTokenFilterV2;
    }
}
