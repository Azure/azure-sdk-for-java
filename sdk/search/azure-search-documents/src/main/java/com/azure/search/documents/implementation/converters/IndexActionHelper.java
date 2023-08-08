// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.IndexAction;

import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link IndexAction} instance.
 */
public final class IndexActionHelper {
    private static IndexActionAccessor accessor;

    private IndexActionHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link IndexAction} instance.
     */
    public interface IndexActionAccessor {
        <U> void setProperties(IndexAction<U> indexAction, Map<String, Object> properties);
        <U> Map<String, Object> getProperties(IndexAction<U> indexAction);
    }

    /**
     * The method called from {@link IndexAction} to set it's accessor.
     *
     * @param indexActionAccessor The accessor.
     */
    public static void setAccessor(final IndexActionAccessor indexActionAccessor) {
        accessor = indexActionAccessor;
    }

    static <U> void setProperties(IndexAction<U> indexAction, Map<String, Object> properties) {
        accessor.setProperties(indexAction, properties);
    }

    static <U> Map<String, Object> getProperties(IndexAction<U> indexAction) {
        return accessor.getProperties(indexAction);
    }
}
