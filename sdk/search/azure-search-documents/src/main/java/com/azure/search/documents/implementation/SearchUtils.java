// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonSerializable;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchRequest;
import reactor.core.publisher.Mono;

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

    /**
     * Adds headers from {@link SearchOptions} to {@link RequestOptions}.
     *
     * @param requestOptions The {@link RequestOptions}.
     * @param searchOptions The {@link SearchOptions}.
     * @return The updated {@link RequestOptions}.
     */
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

    /**
     * Converts a {@link Response} of {@link BinaryData} to a {@link Response} of a specific type that extends
     * {@link JsonSerializable}.
     *
     * @param <T> The type of the class extending {@link JsonSerializable}.
     * @param response The {@link Response} of {@link BinaryData}.
     * @param clazz The class type to convert to.
     * @return The converted {@link Response}.
     */
    public static <T extends JsonSerializable<T>> Response<T> convertResponse(Response<BinaryData> response,
        Class<T> clazz) {
        return new SimpleResponse<>(response, response.getValue().toObject(clazz));
    }

    /**
     * Maps a {@link Mono} of {@link Response} of {@link BinaryData} to a {@link Mono} of {@link Response} of a
     * specific type that extends {@link JsonSerializable}.
     *
     * @param <T> The type of the class extending {@link JsonSerializable}.
     * @param mono The {@link Mono} of {@link Response} of {@link BinaryData}.
     * @param clazz The class type to convert to.
     * @return The mapped {@link Mono} of {@link Response}.
     */
    public static <T extends JsonSerializable<T>> Mono<Response<T>> mapResponse(Mono<Response<BinaryData>> mono,
        Class<T> clazz) {
        return mono.map(response -> new SimpleResponse<>(response, response.getValue().toObject(clazz)));
    }

    private SearchUtils() {
    }
}
