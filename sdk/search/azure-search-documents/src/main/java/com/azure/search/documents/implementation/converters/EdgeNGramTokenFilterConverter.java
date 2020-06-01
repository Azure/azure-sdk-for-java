// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.EdgeNGramTokenFilter;
import com.azure.search.documents.indexes.models.EdgeNGramTokenFilterSide;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter} and
 * {@link EdgeNGramTokenFilter}.
 */
public final class EdgeNGramTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter} to
     * {@link EdgeNGramTokenFilter}.
     */
    public static EdgeNGramTokenFilter map(com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenFilter edgeNGramTokenFilter = new EdgeNGramTokenFilter();

        String name = obj.getName();
        edgeNGramTokenFilter.setName(name);

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenFilter.setMaxGram(maxGram);

        if (obj.getSide() != null) {
            EdgeNGramTokenFilterSide side = EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilter.setSide(side);
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenFilter.setMinGram(minGram);
        return edgeNGramTokenFilter;
    }

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2} to
     * {@link EdgeNGramTokenFilter}.
     */
    public static EdgeNGramTokenFilter map(com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenFilter edgeNGramTokenFilter = new EdgeNGramTokenFilter();

        String name = obj.getName();
        edgeNGramTokenFilter.setName(name);

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenFilter.setMaxGram(maxGram);

        if (obj.getSide() != null) {
            EdgeNGramTokenFilterSide side = EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilter.setSide(side);
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenFilter.setMinGram(minGram);
        return edgeNGramTokenFilter;
    }

    /**
     * Maps from {@link EdgeNGramTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2 map(EdgeNGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2 edgeNGramTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2();

        String name = obj.getName();
        edgeNGramTokenFilter.setName(name);

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenFilter.setMaxGram(maxGram);

        if (obj.getSide() != null) {
            com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide side =
                EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilter.setSide(side);
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenFilter.setMinGram(minGram);
        return edgeNGramTokenFilter;
    }

    private EdgeNGramTokenFilterConverter() {
    }
}
