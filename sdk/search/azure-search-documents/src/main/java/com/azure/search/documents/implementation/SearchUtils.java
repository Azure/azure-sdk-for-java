// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchRequest;

/**
 * Implementation utilities helper class.
 */
public final class SearchUtils {
    private static final HttpHeaderName X_MS_QUERY_SOURCE_AUTHORIZATION
        = HttpHeaderName.fromString("x-ms-query-source-authorization");
    private static final HttpHeaderName X_MS_ENABLE_ELEVATED_READ
        = HttpHeaderName.fromString("x-ms-enable-elevated-read");

    /**
     * Converts the public API {@link SearchOptions} into {@link SearchRequest}.
     *
     * @param options The {@link SearchOptions}.
     * @return An instance of {@link SearchRequest}.
     */
    public static SearchRequest fromSearchOptions(SearchOptions options) {
        if (options == null) {
            return null;
        }

        return new SearchRequest().setIncludeTotalCount(options.isIncludeTotalCount())
            .setFacets(options.getFacets())
            .setFilter(options.getFilter())
            .setHighlightFields(options.getHighlightFields())
            .setHighlightPostTag(options.getHighlightPostTag())
            .setHighlightPreTag(options.getHighlightPreTag())
            .setMinimumCoverage(options.getMinimumCoverage())
            .setOrderBy(options.getOrderBy())
            .setQueryType(options.getQueryType())
            .setScoringStatistics(options.getScoringStatistics())
            .setSessionId(options.getSessionId())
            .setScoringParameters(options.getScoringParameters())
            .setScoringProfile(options.getScoringProfile())
            .setDebug(options.getDebug())
            .setSearchText(options.getSearchText())
            .setSearchFields(options.getSearchFields())
            .setSearchMode(options.getSearchMode())
            .setQueryLanguage(options.getQueryLanguage())
            .setQuerySpeller(options.getQuerySpeller())
            .setSelect(options.getSelect())
            .setSkip(options.getSkip())
            .setTop(options.getTop())
            .setSemanticConfigurationName(options.getSemanticConfigurationName())
            .setSemanticErrorHandling(options.getSemanticErrorHandling())
            .setSemanticMaxWaitInMilliseconds(options.getSemanticMaxWaitInMilliseconds())
            .setSemanticQuery(options.getSemanticQuery())
            .setAnswers(options.getAnswers())
            .setCaptions(options.getCaptions())
            .setQueryRewrites(options.getQueryRewrites())
            .setSemanticFields(options.getSemanticFields())
            .setVectorQueries(options.getVectorQueries())
            .setVectorFilterMode(options.getVectorFilterMode())
            .setHybridSearch(options.getHybridSearch());
    }

    public static RequestOptions addSearchHeaders(RequestOptions requestOptions, SearchOptions searchOptions) {
        // If SearchOptions is null or is both query source authorization and enable elevated read aren't set
        // return requestOptions as-is.
        if (searchOptions == null
            || (CoreUtils.isNullOrEmpty(searchOptions.getQuerySourceAuthorization())
                && searchOptions.isEnableElevatedRead() == null)) {
            return requestOptions;
        }

        if (requestOptions == null) {
            requestOptions = new RequestOptions();
        }

        if (!CoreUtils.isNullOrEmpty(searchOptions.getQuerySourceAuthorization())) {
            requestOptions.setHeader(X_MS_QUERY_SOURCE_AUTHORIZATION, searchOptions.getQuerySourceAuthorization());
        }

        if (searchOptions.isEnableElevatedRead() != null) {
            requestOptions.setHeader(X_MS_ENABLE_ELEVATED_READ, Boolean.toString(searchOptions.isEnableElevatedRead()));
        }

        return requestOptions;
    }

    private SearchUtils() {
    }
}
