// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation.util;

import com.azure.search.documents.models.QueryAnswerResult;
import com.azure.search.documents.models.SemanticErrorReason;
import com.azure.search.documents.models.SemanticQueryRewritesResultType;
import com.azure.search.documents.models.SemanticSearchResults;
import com.azure.search.documents.models.SemanticSearchResultsType;

import java.util.List;

/**
 * Helper class to access internals of {@link SemanticSearchResults}.
 */
public final class SemanticSearchResultsAccessHelper {
    private SemanticSearchResultsAccessHelper() {
    }

    private static SemanticSearchResultsAccessor accessor;

    public interface SemanticSearchResultsAccessor {
        SemanticSearchResults create(List<QueryAnswerResult> queryAnswers, SemanticErrorReason semanticErrorReason,
            SemanticSearchResultsType semanticSearchResultsType,
            SemanticQueryRewritesResultType semanticQueryRewritesResultType);
    }

    public static void setAccessor(final SemanticSearchResultsAccessor newAccessor) {
        accessor = newAccessor;
    }

    public static SemanticSearchResults create(List<QueryAnswerResult> queryAnswers,
        SemanticErrorReason semanticErrorReason, SemanticSearchResultsType semanticSearchResultsType,
        SemanticQueryRewritesResultType semanticQueryRewritesResultType) {
        if (accessor == null) {
            try {
                Class.forName(SemanticSearchResults.class.getName(), true,
                    SemanticSearchResultsAccessHelper.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        assert accessor != null;
        return accessor.create(queryAnswers, semanticErrorReason, semanticSearchResultsType,
            semanticQueryRewritesResultType);
    }
}
