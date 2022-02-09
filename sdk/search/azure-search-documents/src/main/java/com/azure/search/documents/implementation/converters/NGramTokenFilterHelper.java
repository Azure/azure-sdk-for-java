// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.NGramTokenFilter;

/**
 * The helper class to set the non-public properties of an {@link NGramTokenFilter} instance.
 */
public final class NGramTokenFilterHelper {
    private static NGramTokenFilterAccessor accessor;

    private NGramTokenFilterHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link NGramTokenFilter} instance.
     */
    public interface NGramTokenFilterAccessor {
        void setODataType(NGramTokenFilter tokenFilter, String odataType);
        String getODataType(NGramTokenFilter tokenFilter);
    }

    /**
     * The method called from {@link NGramTokenFilter} to set it's accessor.
     *
     * @param tokenFilterAccessor The accessor.
     */
    public static void setAccessor(final NGramTokenFilterAccessor tokenFilterAccessor) {
        accessor = tokenFilterAccessor;
    }

    static void setODataType(NGramTokenFilter tokenFilter, String odataType) {
        accessor.setODataType(tokenFilter, odataType);
    }

    static String getODataType(NGramTokenFilter tokenFilter) {
        return accessor.getODataType(tokenFilter);
    }
}
