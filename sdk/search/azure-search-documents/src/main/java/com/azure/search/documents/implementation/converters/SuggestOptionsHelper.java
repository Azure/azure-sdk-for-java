// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.SuggestOptions;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link SuggestOptions} instance.
 */
public final class SuggestOptionsHelper {
    private static SuggestOptionsAccessor accessor;

    private SuggestOptionsHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SuggestOptions} instance.
     */
    public interface SuggestOptionsAccessor {
        void setOrderBy(SuggestOptions suggestOptions, List<String> orderBy);
        void setSearchFields(SuggestOptions suggestOptions, List<String> searchFields);
        void setSelect(SuggestOptions suggestOptions, List<String> select);
    }

    /**
     * The method called from {@link SuggestOptions} to set it's accessor.
     *
     * @param optionsAccessor The accessor.
     */
    public static void setAccessor(final SuggestOptionsAccessor optionsAccessor) {
        accessor = optionsAccessor;
    }

    static void setOrderBy(SuggestOptions suggestOptions, List<String> orderBy) {
        accessor.setOrderBy(suggestOptions, orderBy);
    }

    static void setSearchFields(SuggestOptions suggestOptions, List<String> searchFields) {
        accessor.setSearchFields(suggestOptions, searchFields);
    }

    static void setSelect(SuggestOptions suggestOptions, List<String> select) {
        accessor.setSelect(suggestOptions, select);
    }
}
