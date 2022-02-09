// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SuggestResult;

/**
 * The helper class to set the non-public properties of an {@link SuggestResult} instance.
 */
public final class SuggestResultHelper {
    private static SuggestResultAccessor accessor;

    private SuggestResultHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SuggestResult} instance.
     */
    public interface SuggestResultAccessor {
        void setAdditionalProperties(SuggestResult suggestResult, SearchDocument additionalProperties);
    }

    /**
     * The method called from {@link SuggestResult} to set it's accessor.
     *
     * @param suggestResultAccessor The accessor.
     */
    public static void setAccessor(final SuggestResultAccessor suggestResultAccessor) {
        accessor = suggestResultAccessor;
    }

    static void setAdditionalProperties(SuggestResult suggestResult, SearchDocument additionalProperties) {
        accessor.setAdditionalProperties(suggestResult, additionalProperties);
    }
}
