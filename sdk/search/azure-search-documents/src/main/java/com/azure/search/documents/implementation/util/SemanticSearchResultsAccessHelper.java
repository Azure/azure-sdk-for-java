// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation.util;

import com.azure.search.documents.models.SemanticSearchResults;
import com.azure.search.documents.util.SearchPagedResponse;

/**
 * Helper class to access internals of {@link SemanticSearchResults}.
 */
public final class SemanticSearchResultsAccessHelper {
    private SemanticSearchResultsAccessHelper() {
    }

    private static SemanticSearchResultsAccessor accessor;

    public interface SemanticSearchResultsAccessor {
        SemanticSearchResults create(SearchPagedResponse pagedResponse);
    }

    public static void setAccessor(final SemanticSearchResultsAccessor newAccessor) {
        accessor = newAccessor;
    }

    public static SemanticSearchResults create(SearchPagedResponse pagedResponse) {
        if (accessor == null) {
            try {
                Class.forName(SemanticSearchResults.class.getName(), true,
                    SemanticSearchResultsAccessHelper.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        assert accessor != null;
        return accessor.create(pagedResponse);
    }
}
