// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.EdgeNGramTokenFilterSide;
import com.azure.search.documents.indexes.models.EdgeNGramTokenFilterV2;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2} and
 * {@link EdgeNGramTokenFilterV2}.
 */
public final class EdgeNGramTokenFilterV2Converter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2} to
     * {@link EdgeNGramTokenFilterV2}.
     */
    public static EdgeNGramTokenFilterV2 map(com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenFilterV2 edgeNGramTokenFilterV2 = new EdgeNGramTokenFilterV2();

        String name = obj.getName();
        edgeNGramTokenFilterV2.setName(name);

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenFilterV2.setMaxGram(maxGram);

        if (obj.getSide() != null) {
            EdgeNGramTokenFilterSide side = EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilterV2.setSide(side);
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenFilterV2.setMinGram(minGram);
        return edgeNGramTokenFilterV2;
    }

    /**
     * Maps from {@link EdgeNGramTokenFilterV2} to
     * {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2 map(EdgeNGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2 edgeNGramTokenFilterV2 =
            new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2();

        String name = obj.getName();
        edgeNGramTokenFilterV2.setName(name);

        Integer maxGram = obj.getMaxGram();
        edgeNGramTokenFilterV2.setMaxGram(maxGram);

        if (obj.getSide() != null) {
            com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide side =
                EdgeNGramTokenFilterSideConverter.map(obj.getSide());
            edgeNGramTokenFilterV2.setSide(side);
        }

        Integer minGram = obj.getMinGram();
        edgeNGramTokenFilterV2.setMinGram(minGram);
        return edgeNGramTokenFilterV2;
    }

    private EdgeNGramTokenFilterV2Converter() {
    }
}
