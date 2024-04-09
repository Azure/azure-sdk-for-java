// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.QueryAnswerResult;
import com.azure.search.documents.models.SemanticErrorReason;
import com.azure.search.documents.models.SemanticSearchResultsType;
import com.azure.search.documents.util.SearchPagedResponse;

import java.util.List;
import java.util.Map;

/**
 * Helper class to access internal values of {@link SearchPagedResponse}.
 */
public final class SearchPagedResponseAccessHelper {
    private SearchPagedResponseAccessHelper() {
    }

    private static SearchPagedResponseAccessor accessor;

    public interface SearchPagedResponseAccessor {
        /**
         * The percentage of the index covered in the search request.
         * <p>
         * If {@code minimumCoverage} wasn't supplied in the request this will be null.
         *
         * @param response The {@link SearchPagedResponse} being accessed.
         * @return The percentage of the index covered in the search request if {@code minimumCoverage} was set in the
         * request, otherwise null.
         */
        Double getCoverage(SearchPagedResponse response);

        /**
         * The facet query results based on the search request.
         * <p>
         * If {@code facets} weren't supplied in the request this will be null.
         *
         * @param response The {@link SearchPagedResponse} being accessed.
         * @return The facet query results if {@code facets} were supplied in the request, otherwise null.
         */
        Map<String, List<FacetResult>> getFacets(SearchPagedResponse response);

        /**
         * The approximate number of documents that matched the search and filter parameters in the request.
         * <p>
         * If {@code count} is set to {@code false} in the request this will be null.
         *
         * @param response The {@link SearchPagedResponse} being accessed.
         * @return The approximate number of documents that match the request if {@code count} is {@code true}, otherwise
         * null.
         */
        Long getCount(SearchPagedResponse response);

        /**
         * The answer results based on the search request.
         * <p>
         * If {@code answers} wasn't supplied in the request this will be null.
         *
         * @param response The {@link SearchPagedResponse} being accessed.
         * @return The answer results if {@code answers} were supplied in the request, otherwise null.
         */
        List<QueryAnswerResult> getQueryAnswers(SearchPagedResponse response);

        /**
         * The reason that a partial response was returned for a semantic search request.
         *
         * @param response The {@link SearchPagedResponse} being accessed.
         * @return The reason that a partial response was returned for a semantic search request.
         */
        SemanticErrorReason getSemanticErrorReason(SearchPagedResponse response);

        /**
         * Get the semanticPartialResponseType property: Type of partial response that was returned for a semantic search
         * request.
         *
         * @param response The {@link SearchPagedResponse} being accessed.
         * @return the semanticPartialResponseType value.
         */
        SemanticSearchResultsType getSemanticSearchResultsType(SearchPagedResponse response);
    }

    /**
     * The percentage of the index covered in the search request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be null.
     *
     * @param response The {@link SearchPagedResponse} being accessed.
     * @return The percentage of the index covered in the search request if {@code minimumCoverage} was set in the
     * request, otherwise null.
     */
    public static Double getCoverage(SearchPagedResponse response) {
        return accessor.getCoverage(response);
    }

    /**
     * The facet query results based on the search request.
     * <p>
     * If {@code facets} weren't supplied in the request this will be null.
     *
     * @param response The {@link SearchPagedResponse} being accessed.
     * @return The facet query results if {@code facets} were supplied in the request, otherwise null.
     */
    public static Map<String, List<FacetResult>> getFacets(SearchPagedResponse response) {
        return accessor.getFacets(response);
    }

    /**
     * The approximate number of documents that matched the search and filter parameters in the request.
     * <p>
     * If {@code count} is set to {@code false} in the request this will be null.
     *
     * @param response The {@link SearchPagedResponse} being accessed.
     * @return The approximate number of documents that match the request if {@code count} is {@code true}, otherwise
     * null.
     */
    public static Long getCount(SearchPagedResponse response) {
        return accessor.getCount(response);
    }

    /**
     * The answer results based on the search request.
     * <p>
     * If {@code answers} wasn't supplied in the request this will be null.
     *
     * @param response The {@link SearchPagedResponse} being accessed.
     * @return The answer results if {@code answers} were supplied in the request, otherwise null.
     */
    public static List<QueryAnswerResult> getQueryAnswers(SearchPagedResponse response) {
        return accessor.getQueryAnswers(response);
    }

    /**
     * The reason that a partial response was returned for a semantic search request.
     * @param response The {@link SearchPagedResponse} being accessed.
     * @return The reason that a partial response was returned for a semantic search request.
     */
    public static SemanticErrorReason getSemanticErrorReason(SearchPagedResponse response) {
        return accessor.getSemanticErrorReason(response);
    }

    /**
     * The type of partial response that was returned for a semantic search request.
     * @param response The {@link SearchPagedResponse} being accessed.
     * @return The type of partial response that was returned for a semantic search request.
     */
    public static SemanticSearchResultsType getSemanticSearchResultsType(SearchPagedResponse response) {
        return accessor.getSemanticSearchResultsType(response);
    }

    public static void setAccessor(SearchPagedResponseAccessor searchPagedResponseAccessor) {
        accessor = searchPagedResponseAccessor;
    }
}
