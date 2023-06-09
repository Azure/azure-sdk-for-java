// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.EdgeNGramTokenFilter;

/**
 * The helper class to set the non-public properties of an {@link EdgeNGramTokenFilter} instance.
 */
public final class EdgeNGramTokenFilterHelper {
    private static EdgeNGramTokenFilterAccessor accessor;

    private EdgeNGramTokenFilterHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link EdgeNGramTokenFilter} instance.
     */
    public interface EdgeNGramTokenFilterAccessor {
        void setODataType(EdgeNGramTokenFilter tokenFilter, String odataType);
        String getODataType(EdgeNGramTokenFilter tokenFilter);
    }

    /**
     * The method called from {@link EdgeNGramTokenFilter} to set it's accessor.
     *
     * @param tokenFilterAccessor The accessor.
     */
    public static void setAccessor(final EdgeNGramTokenFilterAccessor tokenFilterAccessor) {
        accessor = tokenFilterAccessor;
    }

    static void setODataType(EdgeNGramTokenFilter tokenFilter, String odataType) {
        accessor.setODataType(tokenFilter, odataType);
    }

    static String getODataType(EdgeNGramTokenFilter tokenFilter) {
        return accessor.getODataType(tokenFilter);
    }
}
