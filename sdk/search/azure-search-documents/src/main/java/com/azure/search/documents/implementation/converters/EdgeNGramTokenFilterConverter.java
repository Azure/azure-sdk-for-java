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

        return edgeNGramTokenFilter.setMaxGram(obj.getMaxGram())
            .setMinGram(obj.getMinGram())
            .setSide(obj.getSide());
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

        return edgeNGramTokenFilter.setMaxGram(obj.getMaxGram())
            .setMinGram(obj.getMinGram())
            .setSide(obj.getSide());
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

        if (V1_ODATA_TYPE.equals(EdgeNGramTokenFilterHelper.getODataType(obj))) {
            return new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter(obj.getName())
                .setSide(obj.getSide())
                .setMaxGram(obj.getMaxGram())
                .setMinGram(obj.getMinGram());
        } else {
            return new com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2(obj.getName())
                .setSide(obj.getSide())
                .setMaxGram(obj.getMaxGram())
                .setMinGram(obj.getMinGram());
        }
    }

    private EdgeNGramTokenFilterConverter() {
    }
}
