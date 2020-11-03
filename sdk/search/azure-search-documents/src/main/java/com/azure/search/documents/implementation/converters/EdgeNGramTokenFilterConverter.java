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
    private static final String V1_ODATA_TYPE = "#Microsoft.Azure.Search.EdgeNGramTokenFilter";
    private static final String V2_ODATA_TYPE = "#Microsoft.Azure.Search.EdgeNGramTokenFilterV2";
    private static final String ODATA_FIELD_NAME = "odataType";

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter} to
     * {@link EdgeNGramTokenFilter}.
     */
    public static EdgeNGramTokenFilter map(com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        EdgeNGramTokenFilter edgeNGramTokenFilter = new EdgeNGramTokenFilter(obj.getName());
        EdgeNGramTokenFilterHelper.setODataType(edgeNGramTokenFilter, V1_ODATA_TYPE);

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
        EdgeNGramTokenFilter edgeNGramTokenFilter = new EdgeNGramTokenFilter(obj.getName());
        EdgeNGramTokenFilterHelper.setODataType(edgeNGramTokenFilter, V2_ODATA_TYPE);

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
     * {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2} or
     * @link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter} depends on @odata.type.
     */
    public static com.azure.search.documents.indexes.implementation.models.TokenFilter map(EdgeNGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }

<<<<<<< HEAD
        String identifier = PrivateFieldAccessHelper.get(obj, ODATA_FIELD_NAME, String.class);
        com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide side = obj.getSide() == null ?
            null : EdgeNGramTokenFilterSideConverter.map(obj.getSide());
=======
        String identifier = EdgeNGramTokenFilterHelper.getODataType(obj);
        EdgeNGramTokenFilterSide side = obj.getSide() == null ? null : obj.getSide();
>>>>>>> bfd056a1647f7232e7d7cb82ca2a5ad85b9bb6ec

        if (V1_ODATA_TYPE.equals(identifier)) {
            com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter edgeNGramTokenFilter =
                new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter(obj.getName())
                    .setSide(side)
                    .setMaxGram(obj.getMaxGram())
                    .setMinGram(obj.getMinGram());
            edgeNGramTokenFilter.validate();
            return edgeNGramTokenFilter;
        } else {
            com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2 edgeNGramTokenFilter =
                new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2(obj.getName())
                    .setSide(side)
                    .setMaxGram(obj.getMaxGram())
                    .setMinGram(obj.getMinGram());
            edgeNGramTokenFilter.validate();
            return edgeNGramTokenFilter;
        }
    }

    private EdgeNGramTokenFilterConverter() {
    }
}
